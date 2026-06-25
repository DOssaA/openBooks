package com.darioossa.openbooks.domain

import com.darioossa.openbooks.OpenForTest
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

@OpenForTest
class ObserveFavoriteKeysUseCase(
    private val dataSource: BooksDataSource,
) {
    operator fun invoke(): Flow<Set<String>> =
        dataSource
            .observeFavoriteKeys()
            .flowOn(Dispatchers.IO)
}
