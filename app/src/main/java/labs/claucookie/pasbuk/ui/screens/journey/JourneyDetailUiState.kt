package labs.claucookie.pasbuk.ui.screens.journey

import labs.claucookie.pasbuk.domain.model.Journey

/**
 * UI state for JourneyDetailScreen.
 */
sealed class JourneyDetailUiState {
    /**
     * Loading state - fetching journey from database.
     */
    data object Loading : JourneyDetailUiState()

    /**
     * Success state - journey loaded successfully with passes.
     *
     * @property journey The journey with all its passes sorted by relevantDate
     */
    data class Success(
        val journey: Journey
    ) : JourneyDetailUiState()

    /**
     * Error state - failed to load journey or journey not found.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : JourneyDetailUiState()
}
