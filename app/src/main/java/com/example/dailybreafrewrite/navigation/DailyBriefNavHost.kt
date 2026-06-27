package com.example.dailybreafrewrite.navigation

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.dailybreafrewrite.core.ui.component.NavTab
import com.example.dailybreafrewrite.feature.home.HomeScreen
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
            HomeScreen(
                onArticleTapped = { url ->
                    navController.navigate(Route.ArticleDetail(url = url))
                },
            )
        }
        composable<Route.Search> {
            SearchScreen(
                onArticleTapped = { url ->
                    navController.navigate(Route.ArticleDetail(url = url))
                },
            )
        }
        composable<Route.Saved> {
            SavedScreen(
                onArticleTapped = { url ->
                    navController.navigate(Route.ArticleDetail(url = url))
                },
            )
        }
        composable<Route.ArticleDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ArticleDetail>()
            val context = LocalContext.current
            LaunchedEffect(route.url) {
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                customTabsIntent.launchUrl(context, Uri.parse(route.url))
                navController.popBackStack()
            }
        }
    }
}


