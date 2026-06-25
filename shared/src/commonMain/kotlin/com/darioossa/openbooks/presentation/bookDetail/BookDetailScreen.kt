package com.darioossa.openbooks.presentation.bookDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.darioossa.openbooks.domain.entities.Book
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookDetailScreen(
    book: Book,
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(book.key) { viewModel.onBookOpened(book) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        DetailState.Loading -> CenteredBox(modifier) { CircularProgressIndicator() }
        DetailState.Error ->
            CenteredBox(modifier) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Couldn't load this book.", style = MaterialTheme.typography.bodyLarge)
                    FilledTonalButton(onClick = { viewModel.onBookOpened(book) }) { Text("Retry") }
                }
            }
        is DetailState.Success ->
            DetailContent(
                book = current.book,
                isFavorite = current.isFavorite,
                onToggleFavorite = viewModel::toggleFavorite,
                modifier = modifier,
            )
    }
}

@Composable
private fun DetailContent(
    book: Book,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Cover(coverUrl = book.coverUrl, title = book.title)

        Text(text = book.title, style = MaterialTheme.typography.headlineSmall)

        if (book.authors.isNotEmpty()) {
            Text(
                text = book.authors.joinToString(),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        book.firstPublishYear?.let { year ->
            Text(
                text = "First published $year",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
            Text(if (isFavorite) "♥ In favorites" else "♡ Add to favorites")
        }

        Text(
            text = book.description?.takeIf { it.isNotBlank() } ?: "No description available.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Cover(
    coverUrl: String?,
    title: String,
) {
    val shape = RoundedCornerShape(12.dp)
    if (coverUrl != null) {
        AsyncImage(
            model = coverUrl,
            contentDescription = "Cover of $title",
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .size(width = 160.dp, height = 240.dp)
                    .clip(shape),
        )
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier =
                Modifier
                    .size(width = 160.dp, height = 240.dp)
                    .aspectRatio(2f / 3f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
