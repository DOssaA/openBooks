package com.darioossa.openbooks.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darioossa.openbooks.domain.entities.Book
import openbooks.shared.generated.resources.Res
import openbooks.shared.generated.resources.favorites_empty
import openbooks.shared.generated.resources.favorites_remove
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavoritesContent(
        state = state,
        onRemove = viewModel::remove,
    )
}

@Composable
internal fun FavoritesContent(
    state: FavoritesState,
    onRemove: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        FavoritesState.Loading ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        FavoritesState.Empty ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.favorites_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

        is FavoritesState.Content ->
            LazyColumn(modifier.fillMaxSize()) {
                items(state.books, key = { it.key }) { book ->
                    FavoriteRow(book = book, onRemove = { onRemove(book) })
                    HorizontalDivider()
                }
            }
    }
}

@Composable
private fun FavoriteRow(
    book: Book,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        TextButton(onClick = onRemove) {
            Text(stringResource(Res.string.favorites_remove))
        }
    }
}
