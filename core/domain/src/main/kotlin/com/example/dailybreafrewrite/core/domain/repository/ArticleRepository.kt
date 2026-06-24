package com.example.dailybreafrewrite.core.domain.repository

import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun observeHeadlines(category: String): Flow<Resource<List<Article>>>

    fun searchArticles(query: String): Flow<Resource<List<Article>>>

    fun observeSavedArticles(): Flow<Resource<List<Article>>>

    suspend fun refreshHeadlines(category: String, page: Int = 1): Resource<Unit>

    suspend fun searchRemote(query: String, page: Int = 1): Resource<Unit>

    suspend fun toggleSaved(url: String): Resource<Unit>

    suspend fun isSaved(url: String): Boolean
}
