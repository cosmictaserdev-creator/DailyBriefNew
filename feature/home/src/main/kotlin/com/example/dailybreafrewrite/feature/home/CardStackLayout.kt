package com.example.dailybreafrewrite.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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

private const val STACK_SCALE_STEP = 0.06f
private const val STACK_PEEK_X_DP = 22f
private const val STACK_PEEK_Y_DP = 12f
private const val STACK_TILT_DEG = 4f
private const val BLOCKED_DRAG_RESISTANCE = 0.3f

/**
 * Prev/next card deck with a 3D "peek-right" stack and depth promotion.
 *
 * Rest pose: each card behind the top is offset right + down, scaled down and tilted, so a
 * fanned sliver peeks on the right (matching the Home mockup).
 *
 * - Drag left past the threshold => the next card promotes into the top slot, commit [onNext].
 * - Drag right past the threshold => the previous card returns from the left into the top slot
 *   while the deck sinks back, commit [onPrevious]. The returning card is fully centered by the
 *   time the threshold is crossed, so the commit lands continuously.
 * - Blocked directions (no previous at the start, no next at the end) resist and spring back.
 *
 * The discrete [topIndex] is owned by the ViewModel; only the live drag [Animatable] lives here,
 * so every reaction runs inside [graphicsLayer] lambdas with no per-frame recomposition.
 */
@Composable
fun CardStackLayout(
    articles: List<Article>,
    topIndex: Int,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCardTapped: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (articles.isEmpty()) return
    val safeTop = topIndex.coerceIn(0, articles.lastIndex)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val swipeThresholdPx = widthPx * AppConstants.Card.SWIPE_DISPLACEMENT_FRACTION
        val peekXpx = with(density) { STACK_PEEK_X_DP.dp.toPx() }
        val peekYpx = with(density) { STACK_PEEK_Y_DP.dp.toPx() }
        val maxRot = AppConstants.Card.MAX_ROTATION_DEGREES
        val cardHeight = maxHeight * 0.72f

        val offsetX = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        val dragModifier = Modifier.pointerInput(safeTop, canGoNext, canGoPrevious) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    scope.launch {
                        val value = offsetX.value
                        when {
                            value <= -swipeThresholdPx && canGoNext -> {
                                offsetX.animateTo(-widthPx, commitSpring)
                                onNext()
                                offsetX.snapTo(0f)
                            }
                            value >= swipeThresholdPx && canGoPrevious -> {
                                offsetX.animateTo(widthPx, commitSpring)
                                onPrevious()
                                offsetX.snapTo(0f)
                            }
                            else -> offsetX.animateTo(0f, settleSpring)
                        }
                    }
                },
            ) { change, dragAmount ->
                change.consume()
                val raw = offsetX.value + dragAmount
                val resisted = when {
                    raw > 0f && !canGoPrevious -> raw * BLOCKED_DRAG_RESISTANCE
                    raw < 0f && !canGoNext -> raw * BLOCKED_DRAG_RESISTANCE
                    else -> raw
                }
                scope.launch { offsetX.snapTo(resisted) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .align(Alignment.TopCenter)
                .padding(start = 4.dp, end = 28.dp, top = 8.dp, bottom = 8.dp),
        ) {
            // Stack: top card plus the ones behind it, drawn back-to-front.
            for (depth in (AppConstants.Card.STACK_COUNT - 1) downTo 0) {
                val index = safeTop + depth
                if (index > articles.lastIndex) continue
                val article = articles[index]
                val isTop = depth == 0
                key(article.url) {
                    StackCard(
                        article = article,
                        onClick = { onCardTapped(article) },
                        modifier = Modifier
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                val off = offsetX.value
                                val nextProgress = (-off / swipeThresholdPx).coerceIn(0f, 1f)
                                val prevProgress = (off / swipeThresholdPx).coerceIn(0f, 1f)
                                // Cards rise toward the front on a "next" drag and
                                // sink toward the back on a "previous" drag.
                                val effDepth = (depth - nextProgress + prevProgress)
                                    .coerceAtLeast(0f)
                                val scale = 1f - effDepth * STACK_SCALE_STEP
                                scaleX = scale
                                scaleY = scale
                                if (isTop && off < 0f) {
                                    // Top card follows the finger on a forward drag.
                                    translationX = off
                                    translationY = 0f
                                    rotationZ = (off / widthPx * 2f * maxRot)
                                        .coerceIn(-maxRot, maxRot)
                                } else {
                                    translationX = effDepth * peekXpx
                                    translationY = effDepth * peekYpx
                                    rotationZ = effDepth * STACK_TILT_DEG
                                }
                            }
                            .then(if (isTop) dragModifier else Modifier),
                    )
                }
            }

            // The previous card returns from the left while dragging right; it reaches the
            // top slot exactly as the threshold is crossed, so the commit is seamless.
            if (canGoPrevious && safeTop - 1 >= 0) {
                val prevArticle = articles[safeTop - 1]
                key("prev-${prevArticle.url}") {
                    StackCard(
                        article = prevArticle,
                        onClick = { onCardTapped(prevArticle) },
                        modifier = Modifier.graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                            val off = offsetX.value
                            alpha = if (off > 0f) 1f else 0f
                            val p = (off / swipeThresholdPx).coerceIn(0f, 1f)
                            translationX = -widthPx * (1f - p)
                            rotationZ = -maxRot * (1f - p)
                        },
                    )
                }
            }
        }
    }
}

private val commitSpring = spring<Float>(
    stiffness = Spring.StiffnessMedium,
    dampingRatio = Spring.DampingRatioNoBouncy,
)

private val settleSpring = spring<Float>(
    stiffness = Spring.StiffnessMedium,
    dampingRatio = Spring.DampingRatioLowBouncy,
)

@Composable
private fun StackCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ArticleCard(article = article, onClick = onClick)
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppConstants.Card.CORNER_RADIUS.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .clip(
                        RoundedCornerShape(
                            topStart = AppConstants.Card.CORNER_RADIUS.dp,
                            topEnd = AppConstants.Card.CORNER_RADIUS.dp,
                        )
                    ),
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
                            text = article.sourceName.firstOrNull()?.toString() ?: "?",
                            color = Color(0xFF8E8E93),
                            fontSize = 48.sp,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = article.sourceName,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = article.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let { description ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
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
