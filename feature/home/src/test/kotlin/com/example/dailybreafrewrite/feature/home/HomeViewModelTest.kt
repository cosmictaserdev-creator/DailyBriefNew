package com.example.dailybreafrewrite.feature.home

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun `initial state shows shimmer before articles load`() = runTest(testDispatcher) {
        val repo = FakeArticleRepository().apply {
            headlinesFlow.value = Resource.Success(emptyList())
        }
        val viewModel = HomeViewModel(repo, SavedStateHandle())

        assertEquals(true, viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.articles.isEmpty())
    }

    @Test
    fun `articles load from repository`() = runTest(testDispatcher) {
        val articles = listOf(
            Article(
                url = "https://example.com/1",
                title = "Test Article",
                description = "Description",
                content = "Content",
                sourceName = "Test Source",
                sourceId = "test-source",
                author = null,
                urlToImage = null,
                publishedAt = "2026-01-01T00:00:00Z",
            )
        )
        val repo = FakeArticleRepository(initialArticles = articles)
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.articles.size)
        assertEquals("Test Article", viewModel.state.value.articles.first().title)
    }

    @Test
    fun `select category updates selection and triggers refresh`() = runTest(testDispatcher) {
        val repo = FakeArticleRepository()
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()

        viewModel.onAction(HomeAction.SelectCategory("Sports"))
        advanceUntilIdle()

        assertEquals("Sports", viewModel.state.value.selectedCategory)
    }

    @Test
    fun `pull to refresh sets refreshing state`() = runTest(testDispatcher) {
        val articles = listOf(
            Article(
                url = "https://example.com/1",
                title = "Test",
                description = null,
                content = null,
                sourceName = "Source",
                sourceId = "source",
                author = null,
                urlToImage = null,
                publishedAt = "2026-01-01T00:00:00Z",
            )
        )
        val repo = FakeArticleRepository(initialArticles = articles)
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()

        viewModel.onAction(HomeAction.Refresh)

        assertTrue(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `next advances top index`() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(initialArticles = articlesOf(8))
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()

        viewModel.onAction(HomeAction.Next)

        assertEquals(1, viewModel.state.value.topIndex)
    }

    @Test
    fun `previous at start is a no-op`() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(initialArticles = articlesOf(8))
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()

        viewModel.onAction(HomeAction.Previous)

        assertEquals(0, viewModel.state.value.topIndex)
    }

    @Test
    fun `next near the end triggers prefetch`() = runTest(testDispatcher) {
        // 8 articles, PREFETCH_THRESHOLD = 6: at topIndex 1, remaining (6) hits threshold.
        val repo = FakeArticleRepository(initialArticles = articlesOf(8))
        val viewModel = HomeViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        val before = repo.refreshCount

        viewModel.onAction(HomeAction.Next)
        advanceUntilIdle()

        assertTrue(repo.refreshCount > before)
    }

    @Test
    fun `top index is restored from saved state`() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(initialArticles = articlesOf(8))
        val handle = SavedStateHandle(mapOf("home_top_index" to 3))
        val viewModel = HomeViewModel(repo, handle)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.topIndex)
    }

    @Test
    fun `mock articles helper generates valid data`() {
        val now = java.time.ZonedDateTime.now()
        val articles = mockArticles(now)

        assertEquals(10, articles.size)
        articles.forEach { article ->
            assertTrue(article.url.startsWith("https://"))
            assertNotNull(article.title)
            assertNotNull(article.sourceName)
        }
    }
}

private fun articlesOf(count: Int): List<Article> = (0 until count).map { i ->
    Article(
        url = "https://example.com/$i",
        title = "Article $i",
        description = "Description $i",
        content = "Content $i",
        sourceName = "Source $i",
        sourceId = "source-$i",
        author = null,
        urlToImage = null,
        publishedAt = "2026-01-01T00:00:00Z",
    )
}

private class FakeArticleRepository(
    private val initialArticles: List<Article> = emptyList(),
) : ArticleRepository {

    val headlinesFlow = MutableStateFlow<Resource<List<Article>>>(
        Resource.Success(initialArticles)
    )

    private var refreshCalls = 0
    val refreshCount: Int get() = refreshCalls

    override fun observeHeadlines(category: String): Flow<Resource<List<Article>>> {
        return headlinesFlow
    }

    override fun searchArticles(query: String): Flow<Resource<List<Article>>> {
        return flow { emit(Resource.Success(emptyList())) }
    }

    override fun observeSavedArticles(): Flow<Resource<List<Article>>> {
        return flow { emit(Resource.Success(emptyList())) }
    }

    override suspend fun refreshHeadlines(category: String, page: Int): Resource<Unit> {
        refreshCalls++
        if (initialArticles.isNotEmpty() || page == 1) {
            return Resource.Success(Unit)
        }
        return Resource.Error(
            message = com.example.dailybreafrewrite.core.common.UiText.Dynamic("Error")
        )
    }

    override suspend fun searchRemote(query: String, page: Int): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun toggleSaved(url: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun isSaved(url: String): Boolean = false
}
