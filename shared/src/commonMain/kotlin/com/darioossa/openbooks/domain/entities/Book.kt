package com.darioossa.openbooks.domain.entities

data class Book(
    val key: String,
    val title: String,
    val authors: List<String>,
    val coverUrl: String?,
    val firstPublishYear: Int?,
    val description: String? = null,
)
