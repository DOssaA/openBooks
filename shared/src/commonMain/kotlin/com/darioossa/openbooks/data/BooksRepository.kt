package com.darioossa.openbooks.data

import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Issue 5 delegates search to the remote source. Merging remote results with the local favorite
// state is deferred until Room lands (Issue 3, in parallel); localSource stays injected for then.
@Suppress("UnusedPrivateProperty")
class BooksRepository(
    private val remoteSource: BooksRemoteSource,
    private val localSource: BooksLocalSource,
) : BooksDataSource {
    override suspend fun searchBooks(query: String): Flow<List<Book>> = flow { emit(remoteSource.search(query, page = FIRST_PAGE)) }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
