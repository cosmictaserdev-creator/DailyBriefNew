package com.example.dailybreafrewrite.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val content: String?,
    val sourceName: String,
    val sourceId: String?,
    val author: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val category: String?,
    val isSaved: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis(),
)
