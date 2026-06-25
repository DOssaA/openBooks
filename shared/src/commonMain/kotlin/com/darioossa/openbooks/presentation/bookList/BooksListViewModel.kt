package com.darioossa.openbooks.presentation.bookList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darioossa.openbooks.domain.ObserveFavoriteKeysUseCase
import com.darioossa.openbooks.domain.SearchBooksUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.entities.Book
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BooksListViewModel(
    private val searchBooks: SearchBooksUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    observeFavoriteKeys: ObserveFavoriteKeysUseCase,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<ListState>(ListState.Idle)
    val state: StateFlow<ListState> = _state.asStateFlow()

    val favoriteKeys: StateFlow<Set<String>> =
        observeFavoriteKeys()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = emptySet(),
            )

    private var activeQuery = ""
    private var currentPage = 0

    init {
        viewModelScope.launch {
            _query
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { rawQuery ->
                    val trimmed = rawQuery.trim()
                    activeQuery = trimmed
                    currentPage = 0
                    if (trimmed.isEmpty()) {
                        _state.value = ListState.Idle
                    } else {
                        loadFirstPage(trimmed)
                    }
                }
        }
    }

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun onSearch(query: String) {
        onQueryChanged(query)
    }

    fun loadNextPage() {
        val currentState = _state.value as? ListState.Success ?: return
        if (currentState.loadingMore || currentState.endReached || activeQuery.isEmpty()) return

        viewModelScope.launch {
            _state.value = currentState.copy(loadingMore = true)
            runCatching {
                searchBooks(activeQuery, page = currentPage + 1).first()
            }.onSuccess { nextPage ->
                currentPage += 1
                _state.value =
                    currentState.copy(
                        books = currentState.books + nextPage.books,
                        loadingMore = false,
                        endReached = nextPage.endReached || nextPage.books.isEmpty(),
                    )
            }.onFailure {
                _state.value = ListState.Error
            }
        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            toggleFavorite.invoke(book)
        }
    }

    private suspend fun loadFirstPage(query: String) {
        _state.value = ListState.Loading
        runCatching {
            searchBooks(query, page = FIRST_PAGE).first()
        }.onSuccess { page ->
            currentPage = FIRST_PAGE
            _state.value =
                if (page.books.isEmpty()) {
                    ListState.Empty
                } else {
                    ListState.Success(
                        books = page.books,
                        loadingMore = false,
                        endReached = page.endReached,
                    )
                }
        }.onFailure {
            _state.value = ListState.Error
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val DEBOUNCE_MS = 300L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
