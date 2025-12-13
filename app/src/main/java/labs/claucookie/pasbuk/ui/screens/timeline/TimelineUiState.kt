package labs.claucookie.pasbuk.ui.screens.timeline

import labs.claucookie.pasbuk.domain.model.Pass

/**
 * UI state for the Timeline screen.
 */
sealed interface TimelineUiState {
    /**
     * Initial loading state while fetching passes.
     */
    data object Loading : TimelineUiState

    /**
     * Success state with the loaded passes.
     */
    data class Success(
        val passes: List<Pass>,
        val selectedPassIds: Set<String> = emptySet(),
        val isSelectionMode: Boolean = false
    ) : TimelineUiState {
        val isEmpty: Boolean get() = passes.isEmpty()
        val selectedCount: Int get() = selectedPassIds.size
    }

    /**
     * Error state when passes could not be loaded.
     */
    data class Error(val message: String) : TimelineUiState
}

/**
 * Import state for tracking the import progress.
 */
sealed interface ImportState {
    data object Idle : ImportState
    data object Importing : ImportState
    data class Success(val passId: String) : ImportState
    data class Error(val message: String) : ImportState
}

/**
 * Events that can be triggered from the Timeline screen.
 */
sealed interface TimelineEvent {
    /**
     * Navigate to pass detail screen.
     */
    data class NavigateToPassDetail(val passId: String) : TimelineEvent

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : TimelineEvent

    /**
     * Navigate to journey list screen.
     */
    data object NavigateToJourneyList : TimelineEvent
}
