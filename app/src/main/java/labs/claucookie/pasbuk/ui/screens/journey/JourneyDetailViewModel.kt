package labs.claucookie.pasbuk.ui.screens.journey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.domain.usecase.DeleteJourneyUseCase
import labs.claucookie.pasbuk.domain.usecase.GetJourneyDetailUseCase
import labs.claucookie.pasbuk.ui.navigation.Screen
import javax.inject.Inject

/**
 * ViewModel for JourneyDetailScreen.
 *
 * Manages journey details display and deletion.
 */
@HiltViewModel
class JourneyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJourneyDetailUseCase: GetJourneyDetailUseCase,
    private val deleteJourneyUseCase: DeleteJourneyUseCase
) : ViewModel() {

    private val journeyId: Long = savedStateHandle.get<Long>(Screen.JourneyDetail.ARG_JOURNEY_ID)
        ?: throw IllegalArgumentException("Journey ID is required")

    private val _uiState = MutableStateFlow<JourneyDetailUiState>(JourneyDetailUiState.Loading)
    val uiState: StateFlow<JourneyDetailUiState> = _uiState.asStateFlow()

    init {
        loadJourney()
    }

    private fun loadJourney() {
        viewModelScope.launch {
            try {
                val journey = getJourneyDetailUseCase(journeyId)
                if (journey != null) {
                    _uiState.value = JourneyDetailUiState.Success(journey)
                } else {
                    _uiState.value = JourneyDetailUiState.Error("Journey not found")
                }
            } catch (e: Exception) {
                _uiState.value = JourneyDetailUiState.Error(
                    e.message ?: "Failed to load journey"
                )
            }
        }
    }

    fun deleteJourney() {
        viewModelScope.launch {
            try {
                deleteJourneyUseCase(journeyId)
                // Navigation handled by the screen after deletion
            } catch (e: Exception) {
                _uiState.value = JourneyDetailUiState.Error(
                    e.message ?: "Failed to delete journey"
                )
            }
        }
    }
}
