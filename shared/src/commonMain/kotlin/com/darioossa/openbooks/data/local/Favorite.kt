package com.darioossa.openbooks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A book the user has saved. The OpenLibrary Work [key] is the stable primary key, so toggling the
 * same book twice is idempotent and survives across sessions.
 */
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val workKey: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
)
