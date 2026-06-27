package com.example.dailybreafrewrite.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.ui.animation.shimmer

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onArticleTapped: (url: String) -> Unit = {},
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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CategoryPillsRow(
                    categories = AppConstants.CATEGORIES,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.onAction(HomeAction.SelectCategory(category))
                    },
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = { viewModel.onAction(HomeAction.ShowFilterSheet) },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Color(0xFF8E8E93),
                    )
                }
            }

            if (state.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0A84FF),
                    trackColor = Color(0xFF1C1C1E),
                )
            }

            if (state.isLoading && state.articles.isEmpty()) {
                ShimmerStack(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 16.dp),
                )
            } else {
                CardStackLayout(
                    articles = state.articles,
                    topIndex = state.topIndex,
                    canGoNext = state.topIndex < state.articles.lastIndex,
                    canGoPrevious = state.topIndex > 0,
                    onNext = { viewModel.onAction(HomeAction.Next) },
                    onPrevious = { viewModel.onAction(HomeAction.Previous) },
                    onCardTapped = { article -> onArticleTapped(article.url) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                )
            }
        }

        if (state.showFilterSheet) {
            FilterBottomSheet(
                current = state.filterPreferences,
                onApply = { viewModel.onAction(HomeAction.ApplyFilter(it)) },
                onDismiss = { viewModel.onAction(HomeAction.HideFilterSheet) },
            )
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
    val cardCount = 3
    val stackScaleStep = 0.07f
    val cardHeightFraction = 0.72f

    BoxWithConstraints(modifier = modifier) {
        val cardHeight = maxHeight * cardHeightFraction

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        ) {
            for (i in (0 until cardCount).reversed()) {
                val depthIndex = cardCount - 1 - i
                val scaleFactor = 1f - depthIndex * stackScaleStep
                val offsetY = (depthIndex * 16).dp

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = offsetY)
                        .graphicsLayer {
                            scaleX = scaleFactor
                            scaleY = scaleFactor
                        }
                        .shimmer(),
                    shape = RoundedCornerShape(AppConstants.Card.CORNER_RADIUS.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1C1E),
                    ),
                ) { }
            }
        }
    }
}
