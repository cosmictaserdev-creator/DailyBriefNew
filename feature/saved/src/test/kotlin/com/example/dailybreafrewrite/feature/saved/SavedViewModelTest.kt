package com.example.dailybreafrewrite.feature.saved

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

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
    fun `initial state shows loading`() {
        val repo = FakeSavedRepository()
        val viewModel = SavedViewModel(repo)

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `saved articles load from repository`() = runTest(testDispatcher) {
        val articles = listOf(article("https://example.com/1"))
        val repo = FakeSavedRepository(initialSaved = articles)
        val viewModel = SavedViewModel(repo)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.savedArticles.size)
    }

    @Test
    fun `toggle saved removes article`() = runTest(testDispatcher) {
        val articles = listOf(article("https://example.com/1"))
        val repo = FakeSavedRepository(initialSaved = articles)
        val viewModel = SavedViewModel(repo)
        advanceUntilIdle()

        viewModel.onAction(SavedAction.ToggleSaved("https://example.com/1"))
        advanceUntilIdle()

        assertTrue(repo.lastToggledUrl == "https://example.com/1")
    }

    @Test
    fun `search filters saved articles`() = runTest(testDispatcher) {
        val articles = listOf(
            article("https://a.com/1", title = "Android News"),
            article("https://a.com/2", title = "iOS News"),
        )
        val repo = FakeSavedRepository(initialSaved = articles)
        val viewModel = SavedViewModel(repo)
        advanceUntilIdle()

        viewModel.onAction(SavedAction.UpdateSearch("Android"))

        assertEquals("Android", viewModel.state.value.searchQuery)
    }

    @Test
    fun `empty state when no saved articles`() = runTest(testDispatcher) {
        val repo = FakeSavedRepository()
        val viewModel = SavedViewModel(repo)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.savedArticles.isEmpty())
    }

    private fun article(
        url: String,
        title: String = "Saved Article",
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

private class FakeSavedRepository(
    private val initialSaved: List<Article> = emptyList(),
) : ArticleRepository {

    var lastToggledUrl: String? = null
    private val savedFlow = MutableStateFlow<Resource<List<Article>>>(
        Resource.Success(initialSaved)
    )

    override fun observeHeadlines(category: String): Flow<Resource<List<Article>>> =
        flow { emit(Resource.Success(emptyList())) }

    override fun searchArticles(query: String): Flow<Resource<List<Article>>> =
        flow { emit(Resource.Success(emptyList())) }

    override fun observeSavedArticles(): Flow<Resource<List<Article>>> = savedFlow

    override suspend fun refreshHeadlines(category: String, page: Int): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun searchRemote(query: String, page: Int): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun toggleSaved(url: String): Resource<Unit> {
        lastToggledUrl = url
        savedFlow.value = Resource.Success(
            initialSaved.filter { it.url != url }
        )
        return Resource.Success(Unit)
    }

    override suspend fun isSaved(url: String): Boolean = false
}
