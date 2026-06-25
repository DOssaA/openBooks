@file:Suppress("TooManyFunctions", "LongParameterList")

package com.darioossa.openbooks.presentation.bookList

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.darioossa.openbooks.domain.entities.Book
import openbooks.shared.generated.resources.Res
import openbooks.shared.generated.resources.add_to_favorites
import openbooks.shared.generated.resources.app_name
import openbooks.shared.generated.resources.clear_search_desc
import openbooks.shared.generated.resources.discover_books_desc
import openbooks.shared.generated.resources.discover_books_title
import openbooks.shared.generated.resources.end_of_list
import openbooks.shared.generated.resources.error_desc
import openbooks.shared.generated.resources.error_title
import openbooks.shared.generated.resources.favorites
import openbooks.shared.generated.resources.no_cover_desc
import openbooks.shared.generated.resources.no_results_desc
import openbooks.shared.generated.resources.no_results_title
import openbooks.shared.generated.resources.published_year
import openbooks.shared.generated.resources.remove_from_favorites
import openbooks.shared.generated.resources.retry
import openbooks.shared.generated.resources.search_icon_desc
import openbooks.shared.generated.resources.search_placeholder
import openbooks.shared.generated.resources.unknown_author
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private object Dimens {
    val paddingSmall = 8.dp
    val paddingMedium = 12.dp
    val paddingLarge = 16.dp
    val paddingExtraLarge = 24.dp
    val paddingDoubleExtraLarge = 32.dp

    val coverWidth = 70.dp
    val coverHeight = 100.dp
    val iconSizeLarge = 64.dp
    val iconSizeExtraLarge = 120.dp

    val cardElevation = 2.dp

    val cornerSmall = 4.dp
    val cornerMedium = 8.dp
    val cornerLarge = 16.dp
    val cornerExtraLarge = 24.dp
}

private const val BORDER_ALPHA_OUTLINE = 0.5f
private const val CARD_BACKGROUND_ALPHA = 0.3f
private const val IDLE_PULSE_ALPHA = 0.6f
private const val LOADING_SKELETON_COUNT = 8
private const val PAGINATION_THRESHOLD = 3

private const val SHIMMER_TRANSLATE_START = 0f
private const val SHIMMER_TRANSLATE_END = 1000f
private const val SHIMMER_DURATION_MILLIS = 1200
private const val SHIMMER_COLOR_ALPHA = 0.4f

private const val SHIMMER_TITLE_WIDTH_FRACTION = 0.7f
private const val SHIMMER_AUTHOR_WIDTH_FRACTION = 0.5f
private const val SHIMMER_YEAR_WIDTH_FRACTION = 0.3f
private val SHIMMER_TITLE_HEIGHT = 20.dp
private val SHIMMER_AUTHOR_HEIGHT = 16.dp
private val SHIMMER_YEAR_HEIGHT = 12.dp

@Composable
fun BookListScreen(
    viewModel: BooksListViewModel = koinViewModel(),
    onBookClick: (String) -> Unit = {},
    onFavoritesClick: () -> Unit = {},
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favoriteKeys by viewModel.favoriteKeys.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BookListTopBar(onFavoritesClick = onFavoritesClick)
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            BookListSearchBar(
                query = query,
                onQueryChanged = viewModel::onQueryChanged,
            )

            Box(
                modifier = Modifier.weight(1f),
            ) {
                BookListStateContent(
                    state = state,
                    query = query,
                    favoriteKeys = favoriteKeys,
                    onBookClick = onBookClick,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onSearch = viewModel::onSearch,
                    onLoadNextPage = viewModel::loadNextPage,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookListTopBar(onFavoritesClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(onClick = onFavoritesClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(Res.string.favorites),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
    )
}

@Composable
private fun BookListSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.paddingSmall),
        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.search_icon_desc),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.clear_search_desc),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(Dimens.cornerExtraLarge),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = BORDER_ALPHA_OUTLINE),
            ),
    )
}

@Composable
private fun BookListStateContent(
    state: ListState,
    query: String,
    favoriteKeys: Set<String>,
    onBookClick: (String) -> Unit,
    onToggleFavorite: (Book) -> Unit,
    onSearch: (String) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    when (state) {
        is ListState.Idle -> {
            IdleStateView()
        }
        is ListState.Loading -> {
            BookListLoadingView()
        }
        is ListState.Empty -> {
            EmptyStateView(query = query)
        }
        is ListState.Error -> {
            ErrorStateView(onRetry = { onSearch(query) })
        }
        is ListState.Success -> {
            BookListSuccessContent(
                state = state,
                favoriteKeys = favoriteKeys,
                onBookClick = onBookClick,
                onToggleFavorite = onToggleFavorite,
                onLoadNextPage = onLoadNextPage,
            )
        }
    }
}

