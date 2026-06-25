package com.darioossa.openbooks.data.local

import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BooksLocalSource {
    fun observeFavoriteKeys(): Flow<Set<String>>

    suspend fun toggleFavorite(book: Book)
}

class BooksLocal(
    private val favoritesDao: FavoritesDao,
) : BooksLocalSource {
    override fun observeFavoriteKeys(): Flow<Set<String>> =
        favoritesDao
            .observeFavorites()
            .map { favorites -> favorites.mapTo(mutableSetOf()) { it.workKey } }

    override suspend fun toggleFavorite(book: Book) {
        if (favoritesDao.isFavorite(book.key)) {
            favoritesDao.delete(book.key)
        } else {
            favoritesDao.upsert(book.toFavorite())
        }
    }

    private fun Book.toFavorite(): Favorite =
        Favorite(
            workKey = key,
            title = title,
            author = authors.joinToString(),
            coverUrl = coverUrl,
        )
}
