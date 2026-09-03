package com.example.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String
)
