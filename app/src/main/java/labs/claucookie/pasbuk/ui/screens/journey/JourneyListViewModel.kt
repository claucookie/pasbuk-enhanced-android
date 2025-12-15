package labs.claucookie.pasbuk.ui.screens.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.domain.usecase.GetAllJourneysUseCase
import javax.inject.Inject

/**
 * ViewModel for JourneyListScreen.
 *
 * Manages the list of all journeys and provides navigation actions.
 */
@HiltViewModel
class JourneyListViewModel @Inject constructor(
    private val getAllJourneysUseCase: GetAllJourneysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<JourneyListUiState>(JourneyListUiState.Loading)
    val uiState: StateFlow<JourneyListUiState> = _uiState.asStateFlow()

    init {
        loadJourneys()
    }

    private fun loadJourneys() {
        viewModelScope.launch {
            getAllJourneysUseCase()
                .catch { e ->
                    _uiState.value = JourneyListUiState.Error(
                        e.message ?: "Failed to load journeys"
                    )
                }
                .collect { journeys ->
                    _uiState.value = JourneyListUiState.Success(journeys = journeys)
                }
        }
    }
}
