package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val thumbnail: String?,
    val duration: Long,
    val provider: String,
    val sourceId: String,
    val streamAvailable: Boolean
)
