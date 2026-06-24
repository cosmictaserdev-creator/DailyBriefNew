package com.example.dailybreafrewrite.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.domain.model.Article
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CardStackLayout(
    articles: List<Article>,
    onSwipedLeft: (Article) -> Unit,
    onSwipedRight: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (articles.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val swipeThresholdPx = with(density) {
            (maxWidth * AppConstants.Card.SWIPE_DISPLACEMENT_FRACTION).toPx()
        }

        val visibleArticles = articles.drop(currentIndex).take(AppConstants.Card.STACK_COUNT)
        if (visibleArticles.isEmpty()) return@BoxWithConstraints

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 16.dp),
        ) {
            visibleArticles.reversed().forEachIndexed { reversedIndex, article ->
                val depthIndex = visibleArticles.size - 1 - reversedIndex
                val isTop = depthIndex == 0

                val offsetY = (depthIndex * 8).dp
                val scaleFactor = 1f - depthIndex * AppConstants.Card.STACK_SCALE_STEP

                if (isTop) {
                    SwipeableTopCard(
                        article = article,
                        swipeThresholdPx = swipeThresholdPx,
                        scaleFactor = scaleFactor,
                        onSwipedLeft = {
                            currentIndex++
                            onSwipedLeft(article)
                        },
                        onSwipedRight = {
                            currentIndex++
                            onSwipedRight(article)
                        },
                    )
                } else {
                    ArticleCard(
                        article = article,
                        modifier = Modifier
                            .offset(y = offsetY)
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableTopCard(
    article: Article,
    swipeThresholdPx: Float,
    scaleFactor: Float,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val progress = if (swipeThresholdPx > 0f) abs(offsetX.value) / swipeThresholdPx else 0f
    val likeAlpha = if (offsetX.value > 0) (progress * 0.9f).coerceIn(0f, 0.9f) else 0f
    val nopeAlpha = if (offsetX.value < 0) (progress * 0.9f).coerceIn(0f, 0.9f) else 0f

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = (offsetX.value / 20f).coerceIn(
                    -AppConstants.Card.MAX_ROTATION_DEGREES,
                    AppConstants.Card.MAX_ROTATION_DEGREES
                )
                scaleX = scaleFactor
                scaleY = scaleFactor
                alpha = (1f - progress * 0.15f).coerceIn(0.7f, 1f)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) >= swipeThresholdPx) {
                                val targetX = if (offsetX.value > 0) {
                                    swipeThresholdPx * 3
                                } else {
                                    -swipeThresholdPx * 3
                                }
                                offsetX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMedium,
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                    ),
                                )
                                if (targetX > 0) onSwipedRight() else onSwipedLeft()
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                    ),
                                )
                            }
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                    }
                }
            },
    ) {
        ArticleCard(article = article)

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .alpha(likeAlpha),
        ) {
            Text(
                text = "LIKE",
                color = Color(0xFF34C759),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .alpha(nopeAlpha),
        ) {
            Text(
                text = "NOPE",
                color = Color(0xFFFF3B30),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            )
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(AppConstants.Card.CORNER_RADIUS.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(
                        topStart = AppConstants.Card.CORNER_RADIUS.dp,
                        topEnd = AppConstants.Card.CORNER_RADIUS.dp,
                    )),
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
                            fontSize = 48.sp,
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
