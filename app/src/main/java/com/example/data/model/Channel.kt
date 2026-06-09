package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val category: String,
    val logoUrl: String,
    val isPreloaded: Boolean,
    val isFavorite: Boolean = false,
    val description: String = "",
    val liveScoreInfo: String = "",
    val liveStatusText: String = ""
)
