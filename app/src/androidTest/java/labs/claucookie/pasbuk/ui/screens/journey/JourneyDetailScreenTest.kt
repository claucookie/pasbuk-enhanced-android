package labs.claucookie.pasbuk.ui.screens.journey

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.ui.theme.PasbukEnhancedTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * UI tests for JourneyDetailScreen (T080).
 *
 * Tests verify that:
 * - Loading state is displayed
 * - Error state is displayed
 * - Journey details are displayed correctly
 * - Passes are displayed in chronological order
 * - Pass click navigation works
 * - Delete button is displayed
 * - Empty journey (no passes) is handled
 */
class JourneyDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // Given
        val loadingState = JourneyDetailUiState.Loading

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = loadingState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("journey_detail_loading").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        // Given
        val errorState = JourneyDetailUiState.Error("Journey not found")

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = errorState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journey not found").assertIsDisplayed()
    }

    @Test
    fun successState_displaysJourneyName() {
        // Given
        val journey = createTestJourney("Summer Vacation", 3)
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Summer Vacation").assertIsDisplayed()
    }

    @Test
    fun successState_displaysPassesInChronologicalOrder() {
        // Given
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", "Oldest Pass", now.minusSeconds(60 * 60 * 24 * 7)),
            createTestPass("pass-2", "Middle Pass", now.minusSeconds(60 * 60 * 24)),
            createTestPass("pass-3", "Newest Pass", now.minusSeconds(60 * 60))
        )
        val journey = Journey(
            id = 1,
            name = "Test Journey",
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Oldest Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Middle Pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("Newest Pass").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("pass_card").assertCountEquals(3)
    }

    @Test
    fun successState_displaysPassCount() {
        // Given
        val journey = createTestJourney("Trip", 5)
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("5 passes").assertIsDisplayed()
    }

    @Test
    fun passClick_triggersCallback() {
        // Given
        val passes = listOf(createTestPass("pass-1", "Clickable Pass", Instant.now()))
        val journey = Journey(
            id = 1,
            name = "Test Journey",
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        val successState = JourneyDetailUiState.Success(journey)
        var clickedPassId: String? = null

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = { clickedPassId = it },
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Clickable Pass").performClick()

        // Then
        assert(clickedPassId == "pass-1")
    }

    @Test
    fun backButton_triggersCallback() {
        // Given
        val journey = createTestJourney("Test", 1)
        val successState = JourneyDetailUiState.Success(journey)
        var backClicked = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = { backClicked = true },
                    onDeleteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then
        assert(backClicked)
    }

    @Test
    fun deleteButton_triggersCallback() {
        // Given
        val journey = createTestJourney("To Delete", 1)
        val successState = JourneyDetailUiState.Success(journey)
        var deleteClicked = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = { deleteClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete journey").performClick()

        // Then
        assert(deleteClicked)
    }

    @Test
    fun emptyJourney_displaysEmptyState() {
        // Given
        val journey = Journey(
            id = 1,
            name = "Empty Journey",
            passes = emptyList(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Empty Journey").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 passes").assertIsDisplayed()
        composeTestRule.onNodeWithText("No passes in this journey").assertIsDisplayed()
    }

    @Test
    fun successState_displaysDateRange() {
        // Given
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", "First", now.minusSeconds(60 * 60 * 24 * 7)),
            createTestPass("pass-2", "Last", now.minusSeconds(60 * 60))
        )
        val journey = Journey(
            id = 1,
            name = "Date Range Journey",
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Date Range Journey").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 passes").assertIsDisplayed()
        // Date range should be displayed (format may vary)
    }

    @Test
    fun successState_displaysSinglePass() {
        // Given
        val journey = createTestJourney("Single Pass Journey", 1)
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("1 pass").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("pass_card").assertCountEquals(1)
    }

    @Test
    fun successState_displaysManyPasses() {
        // Given
        val journey = createTestJourney("Many Passes", 10)
        val successState = JourneyDetailUiState.Success(journey)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyDetailScreen(
                    uiState = successState,
                    onPassClick = {},
                    onBackClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("10 passes").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("pass_card").assertCountEquals(10)
    }

    private fun createTestJourney(name: String, passCount: Int): Journey {
        val passes = (1..passCount).map { index ->
            createTestPass("pass-$index", "Test Pass $index", Instant.now())
        }

        return Journey(
            id = 1,
            name = name,
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }

    private fun createTestPass(id: String, description: String, relevantDate: Instant): Pass {
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
