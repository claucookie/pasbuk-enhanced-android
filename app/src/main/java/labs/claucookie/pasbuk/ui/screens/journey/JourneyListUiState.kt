package labs.claucookie.pasbuk.ui.screens.journey

import labs.claucookie.pasbuk.domain.model.Journey

/**
 * UI state for JourneyListScreen.
 */
sealed class JourneyListUiState {
    /**
     * Loading state - fetching journeys from database.
     */
    data object Loading : JourneyListUiState()

    /**
     * Success state - journeys loaded successfully.
     *
     * @property journeys List of journeys sorted by creation date (most recent first)
     */
    data class Success(
        val journeys: List<Journey>
    ) : JourneyListUiState() {
        /**
         * True if there are no journeys.
         */
        val isEmpty: Boolean get() = journeys.isEmpty()
    }

    /**
     * Error state - failed to load journeys.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : JourneyListUiState()
}
