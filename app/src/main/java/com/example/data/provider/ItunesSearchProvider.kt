package com.example.data.provider

import com.example.data.remote.ItunesApi
import com.example.domain.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItunesSearchProvider(private val api: ItunesApi) : SearchProvider {
    override val providerName = "iTunes"

    override suspend fun search(query: String): Result<List<SongModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.search(query)
            val songs = response.results
                .filter { track ->
                    val titleLower = (track.trackName ?: "").lowercase()
                    val artistLower = (track.artistName ?: "").lowercase()
                    !titleLower.contains("karaoke") && !titleLower.contains("tribute") && 
                    !titleLower.contains("cover") && !artistLower.contains("tribute") &&
                    !artistLower.contains("karaoke")
                }
                .map { track ->
                SongModel(
                    id = "itunes_${track.trackId}",
                    title = track.trackName ?: "Bilinmeyen Başlık",
                    artist = track.artistName ?: "Bilinmeyen Sanatçı",
                    album = track.collectionName,
                    thumbnail = track.artworkUrl100?.replace("100x100", "500x500"),
                    duration = track.trackTimeMillis ?: 0L,
                    provider = providerName,
                    sourceId = track.previewUrl ?: "",
                    streamAvailable = track.previewUrl != null
                )
            }
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
