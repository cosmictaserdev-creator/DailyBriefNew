package com.example.dailybreafrewrite.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.ui.animation.shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it.toString())
            viewModel.onAction(HomeAction.DismissError)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onAction(HomeAction.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                CategoryPillsRow(
                    categories = AppConstants.CATEGORIES,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.onAction(HomeAction.SelectCategory(category))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (state.isLoading && state.articles.isEmpty()) {
                    ShimmerStack(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp, bottom = 16.dp),
                    )
                } else {
                    CardStackLayout(
                        articles = state.articles,
                        onSwipedLeft = { viewModel.onAction(HomeAction.SwipeLeft) },
                        onSwipedRight = { viewModel.onAction(HomeAction.SwipeRight) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFFFF3B30),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun ShimmerStack(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shimmer(),
            shape = RoundedCornerShape(AppConstants.Card.CORNER_RADIUS.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) { }
    }
}
