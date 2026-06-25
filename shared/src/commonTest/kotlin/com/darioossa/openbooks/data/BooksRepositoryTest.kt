package com.darioossa.openbooks.data

import app.cash.turbine.test
import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.entities.Book
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BooksRepositoryTest {
    private val remote = mock<BooksRemoteSource>()
    private val local = mock<BooksLocalSource>()
    private val repository = BooksRepository(remote, local)

    @Test
    fun `searchBooks delegates to the remote source and emits its results`() =
        runTest {
            val books =
                listOf(
                    Book(
                        key = "OL45804W",
                        title = "Oliver Twist",
                        authors = listOf("Charles Dickens"),
                        coverUrl = null,
                        firstPublishYear = 1838,
                    ),
                )
            everySuspend { remote.search("dickens", page = 1) } returns books

            repository.searchBooks("dickens").test {
                awaitItem() shouldBe books
                awaitComplete()
            }
            verifySuspend { remote.search("dickens", page = 1) }
        }
}
