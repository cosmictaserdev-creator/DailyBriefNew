package com.example.dailybreafrewrite.feature.saved

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.dailybreafrewrite.core.domain.model.Article
import kotlin.math.sin

@Composable
fun SavedScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedViewModel = hiltViewModel(),
    onArticleTapped: (url: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onAction(SavedAction.UpdateSearch(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Search saved...", color = Color(0xFF8E8E93))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E8E93))
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
        )

        Spacer(modifier = Modifier.height(16.dp))

        val query = state.searchQuery.lowercase().trim()
        val filtered = if (query.isEmpty()) state.savedArticles
        else state.savedArticles.filter {
            it.title.lowercase().contains(query) ||
                    (it.description?.lowercase()?.contains(query) == true)
        }

        if (filtered.isEmpty()) {
            EmptySavedState(modifier = Modifier.fillMaxSize())
        } else {
            SavedFanStack(
                articles = filtered,
                onToggleSaved = { viewModel.onAction(SavedAction.ToggleSaved(it)) },
                onArticleTapped = onArticleTapped,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SavedFanStack(
    articles: List<Article>,
    onToggleSaved: (String) -> Unit,
    onArticleTapped: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        itemsIndexed(
            items = articles,
            key = { _, a -> a.url },
        ) { index, article ->
            val rotation = ((index % 5) - 2) * 2f + sin(index.toFloat()) * 1.5f
            SavedFanCard(
                article = article,
                rotation = rotation,
                onToggleSaved = { onToggleSaved(article.url) },
                onCardTapped = { onArticleTapped(article.url) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SavedFanCard(
    article: Article,
    rotation: Float,
    onToggleSaved: () -> Unit,
    onCardTapped: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(stiffness = 200f, dampingRatio = 0.7f),
        label = "fanRotation",
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationZ = animatedRotation
                translationY = 0f
            }
            .clickable(onClick = onCardTapped),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
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
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Unsave",
                            tint = Color.White,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = article.sourceName,
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No saved articles yet",
                color = Color(0xFF8E8E93),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swipe right on articles to save them",
                color = Color(0xFF636366),
                fontSize = 14.sp,
            )
        }
    }
}
