package com.darioossa.openbooks.data.remote

import com.darioossa.openbooks.data.remote.dto.SearchDocDto
import com.darioossa.openbooks.data.remote.dto.WorkDto
import com.darioossa.openbooks.domain.entities.Book

internal const val WORKS_PREFIX = "/works/"

internal fun coverUrl(coverId: Int?): String? = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }

/** Strips the leading `/works/` so the value matches the Work key used for navigation and favorites. */
internal fun String.toWorkKey(): String = removePrefix(WORKS_PREFIX)

fun SearchDocDto.toBook(): Book =
    Book(
        key = key.toWorkKey(),
        title = title,
        authors = authorName.orEmpty(),
        coverUrl = coverUrl(coverId),
        firstPublishYear = firstPublishYear,
    )

fun WorkDto.toBook(): Book =
    Book(
        key = key.toWorkKey(),
        title = title,
        authors = emptyList(),
        coverUrl = coverUrl(covers?.firstOrNull()),
        firstPublishYear = null,
        description = description,
    )
