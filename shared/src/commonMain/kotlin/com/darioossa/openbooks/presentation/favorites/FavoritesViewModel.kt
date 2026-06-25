package com.darioossa.openbooks.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darioossa.openbooks.domain.ObserveFavoritesUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    val state: StateFlow<FavoritesState> =
        observeFavorites()
            .map { books ->
                if (books.isEmpty()) {
                    FavoritesState.Empty
                } else {
                    FavoritesState.Content(books)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = FavoritesState.Loading,
            )

    fun remove(book: Book) {
        viewModelScope.launch {
            toggleFavorite(book)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
