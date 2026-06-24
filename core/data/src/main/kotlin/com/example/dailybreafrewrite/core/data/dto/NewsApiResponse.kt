package com.example.dailybreafrewrite.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>,
)
