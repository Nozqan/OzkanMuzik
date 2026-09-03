package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0,
    val songId: String,
    val title: String,
    val artist: String,
    val thumbnail: String?,
    val playedAt: Long,
    val durationPlayed: Long
)
