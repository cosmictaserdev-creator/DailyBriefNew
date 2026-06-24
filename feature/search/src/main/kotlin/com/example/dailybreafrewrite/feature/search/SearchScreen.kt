package com.example.dailybreafrewrite.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.domain.model.Article

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SearchInput(
            query = state.query,
            onQueryChange = { viewModel.onAction(SearchAction.UpdateQuery(it)) },
            onClear = { viewModel.onAction(SearchAction.ClearQuery) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.query.isBlank()) {
            TrendingSection(
                trendingSearches = state.trendingSearches,
                onTrendingSelected = { viewModel.onAction(SearchAction.SelectTrending(it)) },
            )
        } else if (state.isSearching) {
            SearchShimmer(modifier = Modifier.fillMaxSize())
        } else if (state.results.isEmpty()) {
            EmptySearchState(modifier = Modifier.fillMaxSize())
        } else {
            SearchResults(
                results = state.results,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search articles...",
                color = Color(0xFF8E8E93),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF8E8E93),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF8E8E93),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { /* keyboard search handled by debounce */ }
        ),
    )
}

@Composable
private fun TrendingSection(
    trendingSearches: List<String>,
    onTrendingSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Trending",
                color = Color(0xFF8E8E93),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            itemsIndexed(trendingSearches) { _, term ->
                TrendingChip(
                    text = term,
                    onClick = { onTrendingSelected(term) },
                )
            }
        }
    }
}

@Composable
private fun TrendingChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFF2C2C2E))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = Color(0xFFE5E5EA),
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun SearchResults(
    results: List<Article>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        itemsIndexed(
            items = results,
            key = { _, article -> article.url },
        ) { index, article ->
            StaggeredSearchResultItem(
                article = article,
                index = index,
            )
        }
    }
}

@Composable
private fun StaggeredSearchResultItem(
    article: Article,
    index: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "staggerAlpha",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(stiffness = 300f)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
                ),
    ) {
        SearchResultCard(
            article = article,
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchResultCard(
    article: Article,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                if (article.urlToImage != null) {
                    AsyncImage(
                        model = article.urlToImage,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = article.sourceName.first().toString(),
                            color = Color(0xFF8E8E93),
                            fontSize = 20.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = article.sourceName,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let { desc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(5) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            ) { }
        }
    }
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No results found",
                color = Color(0xFF8E8E93),
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try a different search term",
                color = Color(0xFF636366),
                fontSize = 14.sp,
            )
        }
    }
}
