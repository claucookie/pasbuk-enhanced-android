package labs.claucookie.pasbuk.ui.screens.passdetail

import labs.claucookie.pasbuk.domain.model.Pass

/**
 * UI state for the Pass Detail screen.
 */
sealed interface PassDetailUiState {
    /**
     * Initial loading state while fetching pass data.
     */
    data object Loading : PassDetailUiState

    /**
     * Success state with the loaded pass data.
     */
    data class Success(val pass: Pass) : PassDetailUiState

    /**
     * Error state when pass could not be loaded.
     */
    data class Error(val message: String) : PassDetailUiState
}

/**
 * Events that can be triggered from the Pass Detail screen.
 */
sealed interface PassDetailEvent {
    /**
     * Navigate back to the previous screen.
     */
    data object NavigateBack : PassDetailEvent

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : PassDetailEvent

    /**
     * Pass was deleted successfully, navigate back.
     */
    data object PassDeleted : PassDetailEvent
}