@Composable
private fun BookListLoadingView() {
    val shimmerBrush = rememberShimmerBrush()
    LazyColumn {
        items(LOADING_SKELETON_COUNT) {
            ShimmerBookItem(brush = shimmerBrush)
        }
    }
}

@Composable
private fun BookListSuccessContent(
    state: ListState.Success,
    favoriteKeys: Set<String>,
    onBookClick: (String) -> Unit,
    onToggleFavorite: (Book) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf false
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            lastVisibleItem.index >= totalItems - PAGINATION_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadNextPage()
        }
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Dimens.paddingLarge),
    ) {
        items(
            items = state.books,
            key = { it.key },
        ) { book ->
            val isFavorite = favoriteKeys.contains(book.key)
            BookItem(
                book = book,
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavorite(book) },
                onClick = { onBookClick(book.key) },
                modifier =
                    Modifier.padding(
                        horizontal = Dimens.paddingLarge,
                        vertical = Dimens.paddingSmall,
                    ),
            )
        }

        if (state.loadingMore) {
            item {
                val shimmerBrush = rememberShimmerBrush()
                ShimmerBookItem(brush = shimmerBrush)
            }
        } else if (state.endReached && state.books.isNotEmpty()) {
            item {
                EndOfListFooter()
            }
        }
    }
}

@Composable
private fun EndOfListFooter() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.end_of_list),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
fun BookItem(
    book: Book,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_BACKGROUND_ALPHA),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(Dimens.paddingMedium)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCoverImage(
                coverUrl = book.coverUrl,
                title = book.title,
            )

            Spacer(modifier = Modifier.width(Dimens.paddingLarge))

            BookDetailsColumn(
                title = book.title,
                authors = book.authors,
                firstPublishYear = book.firstPublishYear,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription =
                        stringResource(
                            if (isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites,
                        ),
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun BookCoverImage(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(Dimens.cornerMedium),
        modifier = modifier.size(width = Dimens.coverWidth, height = Dimens.coverHeight),
    ) {
        if (!coverUrl.isNullOrEmpty()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = stringResource(Res.string.no_cover_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        ),
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun BookDetailsColumn(
    title: String,
    authors: List<String>,
    firstPublishYear: Int?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Dimens.cornerSmall))
        Text(
            text = authors.joinToString(", ").ifEmpty { stringResource(Res.string.unknown_author) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (firstPublishYear != null) {
            Spacer(modifier = Modifier.height(Dimens.cornerSmall))
            Text(
                text = stringResource(Res.string.published_year, firstPublishYear),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun ShimmerBookItem(brush: Brush) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.paddingSmall),
        shape = RoundedCornerShape(Dimens.cornerLarge),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CARD_BACKGROUND_ALPHA),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(Dimens.paddingMedium)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(width = Dimens.coverWidth, height = Dimens.coverHeight)
                        .clip(RoundedCornerShape(Dimens.cornerMedium))
                        .background(brush),
            )
            Spacer(modifier = Modifier.width(Dimens.paddingLarge))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                ShimmerLine(brush = brush, fraction = SHIMMER_TITLE_WIDTH_FRACTION, height = SHIMMER_TITLE_HEIGHT)
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                ShimmerLine(brush = brush, fraction = SHIMMER_AUTHOR_WIDTH_FRACTION, height = SHIMMER_AUTHOR_HEIGHT)
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                ShimmerLine(brush = brush, fraction = SHIMMER_YEAR_WIDTH_FRACTION, height = SHIMMER_YEAR_HEIGHT)
            }
        }
    }
}

@Composable
private fun ShimmerLine(
    brush: Brush,
    fraction: Float,
    height: Dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(fraction)
                .height(height)
                .clip(RoundedCornerShape(Dimens.cornerSmall))
                .background(brush),
    )
}

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = SHIMMER_TRANSLATE_START,
        targetValue = SHIMMER_TRANSLATE_END,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmer_translate",
    )

    val shimmerColors =
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SHIMMER_COLOR_ALPHA),
            MaterialTheme.colorScheme.surfaceVariant,
        )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim),
    )
}

@Composable
fun IdleStateView(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Dimens.paddingDoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(Dimens.iconSizeExtraLarge)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = IDLE_PULSE_ALPHA),
                                        Color.Transparent,
                                    ),
                            ),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeLarge),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))
        Text(
            text = stringResource(Res.string.discover_books_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.discover_books_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun EmptyStateView(
    query: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Dimens.paddingDoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeLarge),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))
        Text(
            text = stringResource(Res.string.no_results_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.no_results_desc, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ErrorStateView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Dimens.paddingDoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeLarge),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))
        Text(
            text = stringResource(Res.string.error_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Text(
            text = stringResource(Res.string.error_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(Res.string.retry))
        }
    }
}
