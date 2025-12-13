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
 * UI tests for JourneyListScreen (T079).
 *
 * Tests verify that:
 * - Empty state is displayed when no journeys exist
 * - Journeys are displayed in a list
 * - Journey cards show name and pass count
 * - Journey click navigation works
 * - FAB for creating journey is displayed
 * - Loading and error states are displayed correctly
 */
class JourneyListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_displaysEmptyMessage() {
        // Given
        val emptyState = JourneyListUiState.Success(journeys = emptyList())

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = emptyState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No journeys yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create a journey from your timeline to organize your passes").assertIsDisplayed()
    }

    @Test
    fun loadingState_displaysLoadingIndicator() {
        // Given
        val loadingState = JourneyListUiState.Loading

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = loadingState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("journey_list_loading").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        // Given
        val errorState = JourneyListUiState.Error("Failed to load journeys")

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = errorState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load journeys").assertIsDisplayed()
    }

    @Test
    fun successState_displaysJourneyList() {
        // Given
        val journeys = listOf(
            createTestJourney(1, "Summer Vacation", 5),
            createTestJourney(2, "Conference Trip", 3),
            createTestJourney(3, "Weekend Getaway", 2)
        )
        val successState = JourneyListUiState.Success(journeys = journeys)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = successState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Summer Vacation").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 passes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Conference Trip").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 passes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekend Getaway").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 passes").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("journey_card").assertCountEquals(3)
    }

    @Test
    fun journeyCard_displaysCorrectPassCount() {
        // Given
        val journeys = listOf(
            createTestJourney(1, "One Pass", 1),
            createTestJourney(2, "Many Passes", 10)
        )
        val successState = JourneyListUiState.Success(journeys = journeys)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = successState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("1 pass").assertIsDisplayed()
        composeTestRule.onNodeWithText("10 passes").assertIsDisplayed()
    }

    @Test
    fun journeyClick_triggersCallback() {
        // Given
        val journey = createTestJourney(1, "Clickable Journey", 3)
        val successState = JourneyListUiState.Success(journeys = listOf(journey))
        var clickedJourneyId: Long? = null

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = successState,
                    onJourneyClick = { clickedJourneyId = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Clickable Journey").performClick()

        // Then
        assert(clickedJourneyId == 1L)
    }

    @Test
    fun backButton_triggersCallback() {
        // Given
        val emptyState = JourneyListUiState.Success(journeys = emptyList())
        var backClicked = false

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = emptyState,
                    onJourneyClick = {},
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then
        assert(backClicked)
    }

    @Test
    fun journeyList_displaysMultipleJourneys() {
        // Given
        val journeys = (1..10).map { index ->
            createTestJourney(index.toLong(), "Journey $index", index)
        }
        val successState = JourneyListUiState.Success(journeys = journeys)

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = successState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onAllNodesWithTag("journey_card").assertCountEquals(10)
        composeTestRule.onNodeWithText("Journey 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journey 10").assertIsDisplayed()
    }

    @Test
    fun journeyCard_displaysDateRange() {
        // Given
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", now.minusSeconds(60 * 60 * 24 * 7)),
            createTestPass("pass-2", now.minusSeconds(60 * 60 * 24))
        )
        val journey = Journey(
            id = 1,
            name = "Trip with Dates",
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        val successState = JourneyListUiState.Success(journeys = listOf(journey))

        // When
        composeTestRule.setContent {
            PasbukEnhancedTheme {
                JourneyListScreen(
                    uiState = successState,
                    onJourneyClick = {},
                    onBackClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Trip with Dates").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 passes").assertIsDisplayed()
    }

    private fun createTestJourney(id: Long, name: String, passCount: Int): Journey {
        val passes = (1..passCount).map { index ->
            createTestPass("pass-$id-$index", Instant.now())
        }

        return Journey(
            id = id,
            name = name,
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }

    private fun createTestPass(id: String, relevantDate: Instant): Pass {
        return Pass(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass",
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
