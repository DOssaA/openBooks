package com.darioossa.openbooks.data

import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.SearchBooksPage
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BooksRepository(
    private val remoteSource: BooksRemoteSource,
    private val localSource: BooksLocalSource,
) : BooksDataSource {
    override suspend fun searchBooks(
        query: String,
        page: Int,
    ): Flow<SearchBooksPage> = flow { emit(remoteSource.search(query, page)) }

    override suspend fun getBook(key: String): Book = remoteSource.getBook(key)

    override fun observeFavorites(): Flow<List<Book>> = localSource.observeFavorites()

    override fun observeFavoriteKeys(): Flow<Set<String>> = localSource.observeFavoriteKeys()

    override suspend fun toggleFavorite(book: Book) = localSource.toggleFavorite(book)
}
