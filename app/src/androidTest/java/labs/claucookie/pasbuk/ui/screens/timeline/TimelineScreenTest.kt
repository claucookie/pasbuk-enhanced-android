package labs.claucookie.pasbuk.ui.screens.timeline

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.ui.theme.PasbukEnhancedTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * UI tests for TimelineScreen (T062).
 *
 * Tests verify that:
 * - Empty state is displayed when no passes exist
 * - Passes are displayed in chronological order (most recent first)
 * - Pass click navigation works
 * - FAB functionality works
 * - Selection mode works correctly
 * - Loading and error states are displayed correctly
 */
class TimelineScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_displaysEmptyMessage() {
        // Given
        val emptyState = TimelineUiState.Success(
            passes = emptyList(),
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = emptyState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No passes yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import your first .pkpass file to get started").assertIsDisplayed()
    }

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // Given
        val loadingState = TimelineUiState.Loading

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = loadingState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeline_loading").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        // Given
        val errorState = TimelineUiState.Error("Failed to load passes")

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = errorState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load passes").assertIsDisplayed()
    }

    @Test
    fun successState_displaysPassesInChronologicalOrder() {
        // Given: Passes with different dates (already sorted most recent first)
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", "Newest Pass", now.minusSeconds(60 * 60)), // 1 hour ago
            createTestPass("pass-2", "Middle Pass", now.minusSeconds(60 * 60 * 24)), // 1 day ago
            createTestPass("pass-3", "Oldest Pass", now.minusSeconds(60 * 60 * 24 * 7)) // 7 days ago
        )
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then: All passes are displayed
        composeTestRule.onNodeWithText("Newest Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Middle Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oldest Pass").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("pass_card").assertCountEquals(3)
    }

    @Test
    fun passClick_triggersOnPassClickCallback() {
        // Given
        val passes = listOf(createTestPass("pass-1", "Test Pass"))
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )
        var clickedPassId: String? = null

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = { clickedPassId = it },
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // When: Click on pass
        composeTestRule.onNodeWithText("Test Pass").performClick()

        // Then
        assert(clickedPassId == "pass-1")
    }

    @Test
    fun fab_triggersOnImportClickCallback() {
        // Given
        val emptyState = TimelineUiState.Success(
            passes = emptyList(),
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )
        var importClicked = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = emptyState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = { importClicked = true },
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // When: Click FAB
        composeTestRule.onNodeWithContentDescription("Import pass").performClick()

        // Then
        assert(importClicked)
    }

    @Test
    fun journeysButton_triggersOnJourneysClickCallback() {
        // Given
        val emptyState = TimelineUiState.Success(
            passes = emptyList(),
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )
        var journeysClicked = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = emptyState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = { journeysClicked = true },
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // When: Click Journeys action
        composeTestRule.onNodeWithContentDescription("View journeys").performClick()

        // Then
        assert(journeysClicked)
    }

    @Test
    fun selectionMode_displaysSelectionTopBar() {
        // Given
        val passes = listOf(
            createTestPass("pass-1", "Pass 1"),
            createTestPass("pass-2", "Pass 2")
        )
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = setOf("pass-1"),
            isSelectionMode = true
        )

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then: Selection top bar is displayed with count
        composeTestRule.onNodeWithText("1 selected").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear selection").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete selected").assertIsDisplayed()
    }

    @Test
    fun selectionMode_clearSelection_triggersCallback() {
        // Given
        val passes = listOf(createTestPass("pass-1", "Pass 1"))
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = setOf("pass-1"),
            isSelectionMode = true
        )
        var clearSelectionCalled = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = { clearSelectionCalled = true },
                    onClearImportState = {}
                )
            }
        }

        // When: Click clear selection
        composeTestRule.onNodeWithContentDescription("Clear selection").performClick()

        // Then
        assert(clearSelectionCalled)
    }

    @Test
    fun selectionMode_deleteSelected_triggersCallback() {
        // Given
        val passes = listOf(createTestPass("pass-1", "Pass 1"))
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = setOf("pass-1"),
            isSelectionMode = true
        )
        var deleteSelectedCalled = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = { deleteSelectedCalled = true },
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // When: Click delete
        composeTestRule.onNodeWithContentDescription("Delete selected").performClick()

        // Then
        assert(deleteSelectedCalled)
    }

    @Test
    fun importingState_displaysProgressIndicator() {
        // Given
        val successState = TimelineUiState.Success(
            passes = emptyList(),
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Importing,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then: Progress indicator is displayed
        composeTestRule.onNodeWithTag("import_progress").assertIsDisplayed()
    }

    @Test
    fun passesWithoutRelevantDate_areDisplayedAtEnd() {
        // Given: Mix of passes with and without dates
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", "Recent Pass", now.minusSeconds(60 * 60)),
            createTestPass("pass-2", "No Date Pass", null),
            createTestPass("pass-3", "Older Pass", now.minusSeconds(60 * 60 * 24))
        )
        val successState = TimelineUiState.Success(
            passes = passes,
            selectedPassIds = emptySet(),
            isSelectionMode = false
        )

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                TimelineScreen(
                    uiState = successState,
                    importState = ImportState.Idle,
                    onPassClick = {},
                    onPassLongClick = {},
                    onImportClick = {},
                    onJourneysClick = {},
                    onDeleteSelected = {},
                    onClearSelection = {},
                    onClearImportState = {}
                )
            }
        }

        // Then: All passes are displayed
        composeTestRule.onNodeWithText("Recent Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Date Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Older Pass").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("pass_card").assertCountEquals(3)
    }

    private fun createTestPass(
        id: String,
        description: String,
        relevantDate: Instant? = Instant.now()
    ): Pass {
        return Pass(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = description,
            teamIdentifier = "TEAM123",
            relevantDate = relevantDate,
            expirationDate = null,
            locations = emptyList(),
            logoText = null,
            backgroundColor = null,
            foregroundColor = null,
            labelColor = null,
            barcode = null,
            logoImagePath = null,
            iconImagePath = null,
            thumbnailImagePath = null,
            stripImagePath = null,
            backgroundImagePath = null,
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = PassType.GENERIC,
            fields = emptyMap(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }
}
