package com.darioossa.openbooks.presentation.bookDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.darioossa.openbooks.domain.entities.Book
import openbooks.shared.generated.resources.Res
import openbooks.shared.generated.resources.detail_cover_cd
import openbooks.shared.generated.resources.detail_error
import openbooks.shared.generated.resources.detail_favorite_add
import openbooks.shared.generated.resources.detail_favorite_added
import openbooks.shared.generated.resources.detail_first_published
import openbooks.shared.generated.resources.detail_no_description
import openbooks.shared.generated.resources.detail_retry
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding: Dp = 16.dp
private val SectionGap: Dp = 12.dp
private val CoverWidth: Dp = 160.dp
private val CoverHeight: Dp = 240.dp
private val CoverCorner: Dp = 12.dp
private val PlaceholderPadding: Dp = 8.dp

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
                    Text(
                        text = stringResource(Res.string.detail_error),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    FilledTonalButton(onClick = { viewModel.onBookOpened(book) }) {
                        Text(stringResource(Res.string.detail_retry))
                    }
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
                .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
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
                text = stringResource(Res.string.detail_first_published, year),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
            val label = if (isFavorite) Res.string.detail_favorite_added else Res.string.detail_favorite_add
            Text(stringResource(label))
        }

        val description =
            book.description?.takeIf { it.isNotBlank() }
                ?: stringResource(Res.string.detail_no_description)
        Text(text = description, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Cover(
    coverUrl: String?,
    title: String,
) {
    val shape = RoundedCornerShape(CoverCorner)
    val coverModifier = Modifier.size(width = CoverWidth, height = CoverHeight).clip(shape)
    if (coverUrl != null) {
        AsyncImage(
            model = coverUrl,
            contentDescription = stringResource(Res.string.detail_cover_cd, title),
            contentScale = ContentScale.Fit,
            modifier = coverModifier,
        )
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = coverModifier,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(PlaceholderPadding),
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
