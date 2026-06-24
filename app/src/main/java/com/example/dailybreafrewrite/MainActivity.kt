package com.example.dailybreafrewrite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dailybreafrewrite.core.ui.component.FloatingCapsuleNavBar
import com.example.dailybreafrewrite.core.ui.component.NavTab
import com.example.dailybreafrewrite.core.ui.theme.DailyBriefTheme
import com.example.dailybreafrewrite.feature.onboarding.OnboardingScreen
import com.example.dailybreafrewrite.navigation.DailyBriefNavGraph
import com.example.dailybreafrewrite.navigation.NavActions
import com.example.dailybreafrewrite.navigation.Route
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyBriefTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    DailyBriefApp()
                }
            }
        }
    }
}

@Composable
fun DailyBriefApp(modifier: Modifier = Modifier) {
    var onboardingCompleted by remember { mutableStateOf(false) }

    if (!onboardingCompleted) {
        OnboardingScreen(
            onCompleted = { onboardingCompleted = true },
            modifier = modifier,
        )
    } else {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val showNavBar = currentDestination?.hasRoute<Route.Home>() == true ||
                currentDestination?.hasRoute<Route.Search>() == true ||
                currentDestination?.hasRoute<Route.Saved>() == true

        val selectedTab = when {
            currentDestination?.hasRoute<Route.Home>() == true -> NavTab.HOME
            currentDestination?.hasRoute<Route.Search>() == true -> NavTab.SEARCH
            currentDestination?.hasRoute<Route.Saved>() == true -> NavTab.SAVED
            else -> NavTab.HOME
        }

        Box(modifier = modifier.fillMaxSize()) {
            DailyBriefNavGraph(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
            )

            FloatingCapsuleNavBar(
                selectedTab = selectedTab,
                visible = showNavBar,
                onTabSelected = { tab ->
                    NavActions.navigateToTab(navController, tab)
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
