package com.dynodevv.mercury

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dynodevv.mercury.ui.screens.browser.BrowserScreen
import com.dynodevv.mercury.ui.screens.browser.BrowserViewModel
import com.dynodevv.mercury.ui.screens.history.HistoryScreen
import com.dynodevv.mercury.ui.screens.search.SearchScreen
import com.dynodevv.mercury.ui.screens.settings.AiSettingsScreen
import com.dynodevv.mercury.ui.screens.settings.PrivacySettingsScreen
import com.dynodevv.mercury.ui.screens.settings.SearchSettingsScreen
import com.dynodevv.mercury.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Browser : Screen("browser")
    data object Settings : Screen("settings")
    data object AiSettings : Screen("ai_settings")
    data object SearchSettings : Screen("search_settings")
    data object PrivacySettings : Screen("privacy_settings")
    data object History : Screen("history")
    data object Search : Screen("search/{query}") {
        fun createRoute(query: String) = "search/${java.net.URLEncoder.encode(query, "UTF-8")}"
    }
}

@Composable
fun MercuryApp() {
    val navController = rememberNavController()
    val browserViewModel: BrowserViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Browser.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
        }
    ) {
        composable(Screen.Browser.route) {
            BrowserScreen(
                viewModel = browserViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSearch = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAiSettings = { navController.navigate(Screen.AiSettings.route) },
                onNavigateToSearchSettings = { navController.navigate(Screen.SearchSettings.route) },
                onNavigateToPrivacySettings = { navController.navigate(Screen.PrivacySettings.route) }
            )
        }
        composable(Screen.AiSettings.route) {
            AiSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.SearchSettings.route) {
            SearchSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onUrlSelected = { url ->
                    browserViewModel.loadUrl(url)
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Search.route) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            SearchScreen(
                query = query,
                onNavigateBack = { navController.popBackStack() },
                onUrlSelected = { url ->
                    browserViewModel.loadUrl(url)
                    navController.popBackStack()
                }
            )
        }
    }
}
