package labs.claucookie.pasbuk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import labs.claucookie.pasbuk.ui.screens.journey.JourneyDetailScreen
import labs.claucookie.pasbuk.ui.screens.journey.JourneyListScreen
import labs.claucookie.pasbuk.ui.screens.passdetail.PassDetailScreen
import labs.claucookie.pasbuk.ui.screens.timeline.TimelineScreen

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    /**
     * Timeline screen - displays all passes in chronological order.
     * This is the start destination.
     */
    data object Timeline : Screen("timeline")

    /**
     * Pass detail screen - displays details of a single pass.
     *
     * @property passId The ID of the pass to display
     */
    data object PassDetail : Screen("pass_detail/{passId}") {
        fun createRoute(passId: String): String = "pass_detail/$passId"

        const val ARG_PASS_ID = "passId"
    }

    /**
     * Journey list screen - displays all user-created journeys.
     */
    data object JourneyList : Screen("journey_list")

    /**
     * Journey detail screen - displays details of a single journey with its passes.
     *
     * @property journeyId The ID of the journey to display
     */
    data object JourneyDetail : Screen("journey_detail/{journeyId}") {
        fun createRoute(journeyId: Long): String = "journey_detail/$journeyId"

        const val ARG_JOURNEY_ID = "journeyId"
    }
}

/**
 * Main navigation composable for the Pasbuk app.
 *
 * @param modifier Modifier for the NavHost
 * @param navController Navigation controller for managing navigation state
 * @param startDestination The initial screen to display (defaults to Timeline)
 */
@Composable
fun PasbukNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Timeline.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Timeline screen - start destination
        composable(route = Screen.Timeline.route) {
            TimelineScreen(
                onNavigateToPassDetail = { passId ->
                    navController.navigate(Screen.PassDetail.createRoute(passId))
                },
                onNavigateToJourneys = {
                    navController.navigate(Screen.JourneyList.route)
                },
                onNavigateToJourneyDetail = { journeyId ->
                    navController.navigate(Screen.JourneyDetail.createRoute(journeyId))
                }
            )
        }

        // Pass detail screen
        composable(
            route = Screen.PassDetail.route,
            arguments = listOf(
                navArgument(Screen.PassDetail.ARG_PASS_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            PassDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Journey list screen
        composable(route = Screen.JourneyList.route) {
            JourneyListScreen(
                onJourneyClick = { journeyId ->
                    navController.navigate(Screen.JourneyDetail.createRoute(journeyId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Journey detail screen
        composable(
            route = Screen.JourneyDetail.route,
            arguments = listOf(
                navArgument(Screen.JourneyDetail.ARG_JOURNEY_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            JourneyDetailScreen(
                onPassClick = { passId ->
                    navController.navigate(Screen.PassDetail.createRoute(passId))
                },
                onBackClick = { navController.popBackStack() },
                onDeleteClick = { navController.popBackStack() }
            )
        }
    }
}
