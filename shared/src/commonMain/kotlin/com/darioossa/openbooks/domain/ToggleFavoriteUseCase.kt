package com.darioossa.openbooks.domain

import com.darioossa.openbooks.OpenForTest
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OpenForTest
class ToggleFavoriteUseCase(
    private val dataSource: BooksDataSource,
) {
    suspend operator fun invoke(book: Book) {
        withContext(Dispatchers.IO) {
            dataSource.toggleFavorite(book)
        }
    }
}
