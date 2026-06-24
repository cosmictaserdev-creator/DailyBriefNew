package com.example.dailybreafrewrite.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Search : Route
    @Serializable data object Saved : Route
    @Serializable data class ArticleDetail(val url: String) : Route
    @Serializable data object Onboarding : Route
}
