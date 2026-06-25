package com.darioossa.openbooks.domain

import com.darioossa.openbooks.OpenForTest
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

@OpenForTest
class ObserveFavoritesUseCase(
    private val dataSource: BooksDataSource,
) {
    operator fun invoke(): Flow<List<Book>> =
        dataSource
            .observeFavorites()
            .flowOn(Dispatchers.IO)
}
