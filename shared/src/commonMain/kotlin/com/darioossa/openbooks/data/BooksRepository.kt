package com.darioossa.openbooks.data

import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow

// Sources wired here for the Ktor + Room implementation in Issue 5.
@Suppress("UnusedPrivateProperty")
class BooksRepository(
    private val remoteSource: BooksRemoteSource,
    private val localSource: BooksLocalSource,
) : BooksDataSource {
    override suspend fun searchBooks(query: String): Flow<List<Book>> {
        TODO("Not yet implemented")
    }
}
