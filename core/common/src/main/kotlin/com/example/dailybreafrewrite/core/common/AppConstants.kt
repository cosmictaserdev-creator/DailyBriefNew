package com.example.dailybreafrewrite.core.common

object AppConstants {
    const val API_BASE_URL = "https://newsapi.org/v2/"
    const val PAGE_SIZE = 20

    object Card {
        const val WIDTH_FRACTION = 0.88f
        const val CORNER_RADIUS = 24
        const val STACK_SCALE_STEP = 0.05f
        const val SWIPE_VELOCITY_THRESHOLD = 500f
        const val SWIPE_DISPLACEMENT_FRACTION = 0.4f
        const val MAX_ROTATION_DEGREES = 15f
        const val STACK_COUNT = 3
        const val PREFETCH_THRESHOLD = 6
    }

    object Animation {
        const val SPRING_STIFFNESS = 400f
        const val SPRING_DAMPING = 28f
        const val SPRING_GENTLE_STIFFNESS = 200f
        const val SPRING_GENTLE_DAMPING = 30f
        const val SPRING_BOUNCY_STIFFNESS = 280f
        const val SPRING_BOUNCY_DAMPING = 22f
        const val SHIMMER_DURATION_MS = 1400L
        const val MICRO_DURATION_MS = 120L
        const val DEBOUNCE_MS = 300L
        const val TOAST_DURATION_MS = 2500L
        const val CARD_TAP_SCALE = 0.97f
        const val CARD_TAP_DURATION_MS = 80L
    }

    object Dimension {
        const val NAV_CAPSULE_HEIGHT = 64
        const val NAV_CAPSULE_MARGIN_BOTTOM = 16
        const val NAV_CAPSULE_PADDING_H = 20
        const val NAV_CAPSULE_PADDING_V = 10
        const val SEARCH_INPUT_HEIGHT = 52
        const val FILTER_SHEET_DRAG_THRESHOLD = 0.4f
        const val PULL_TO_REFRESH_THRESHOLD = 80
    }

    val CATEGORIES = listOf(
        "Trending", "Health", "Sports", "Finance", "Politics", "Tech"
    )

    val TRENDING_SEARCHES = listOf(
        "India", "Apple", "AI", "Meta", "Climate", "Space"
    )
}
