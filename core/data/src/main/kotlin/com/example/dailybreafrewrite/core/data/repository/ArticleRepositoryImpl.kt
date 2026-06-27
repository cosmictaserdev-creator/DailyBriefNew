package com.example.dailybreafrewrite.core.data.repository

import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.common.UiText
import com.example.dailybreafrewrite.core.data.local.ArticleDao
import com.example.dailybreafrewrite.core.data.mapper.toArticle
import com.example.dailybreafrewrite.core.data.mapper.toArticleEntity
import com.example.dailybreafrewrite.core.data.remote.NewsApi
import com.example.dailybreafrewrite.core.domain.model.Article
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val dao: ArticleDao,
    private val api: NewsApi,
) : ArticleRepository {

    override fun observeHeadlines(category: String): Flow<Resource<List<Article>>> {
        return dao.getArticlesByCategory(category).map { entities ->
            Resource.Success(entities.map { it.toArticle() })
        }
    }

    override fun searchArticles(query: String): Flow<Resource<List<Article>>> {
        return dao.searchArticles(query).map { entities ->
            Resource.Success(entities.map { it.toArticle() })
        }
    }

    override fun observeSavedArticles(): Flow<Resource<List<Article>>> {
        return dao.getSavedArticles().map { entities ->
            Resource.Success(entities.map { it.toArticle() })
        }
    }

    override suspend fun refreshHeadlines(category: String, page: Int): Resource<Unit> {
        return try {
            Timber.d("refreshHeadlines: calling API for category=$category page=$page")
            val apiCategory = if (category == "Trending") null else category
            val response = api.getTopHeadlines(category = apiCategory, page = page)
            Timber.d("refreshHeadlines: status=${response.status} totalResults=${response.totalResults} articles=${response.articles.size}")
            val entities = response.articles.map { it.toArticleEntity(category) }
            Timber.d("refreshHeadlines: upserting ${entities.size} entities")
            dao.upsertAll(entities)
            Timber.d("refreshHeadlines: success")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "refreshHeadlines: failed")
            Resource.Error(
                message = UiText.Dynamic("Couldn't refresh news. Showing cached articles.")
            )
        }
    }

    override suspend fun searchRemote(query: String, page: Int): Resource<Unit> {
        return try {
            val response = api.searchNews(query = query, page = page)
            val entities = response.articles.map { it.toArticleEntity() }
            dao.upsertAll(entities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = UiText.Dynamic("Couldn't search. Showing cached results.")
            )
        }
    }

    override suspend fun toggleSaved(url: String): Resource<Unit> {
        return try {
            val article = dao.getArticleByUrl(url)
            if (article != null) {
                dao.updateSavedStatus(url, !article.isSaved)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = UiText.Dynamic("Couldn't update bookmark.")
            )
        }
    }

    override suspend fun isSaved(url: String): Boolean {
        return dao.getArticleByUrl(url)?.isSaved ?: false
    }
}
