package com.example.dailybreafrewrite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterPreferences(
    val sortBy: SortBy = SortBy.TRENDING,
    val sources: Set<String> = emptySet(),
    val dateRange: DateRange = DateRange.TODAY,
)

@Serializable
enum class SortBy(val displayName: String, val apiValue: String) {
    LATEST("Latest First", "publishedAt"),
    TRENDING("Trending First", "relevancy"),
    MOST_SAVED("Most Saved", ""),
}

@Serializable
enum class DateRange(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
}
