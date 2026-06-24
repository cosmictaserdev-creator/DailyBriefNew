package com.example.dailybreafrewrite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.dailybreafrewrite.core.ui.component.NavTab
import com.example.dailybreafrewrite.feature.home.HomeScreen
import com.example.dailybreafrewrite.feature.onboarding.OnboardingScreen
import com.example.dailybreafrewrite.feature.saved.SavedScreen
import com.example.dailybreafrewrite.feature.search.SearchScreen

object NavActions {
    fun navigateToTab(navController: NavHostController, tab: NavTab) {
        val route = when (tab) {
            NavTab.HOME -> Route.Home
            NavTab.SEARCH -> Route.Search
            NavTab.SAVED -> Route.Saved
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun DailyBriefNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,
    ) {
        composable<Route.Home> {
            HomeScreen()
        }
        composable<Route.Search> {
            SearchScreen()
        }
        composable<Route.Saved> {
            SavedScreen()
        }
        composable<Route.ArticleDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ArticleDetail>()
            ArticleDetailScreen(url = route.url)
        }
        composable<Route.Onboarding> {
            OnboardingScreen()
        }
    }
}

@Composable
private fun ArticleDetailScreen(url: String) {
}
