package com.example.dailybreafrewrite.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query(
        "SELECT * FROM articles WHERE title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%' ORDER BY publishedAt DESC"
    )
    fun searchArticles(query: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY cachedAt DESC")
    fun getSavedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE url = :url")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isSaved = :saved WHERE url = :url")
    suspend fun updateSavedStatus(url: String, saved: Boolean)

    @Query("DELETE FROM articles WHERE cachedAt < :threshold")
    suspend fun purgeOldCache(threshold: Long)

    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun deleteByCategory(category: String)
}
