package com.example.data.source

import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.local.PlaylistWithSongs
import com.example.data.local.SongEntity
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getFavorites(): Flow<List<FavoriteEntity>>
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavorite(songId: String)
    fun isFavorite(songId: String): Flow<Boolean>

    fun getHistory(): Flow<List<HistoryEntity>>
    fun getMostPlayedHistory(): Flow<List<HistoryEntity>>
    suspend fun insertHistory(history: HistoryEntity)
    suspend fun clearHistory()
    suspend fun getTopArtistsFromHistory(): List<String>

    fun getUserProfile(): Flow<UserProfileEntity?>
    suspend fun insertUserProfile(profile: UserProfileEntity)

    suspend fun insertSongs(songs: List<SongEntity>)
    suspend fun insertSong(song: SongEntity)
    fun getAllSongs(): Flow<List<SongEntity>>

    fun getPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    suspend fun deleteSongFromPlaylist(playlistId: String, songId: String)
    fun getSearchHistory(): Flow<List<com.example.data.local.SearchHistoryEntity>>
    suspend fun insertSearchQuery(search: com.example.data.local.SearchHistoryEntity)
    suspend fun getTopSearchQueries(): List<String>
}

class RoomLocalDataSource(private val musicDao: MusicDao) : LocalDataSource {
    override fun getFavorites() = musicDao.getFavorites()
    override suspend fun insertFavorite(favorite: FavoriteEntity) = musicDao.insertFavorite(favorite)
    override suspend fun deleteFavorite(songId: String) = musicDao.deleteFavorite(songId)
    override fun isFavorite(songId: String) = musicDao.isFavorite(songId)

    override fun getHistory() = musicDao.getHistory()
    override fun getMostPlayedHistory() = musicDao.getMostPlayedHistory()
    override suspend fun insertHistory(history: HistoryEntity) = musicDao.insertHistory(history)
    override suspend fun clearHistory() = musicDao.clearHistory()
    override suspend fun getTopArtistsFromHistory() = musicDao.getTopArtistsFromHistory()

    override fun getUserProfile() = musicDao.getUserProfile()
    override suspend fun insertUserProfile(profile: UserProfileEntity) = musicDao.insertUserProfile(profile)

    override suspend fun insertSongs(songs: List<SongEntity>) = musicDao.insertSongs(songs)
    override suspend fun insertSong(song: SongEntity) = musicDao.insertSong(song)
    override fun getAllSongs() = musicDao.getAllSongs()

    override fun getPlaylistsWithSongs() = musicDao.getPlaylistsWithSongs()
    override suspend fun insertPlaylist(playlist: PlaylistEntity) = musicDao.insertPlaylist(playlist)
    override suspend fun deletePlaylist(playlistId: String) = musicDao.deletePlaylist(playlistId)
    override suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef) = musicDao.insertPlaylistSongCrossRef(crossRef)
    override suspend fun deleteSongFromPlaylist(playlistId: String, songId: String) = musicDao.deleteSongFromPlaylist(playlistId, songId)
    override fun getSearchHistory() = musicDao.getSearchHistory()
    override suspend fun insertSearchQuery(search: com.example.data.local.SearchHistoryEntity) = musicDao.insertSearchQuery(search)
    override suspend fun getTopSearchQueries() = musicDao.getTopSearchQueries()
}
