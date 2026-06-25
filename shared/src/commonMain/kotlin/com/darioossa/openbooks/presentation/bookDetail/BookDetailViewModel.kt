package com.darioossa.openbooks.presentation.bookDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darioossa.openbooks.domain.GetBookUseCase
import com.darioossa.openbooks.domain.ObserveFavoriteKeysUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val getBook: GetBookUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    observeFavoriteKeys: ObserveFavoriteKeysUseCase,
) : ViewModel() {
    private val phase = MutableStateFlow<Phase>(Phase.Loading)
    private val book = MutableStateFlow<Book?>(null)
    private var loadedKey: String? = null

    val state: StateFlow<DetailState> =
        combine(phase, book, observeFavoriteKeys()) { phase, book, favoriteKeys ->
            when (phase) {
                Phase.Loading -> DetailState.Loading
                Phase.Error -> DetailState.Error
                Phase.Loaded ->
                    book?.let { DetailState.Success(it, it.key in favoriteKeys) }
                        ?: DetailState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = DetailState.Loading,
        )

    fun onBookOpened(book: Book) {
        if (loadedKey == book.key) return
        loadedKey = book.key
        this.book.value = book
        phase.value = Phase.Loading

        viewModelScope.launch {
            runCatching { getBook(book.key) }
                .onSuccess { full ->
                    this@BookDetailViewModel.book.value = book.copy(description = full.description)
                    phase.value = Phase.Loaded
                }.onFailure {
                    loadedKey = null
                    phase.value = Phase.Error
                }
        }
    }

    fun toggleFavorite() {
        val current = book.value ?: return
        viewModelScope.launch {
            toggleFavorite.invoke(current)
        }
    }

    private enum class Phase { Loading, Loaded, Error }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
