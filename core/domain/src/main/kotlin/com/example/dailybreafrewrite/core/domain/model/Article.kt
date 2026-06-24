package com.example.dailybreafrewrite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val content: String?,
    val sourceName: String,
    val sourceId: String?,
    val author: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val category: String? = null,
    val isSaved: Boolean = false,
)
