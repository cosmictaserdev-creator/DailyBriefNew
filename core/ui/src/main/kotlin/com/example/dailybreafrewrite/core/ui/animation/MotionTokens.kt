package com.example.dailybreafrewrite.core.ui.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing

val SpringSnappy = spring<Float>(
    stiffness = Spring.StiffnessMedium,
    dampingRatio = Spring.DampingRatioMediumBouncy,
)

val SpringBouncy = spring<Float>(
    stiffness = Spring.StiffnessLow,
    dampingRatio = Spring.DampingRatioLowBouncy,
)

val SpringGentle = spring<Float>(
    stiffness = 200f,
    dampingRatio = 1f,
)

val EaseOutExpo = tween<Float>(
    durationMillis = 320,
    easing = FastOutSlowInEasing,
)

val MicroDuration = tween<Float>(
    durationMillis = 120,
    easing = FastOutSlowInEasing,
)
