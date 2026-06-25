package com.darioossa.openbooks.presentation

import app.cash.turbine.test
import com.darioossa.openbooks.domain.ObserveFavoriteKeysUseCase
import com.darioossa.openbooks.domain.SearchBooksPage
import com.darioossa.openbooks.domain.SearchBooksUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import com.darioossa.openbooks.presentation.bookList.BooksListViewModel
import com.darioossa.openbooks.presentation.bookList.ListState
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BooksListViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: BooksListViewModel
    private val searchBooksUseCase = mock<SearchBooksUseCase>()
    private val toggleFavoriteUseCase = mock<ToggleFavoriteUseCase>()
    private val observeFavoriteKeysUseCase = mock<ObserveFavoriteKeysUseCase>()

    @Before
    fun init() {
        every { observeFavoriteKeysUseCase.invoke() } returns flowOf(emptySet())
        viewModel =
            BooksListViewModel(
                searchBooks = searchBooksUseCase,
                toggleFavorite = toggleFavoriteUseCase,
                observeFavoriteKeys = observeFavoriteKeysUseCase,
            )
    }

    @Test
    fun `blank query starts in Idle without hitting search`() =
        runTest(dispatcher) {
            viewModel.state.test {
                awaitItem() shouldBe ListState.Idle
                viewModel.onQueryChanged("   ")
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `non-empty query emits Loading then Success`() =
        runTest(dispatcher) {
            val query = "dickens"
            val books = listOf(oliverTwist)
            everySuspend { searchBooksUseCase.invoke(query, page = 1) } calls {
                flowOf(SearchBooksPage(books = books, endReached = false))
            }

            viewModel.state.test {
                awaitItem() shouldBe ListState.Idle
                viewModel.onQueryChanged(query)
                awaitItem() shouldBe ListState.Loading
                awaitItem() shouldBe
                    ListState.Success(
                        books = books,
                        loadingMore = false,
                        endReached = false,
                    )
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query, page = 1) }
        }

    @Test
    fun `query with zero results emits Loading then Empty`() =
        runTest(dispatcher) {
            val query = "no matches"
            everySuspend { searchBooksUseCase.invoke(query, page = 1) } calls {
                flowOf(SearchBooksPage(books = emptyList(), endReached = true))
            }

            viewModel.state.test {
                awaitItem() shouldBe ListState.Idle
                viewModel.onQueryChanged(query)
                awaitItem() shouldBe ListState.Loading
                awaitItem() shouldBe ListState.Empty
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query, page = 1) }
        }

    @Test
    fun `query failure emits Loading then Error`() =
        runTest(dispatcher) {
            val query = "network down"
            everySuspend { searchBooksUseCase.invoke(query, page = 1) } throws RuntimeException()

            viewModel.state.test {
                awaitItem() shouldBe ListState.Idle
                viewModel.onQueryChanged(query)
                awaitItem() shouldBe ListState.Loading
                awaitItem() shouldBe ListState.Error
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query, page = 1) }
        }

    @Test
    fun `load next page marks current list as loadingMore then appends results`() =
        runTest(dispatcher) {
            val query = "dickens"
            everySuspend { searchBooksUseCase.invoke(query, page = 1) } calls {
                flowOf(SearchBooksPage(books = listOf(oliverTwist), endReached = false))
            }
            everySuspend { searchBooksUseCase.invoke(query, page = 2) } calls {
                flowOf(SearchBooksPage(books = listOf(christmasCarol), endReached = true))
            }

            viewModel.state.test {
                awaitItem() shouldBe ListState.Idle
                viewModel.onQueryChanged(query)
                awaitItem() shouldBe ListState.Loading
                awaitItem() shouldBe
                    ListState.Success(
                        books = listOf(oliverTwist),
                        loadingMore = false,
                        endReached = false,
                    )

                viewModel.loadNextPage()

                awaitItem() shouldBe
                    ListState.Success(
                        books = listOf(oliverTwist),
                        loadingMore = true,
                        endReached = false,
                    )
                awaitItem() shouldBe
                    ListState.Success(
                        books = listOf(oliverTwist, christmasCarol),
                        loadingMore = false,
                        endReached = true,
                    )
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query, page = 1) }
            verifySuspend { searchBooksUseCase.invoke(query, page = 2) }
        }

    @Test
    fun `toggle favorite delegates the selected book`() =
        runTest(dispatcher) {
            everySuspend { toggleFavoriteUseCase.invoke(oliverTwist) } returns Unit

            viewModel.toggleFavorite(oliverTwist)
            advanceUntilIdle()

            verifySuspend { toggleFavoriteUseCase.invoke(oliverTwist) }
        }

    private companion object {
        val oliverTwist =
            Book(
                key = "OL45804W",
                title = "Oliver Twist",
                authors = listOf("Charles Dickens"),
                coverUrl = null,
                firstPublishYear = 1838,
            )
        val christmasCarol =
            Book(
                key = "OL27448W",
                title = "A Christmas Carol",
                authors = listOf("Charles Dickens"),
                coverUrl = null,
                firstPublishYear = 1843,
            )
    }
}
