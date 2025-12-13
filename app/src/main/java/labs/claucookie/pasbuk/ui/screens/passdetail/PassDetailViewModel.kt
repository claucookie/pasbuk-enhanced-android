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

    fun onBackClick() {
        viewModelScope.launch {
            _events.send(PassDetailEvent.NavigateBack)
        }
    }

    fun retry() {
        loadPass()
    }
}
