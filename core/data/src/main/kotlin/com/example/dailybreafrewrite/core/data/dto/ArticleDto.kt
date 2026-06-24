package com.example.dailybreafrewrite.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    val source: SourceDto,
    val author: String? = null,
    val title: String,
    val description: String? = null,
    val url: String,
    val urlToImage: String? = null,
    val publishedAt: String,
    val content: String? = null,
)

@Serializable
data class SourceDto(
    val id: String? = null,
    val name: String,
)
