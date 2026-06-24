package com.example.dailybreafrewrite.core.data.mapper

import com.example.dailybreafrewrite.core.data.dto.ArticleDto
import com.example.dailybreafrewrite.core.data.local.ArticleEntity
import com.example.dailybreafrewrite.core.domain.model.Article

fun ArticleDto.toArticleEntity(category: String? = null): ArticleEntity {
    return ArticleEntity(
        url = url,
        title = title,
        description = description,
        content = content,
        sourceName = source.name,
        sourceId = source.id,
        author = author,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        category = category,
    )
}

fun ArticleEntity.toArticle(): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        content = content,
        sourceName = sourceName,
        sourceId = sourceId,
        author = author,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        category = category,
        isSaved = isSaved,
    )
}

fun ArticleDto.toArticle(): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        content = content,
        sourceName = source.name,
        sourceId = source.id,
        author = author,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
    )
}
