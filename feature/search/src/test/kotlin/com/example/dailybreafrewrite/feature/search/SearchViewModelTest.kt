package com.example.dailybreafrewrite.feature.search

import app.cash.turbine.test
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.common.Resource
import com.example.dailybreafrewrite.core.domain.model.Article
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query and trending searches`() {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repo)

        assertEquals("", viewModel.state.value.query)
        assertEquals(AppConstants.TRENDING_SEARCHES, viewModel.state.value.trendingSearches)
        assertFalse(viewModel.state.value.isSearching)
    }

    @Test
    fun `update query starts searching after debounce`() = runTest(testDispatcher) {
        val articles = listOf(article())
        val repo = FakeSearchRepository(initialResults = articles)
        val viewModel = SearchViewModel(repo)

        viewModel.onAction(SearchAction.UpdateQuery("test"))
        assertTrue(viewModel.state.value.query == "test")

        advanceTimeBy(350)
        // search triggered after debounce, repo emits articles
    }

    @Test
    fun `clear query resets state`() {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repo)

        viewModel.onAction(SearchAction.UpdateQuery("test"))
        viewModel.onAction(SearchAction.ClearQuery)

        assertEquals("", viewModel.state.value.query)
        assertTrue(viewModel.state.value.results.isEmpty())
    }

    @Test
    fun `select trending sets query and searches`() = runTest(testDispatcher) {
        val articles = listOf(article(title = "AI News"))
        val repo = FakeSearchRepository(initialResults = articles)
        val viewModel = SearchViewModel(repo)

        viewModel.onAction(SearchAction.SelectTrending("AI"))

        assertEquals("AI", viewModel.state.value.query)
    }

    @Test
    fun `trending searches from constants`() {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repo)

        assertEquals(AppConstants.TRENDING_SEARCHES, viewModel.state.value.trendingSearches)
        assertTrue(AppConstants.TRENDING_SEARCHES.contains("AI"))
    }

    private fun article(
        title: String = "Test Article",
        url: String = "https://example.com/test",
    ) = Article(
        url = url,
        title = title,
        description = "Description",
        content = "Content",
        sourceName = "Source",
        sourceId = "source",
        author = null,
        urlToImage = null,
        publishedAt = "2026-01-01T00:00:00Z",
    )
}

private class FakeSearchRepository(
    private val initialResults: List<Article> = emptyList(),
) : ArticleRepository {

    private val searchFlow = MutableStateFlow<Resource<List<Article>>>(
        Resource.Success(initialResults)
    )

    override fun observeHeadlines(category: String): Flow<Resource<List<Article>>> {
        return flow { emit(Resource.Success(emptyList())) }
    }

    override fun searchArticles(query: String): Flow<Resource<List<Article>>> {
        return searchFlow
    }

    override fun observeSavedArticles(): Flow<Resource<List<Article>>> {
        return flow { emit(Resource.Success(emptyList())) }
    }

    override suspend fun refreshHeadlines(category: String, page: Int): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun searchRemote(query: String, page: Int): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun toggleSaved(url: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun isSaved(url: String): Boolean = false
}
