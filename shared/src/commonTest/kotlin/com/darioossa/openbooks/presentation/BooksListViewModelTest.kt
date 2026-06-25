package com.darioossa.openbooks.presentation

import app.cash.turbine.test
import com.darioossa.openbooks.domain.SearchBooksUseCase
import com.darioossa.openbooks.domain.entities.Book
import com.darioossa.openbooks.presentation.bookList.BooksListViewModel
import com.darioossa.openbooks.presentation.bookList.ListState
import dev.mokkery.answering.calls
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class BooksListViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: BooksListViewModel
    private val searchBooksUseCase = mock<SearchBooksUseCase>()

    @Before
    fun init() {
        viewModel = BooksListViewModel(searchBooksUseCase)
    }

    @Test
    fun `non-empty query emits Loading then Success`() =
        runTest(dispatcher) {
            val list =
                listOf(
                    Book(
                        key = "OL45804W",
                        title = "Oliver Twist",
                        authors = listOf("Charles Dickens"),
                        coverUrl = null,
                        firstPublishYear = 1838,
                    ),
                    Book(
                        key = "OL27448W",
                        title = "A Christmas Carol",
                        authors = listOf("Charles Dickens"),
                        coverUrl = null,
                        firstPublishYear = 1843,
                    ),
                )
            val query = "Charles D"
            everySuspend { searchBooksUseCase.invoke(query) } calls { flowOf(list) }

            viewModel.state.test {
                assertSame(ListState.Empty, awaitItem())
                viewModel.onSearch(query)
                assertSame(ListState.Loading, awaitItem())
                val result = awaitItem()
                assertIs<ListState.Success>(result)
                assertSame(list, result.booksList)
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query) }
        }

    @Test
    fun `query failure emits Loading then Error`() =
        runTest(dispatcher) {
            val query = "C"
            everySuspend { searchBooksUseCase.invoke(query) } throws RuntimeException()

            viewModel.state.test {
                assertSame(ListState.Empty, awaitItem())
                viewModel.onSearch(query)
                assertSame(ListState.Loading, awaitItem())
                assertSame(ListState.Error, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query) }
        }

    @Test
    fun `blank query stays Empty without hitting the use case`() =
        runTest(dispatcher) {
            viewModel.state.test {
                assertSame(ListState.Empty, awaitItem())
                viewModel.onSearch("   ")
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `query with zero results emits Loading then Empty`() =
        runTest(dispatcher) {
            val query = "no matches"
            everySuspend { searchBooksUseCase.invoke(query) } calls { flowOf(emptyList<Book>()) }

            viewModel.state.test {
                assertSame(ListState.Empty, awaitItem())
                viewModel.onSearch(query)
                assertSame(ListState.Loading, awaitItem())
                assertSame(ListState.Empty, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            verifySuspend { searchBooksUseCase.invoke(query) }
        }
}
