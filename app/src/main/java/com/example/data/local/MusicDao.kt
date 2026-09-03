package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Songs
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    // History
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT 50")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history GROUP BY songId ORDER BY count(songId) DESC, MAX(playedAt) DESC LIMIT 20")
    fun getMostPlayedHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: String): Flow<Boolean>

    // User Profile
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Playlists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: String, songId: String)

    @androidx.room.Transaction
    @Query("SELECT * FROM playlists")
    fun getPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    // Recommendations Helper
    @Query("SELECT artist FROM history GROUP BY artist ORDER BY COUNT(artist) DESC LIMIT 5")
    suspend fun getTopArtistsFromHistory(): List<String>
    
    // Search History
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(search: SearchHistoryEntity)
    
    @Query("SELECT query FROM search_history GROUP BY query ORDER BY COUNT(query) DESC LIMIT 5")
    suspend fun getTopSearchQueries(): List<String>
}
