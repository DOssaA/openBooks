package com.darioossa.openbooks.presentation.favorites

import com.darioossa.openbooks.domain.entities.Book

sealed interface FavoritesState {
    data object Loading : FavoritesState

    data object Empty : FavoritesState

    data class Content(
        val books: List<Book>,
    ) : FavoritesState
}
