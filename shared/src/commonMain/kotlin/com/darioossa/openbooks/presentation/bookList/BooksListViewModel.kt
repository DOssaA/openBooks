package com.darioossa.openbooks.presentation.bookList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darioossa.openbooks.domain.SearchBooksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BooksListViewModel(
    private val searchBooks: SearchBooksUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val state: StateFlow<ListState> =
        query
            .debounce(DEBOUNCE_MS)
            .flatMapLatest { rawQuery ->
                val trimmed = rawQuery.trim()
                if (trimmed.isEmpty()) {
                    flowOf(ListState.Empty)
                } else {
                    flow {
                        emit(ListState.Loading)
                        emitAll(
                            searchBooks(trimmed).map { books ->
                                if (books.isEmpty()) ListState.Empty else ListState.Success(books)
                            },
                        )
                    }.catch { emit(ListState.Error) }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = ListState.Empty,
            )

    fun onSearch(query: String) {
        this.query.value = query
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
