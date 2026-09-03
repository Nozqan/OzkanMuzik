package com.example.data.repository

import com.example.data.local.SongEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.FavoriteEntity
import com.example.data.provider.SearchProvider
import com.example.data.source.LocalDataSource
import com.example.domain.model.SongModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.async

class MusicRepository(
    private val localDataSource: LocalDataSource,
    private val searchProviders: List<SearchProvider>
) {

    val favorites: Flow<List<SongModel>> = localDataSource.getFavorites().map { list ->
        list.map { 
            SongModel(it.songId, it.title, it.artist, null, it.thumbnail, 0, "local", it.songId, true) 
        }
    }
    
    val history: Flow<List<SongModel>> = localDataSource.getHistory().map { list ->
        list.map {
            SongModel(it.songId, it.title, it.artist, null, it.thumbnail, it.durationPlayed, "local", it.songId, true)
        }
    }

    val mostPlayed: Flow<List<SongModel>> = localDataSource.getMostPlayedHistory().map { list ->
        list.map {
            SongModel(it.songId, it.title, it.artist, null, it.thumbnail, it.durationPlayed, "local", it.songId, true)
        }
    }

    val localSongs: Flow<List<SongModel>> = localDataSource.getAllSongs().map { list ->
        list.map {
            SongModel(it.id, it.title, it.artist, it.album, it.thumbnail, it.duration, it.provider, it.sourceId, it.streamAvailable)
        }
    }

    private val searchCache = java.util.concurrent.ConcurrentHashMap<String, List<SongModel>>()

    suspend fun search(query: String): Result<List<SongModel>> {
        // Save query to local history if it's not a generic/internal call
        if (query.isNotBlank() && query != "trending" && query != "pop") {
            localDataSource.insertSearchQuery(com.example.data.local.SearchHistoryEntity(query = query.lowercase().trim(), searchedAt = System.currentTimeMillis()))
        }
        
        val cached = searchCache[query.lowercase()]
        if (cached != null && cached.isNotEmpty()) {
            return Result.success(cached)
        }
        val allSongs = mutableListOf<SongModel>()
        var lastError: Throwable? = null
        for (provider in searchProviders) {
            try {
                val result = provider.search(query)
                if (result.isSuccess) {
                    val list = result.getOrNull() ?: emptyList()
                    if (list.isNotEmpty()) {
                        allSongs.addAll(list)
                        if (allSongs.size >= 40) break // Increased for richer results
                    }
                } else {
                    lastError = result.exceptionOrNull()
                }
            } catch (e: Exception) {
                lastError = e
            }
        }
        return if (allSongs.isNotEmpty()) {
            val distinctList = allSongs.distinctBy { it.title.lowercase().trim() + it.artist.lowercase().trim() }
            searchCache[query.lowercase()] = distinctList
            Result.success(distinctList)
        } else {
            Result.failure(lastError ?: Exception("Arama sonucu bulunamadı"))
        }
    }

    suspend fun toggleFavorite(song: SongModel, isFavorite: Boolean) {
        if (isFavorite) {
            localDataSource.insertFavorite(FavoriteEntity(song.id, song.title, song.artist, song.thumbnail, System.currentTimeMillis()))
        } else {
            localDataSource.deleteFavorite(song.id)
        }
    }
    
    fun isFavorite(songId: String): Flow<Boolean> = localDataSource.isFavorite(songId)
    
    suspend fun addToHistory(song: SongModel) {
        localDataSource.insertHistory(HistoryEntity(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            thumbnail = song.thumbnail,
            playedAt = System.currentTimeMillis(),
            durationPlayed = song.duration
        ))
    }

    // User Profile
    val userProfile = localDataSource.getUserProfile()

    suspend fun updateUserProfile(name: String, avatarUrl: String?) {
        localDataSource.insertUserProfile(
            com.example.data.local.UserProfileEntity(
                name = name,
                avatarUrl = avatarUrl,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // Playlists
    val playlists = localDataSource.getPlaylistsWithSongs()

    suspend fun syncLocalFolder(context: android.content.Context, folderUri: android.net.Uri) {
        val scanner = com.example.data.local.FolderScanner(context)
        val localSongs = scanner.scanFolder(folderUri)
        localDataSource.insertSongs(localSongs)
    }

    suspend fun syncLocalMusic(context: android.content.Context) {
        val scanner = com.example.data.local.MediaStoreScanner(context)
        val localSongs = scanner.scanLocalAudio()
        localDataSource.insertSongs(localSongs)
    }

    suspend fun createPlaylist(name: String) {
        val id = java.util.UUID.randomUUID().toString()
        localDataSource.insertPlaylist(
            com.example.data.local.PlaylistEntity(
                playlistId = id,
                name = name,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addSongToPlaylist(playlistId: String, song: SongModel) {
        localDataSource.insertSong(com.example.data.local.SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            thumbnail = song.thumbnail,
            duration = song.duration,
            provider = song.provider,
            sourceId = song.sourceId,
            streamAvailable = song.streamAvailable
        ))
        localDataSource.insertPlaylistSongCrossRef(com.example.data.local.PlaylistSongCrossRef(playlistId, song.id))
    }

    // Recommendations
    suspend fun getPersonalizedRecommendations(): Result<List<SongModel>> = kotlinx.coroutines.coroutineScope {
        val topArtists = localDataSource.getTopArtistsFromHistory()
        val topSearches = localDataSource.getTopSearchQueries()
        
        val combinedKeywords = (topArtists + topSearches).distinct().filter { it.isNotBlank() }
        
        if (combinedKeywords.isEmpty()) {
            return@coroutineScope search("trending") // default
        }
        
        // Fetch from multiple top keywords concurrently to build a rich Smart Mix
        val keywordsToFetch = combinedKeywords.shuffled().take(3)
        val deferredResults = keywordsToFetch.map { keyword ->
            async { search(keyword) }
        }
        
        val mixedSongs = mutableListOf<SongModel>()
        deferredResults.forEach { deferred ->
            val res = deferred.await()
            if (res.isSuccess) {
                val songs = res.getOrNull() ?: emptyList()
                mixedSongs.addAll(songs.take(10)) // Take top 10 from each artist
            }
        }
        
        if (mixedSongs.isNotEmpty()) {
            Result.success(mixedSongs.shuffled().distinctBy { it.id }.take(25))
        } else {
            search("pop") // ultimate fallback
        }
    }
}
