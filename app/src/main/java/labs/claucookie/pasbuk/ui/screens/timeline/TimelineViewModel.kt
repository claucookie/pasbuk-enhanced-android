package labs.claucookie.pasbuk.ui.screens.timeline

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.data.mapper.toDomain
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.DuplicateJourneyNameException
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import labs.claucookie.pasbuk.domain.repository.PassRepository
import labs.claucookie.pasbuk.domain.usecase.CreateJourneyUseCase
import labs.claucookie.pasbuk.domain.usecase.GetTimelineUseCase
import labs.claucookie.pasbuk.domain.usecase.ImportPassUseCase
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimelineUseCase: GetTimelineUseCase,
    private val importPassUseCase: ImportPassUseCase,
    private val createJourneyUseCase: CreateJourneyUseCase,
    private val passRepository: PassRepository,
    private val moshi: Moshi
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _events = Channel<TimelineEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Paging data flow for efficient handling of large lists
    val pagedPasses: Flow<PagingData<Pass>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 40
        )
    ) {
        passRepository.getAllPassesSortedByDatePaged()
    }.flow
        .map { pagingData ->
            pagingData.map { entity -> entity.toDomain(moshi) }
        }
        .cachedIn(viewModelScope)

    init {
        loadPasses()
    }

    private fun loadPasses() {
        viewModelScope.launch {
            getTimelineUseCase()
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

    fun createJourney(name: String) {
        viewModelScope.launch {
            val selectedPassIds = getSelectedPasses()
            if (selectedPassIds.isEmpty()) {
                _events.send(TimelineEvent.ShowSnackbar("Please select at least one pass"))
                return@launch
            }

            val result = createJourneyUseCase(name, selectedPassIds)
            result.fold(
                onSuccess = { journey ->
                    clearSelection()
                    _events.send(TimelineEvent.ShowSnackbar("Journey created successfully"))
                    _events.send(TimelineEvent.NavigateToJourneyDetail(journey.id))
                },
                onFailure = { e ->
                    val errorMessage = when (e) {
                        is DuplicateJourneyNameException -> "A journey with this name already exists"
                        is IllegalArgumentException -> e.message ?: "Invalid journey data"
                        else -> e.message ?: "Failed to create journey"
                    }
                    _events.send(TimelineEvent.ShowSnackbar(errorMessage))
                }
            )
        }
    }
}
