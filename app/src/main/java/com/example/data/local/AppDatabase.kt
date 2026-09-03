package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class, 
        HistoryEntity::class, 
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        UserProfileEntity::class,
        SearchHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
