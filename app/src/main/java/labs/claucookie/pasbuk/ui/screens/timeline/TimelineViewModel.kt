package labs.claucookie.pasbuk.ui.screens.timeline

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import labs.claucookie.pasbuk.domain.repository.PassRepository
import labs.claucookie.pasbuk.domain.usecase.ImportPassUseCase
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val passRepository: PassRepository,
    private val importPassUseCase: ImportPassUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _events = Channel<TimelineEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadPasses()
    }

    private fun loadPasses() {
        viewModelScope.launch {
            passRepository.getAllPassesSortedByDate()
                .catch { e ->
                    _uiState.value = TimelineUiState.Error(
                        e.message ?: "Failed to load passes"
                    )
                }
                .collect { passes ->
                    val currentState = _uiState.value
                    val selectedIds = if (currentState is TimelineUiState.Success) {
                        currentState.selectedPassIds
                    } else {
                        emptySet()
                    }
                    val isSelectionMode = if (currentState is TimelineUiState.Success) {
                        currentState.isSelectionMode
                    } else {
                        false
                    }

                    _uiState.value = TimelineUiState.Success(
                        passes = passes,
                        selectedPassIds = selectedIds.filter { id ->
                            passes.any { it.id == id }
                        }.toSet(),
                        isSelectionMode = isSelectionMode && passes.isNotEmpty()
                    )
                }
        }
    }

    fun importPass(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Importing

            val result = importPassUseCase(uri)
            result.fold(
                onSuccess = { pass ->
                    _importState.value = ImportState.Success(pass.id)
                    _events.send(TimelineEvent.ShowSnackbar("Pass imported successfully"))
                    _events.send(TimelineEvent.NavigateToPassDetail(pass.id))
                },
                onFailure = { e ->
                    val errorMessage = when (e) {
                        is DuplicatePassException -> "This pass has already been imported"
                        is InvalidPassException -> "Invalid or corrupted pass file"
                        else -> e.message ?: "Failed to import pass"
                    }
                    _importState.value = ImportState.Error(errorMessage)
                    _events.send(TimelineEvent.ShowSnackbar(errorMessage))
                }
            )
        }
    }

    fun clearImportState() {
        _importState.value = ImportState.Idle
    }

    fun onPassClick(passId: String) {
        val currentState = _uiState.value
        if (currentState is TimelineUiState.Success && currentState.isSelectionMode) {
            togglePassSelection(passId)
        } else {
            viewModelScope.launch {
                _events.send(TimelineEvent.NavigateToPassDetail(passId))
            }
        }
    }

    fun onPassLongClick(passId: String) {
        val currentState = _uiState.value
        if (currentState is TimelineUiState.Success) {
            _uiState.value = currentState.copy(
                isSelectionMode = true,
                selectedPassIds = setOf(passId)
            )
        }
    }

    private fun togglePassSelection(passId: String) {
        val currentState = _uiState.value
        if (currentState is TimelineUiState.Success) {
            val newSelectedIds = if (currentState.selectedPassIds.contains(passId)) {
                currentState.selectedPassIds - passId
            } else {
                currentState.selectedPassIds + passId
            }

            _uiState.value = currentState.copy(
                selectedPassIds = newSelectedIds,
                isSelectionMode = newSelectedIds.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        val currentState = _uiState.value
        if (currentState is TimelineUiState.Success) {
            _uiState.value = currentState.copy(
                selectedPassIds = emptySet(),
                isSelectionMode = false
            )
        }
    }

    fun getSelectedPasses(): List<String> {
        val currentState = _uiState.value
        return if (currentState is TimelineUiState.Success) {
            currentState.selectedPassIds.toList()
        } else {
            emptyList()
        }
    }

    fun navigateToJourneys() {
        viewModelScope.launch {
            _events.send(TimelineEvent.NavigateToJourneyList)
        }
    }
}
