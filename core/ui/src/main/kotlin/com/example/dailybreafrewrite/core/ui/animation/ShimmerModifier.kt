package com.example.dailybreafrewrite.core.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.dailybreafrewrite.core.ui.theme.ShimmerBase
import com.example.dailybreafrewrite.core.ui.theme.ShimmerHighlight

fun Modifier.shimmer(
    shimmerBase: Color = ShimmerBase,
    shimmerHighlight: Color = ShimmerHighlight,
    durationMillis: Int = 1400,
): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val brush = Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 500f, 0f),
    )
    background(brush)
}
