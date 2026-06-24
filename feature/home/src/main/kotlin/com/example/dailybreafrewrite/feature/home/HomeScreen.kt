package com.example.dailybreafrewrite.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.dailybreafrewrite.core.common.AppConstants

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    HomeContent(
        state = state,
        onCategorySelected = { category ->
            viewModel.onAction(HomeAction.SelectCategory(category))
        },
        onSwipedLeft = {},
        onSwipedRight = {},
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onCategorySelected: (String) -> Unit,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        CategoryPillsRow(
            categories = AppConstants.CATEGORIES,
            selectedCategory = state.selectedCategory,
            onCategorySelected = onCategorySelected,
            modifier = Modifier.fillMaxWidth(),
        )

        CardStackLayout(
            articles = state.articles,
            onSwipedLeft = { onSwipedLeft() },
            onSwipedRight = { onSwipedRight() },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        )
    }
}
