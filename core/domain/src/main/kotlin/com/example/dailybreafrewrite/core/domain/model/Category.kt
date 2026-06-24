package com.example.dailybreafrewrite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Category(val displayName: String) {
    TRENDING("Trending"),
    HEALTH("Health"),
    SPORTS("Sports"),
    FINANCE("Finance"),
    POLITICS("Politics"),
    TECH("Tech"),
}
