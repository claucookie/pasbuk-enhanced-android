package labs.claucookie.pasbuk.ui.screens.passdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.domain.usecase.DeletePassUseCase
import labs.claucookie.pasbuk.domain.usecase.GetPassDetailUseCase
import labs.claucookie.pasbuk.ui.navigation.Screen
import javax.inject.Inject

/**
 * ViewModel for the Pass Detail screen.
 *
 * Manages the state and user interactions for viewing a single pass, including:
 * - Loading and displaying pass details and metadata
 * - Handling pass deletion with confirmation
 * - Error handling and retry functionality
 *
 * The pass ID is extracted from navigation arguments via SavedStateHandle.
 */
@HiltViewModel
class PassDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPassDetailUseCase: GetPassDetailUseCase,
    private val deletePassUseCase: DeletePassUseCase
) : ViewModel() {

    private val passId: String = checkNotNull(savedStateHandle[Screen.PassDetail.ARG_PASS_ID])

    private val _uiState = MutableStateFlow<PassDetailUiState>(PassDetailUiState.Loading)
    val uiState: StateFlow<PassDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<PassDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadPass()
    }

    private fun loadPass() {
        viewModelScope.launch {
            _uiState.value = PassDetailUiState.Loading
            try {
                val pass = getPassDetailUseCase(passId)
                if (pass != null) {
                    _uiState.value = PassDetailUiState.Success(pass)
                } else {
                    _uiState.value = PassDetailUiState.Error("Pass not found")
                }
            } catch (e: Exception) {
                _uiState.value = PassDetailUiState.Error(
                    e.message ?: "Failed to load pass"
                )
            }
        }
    }

    /**
     * Handles the delete button click.
     *
     * Deletes the pass and its associated files. On success, shows a confirmation
     * message and navigates back to the timeline. On failure, shows an error message.
     */
    fun onDeleteClick() {
        viewModelScope.launch {
            val result = deletePassUseCase(passId)
            result.fold(
                onSuccess = {
                    _events.send(PassDetailEvent.ShowSnackbar("Pass deleted"))
                    _events.send(PassDetailEvent.PassDeleted)
                },
                onFailure = { e ->
                    _events.send(
                        PassDetailEvent.ShowSnackbar(
                            e.message ?: "Failed to delete pass"
                        )
                    )
                }
            )
        }
    }

    /**
     * Handles the back button click.
     *
     * Triggers navigation back to the previous screen (typically the timeline).
     */
    fun onBackClick() {
        viewModelScope.launch {
            _events.send(PassDetailEvent.NavigateBack)
        }
    }

    /**
     * Retries loading the pass details.
     *
     * Called when the user taps a retry button after a load failure.
     * Resets the state to loading and attempts to fetch the pass again.
     */
    fun retry() {
        loadPass()
    }
}
