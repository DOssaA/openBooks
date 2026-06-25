package com.darioossa.openbooks.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    @SerialName("numFound") val numFound: Int = 0,
    @SerialName("docs") val docs: List<SearchDocDto> = emptyList(),
)

@Serializable
data class SearchDocDto(
    @SerialName("key") val key: String,
    @SerialName("title") val title: String,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Int? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
)
