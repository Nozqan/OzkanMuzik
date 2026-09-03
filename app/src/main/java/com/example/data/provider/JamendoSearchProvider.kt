package com.example.data.provider

import com.example.data.remote.JamendoApi
import com.example.domain.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamendoSearchProvider(private val api: JamendoApi) : SearchProvider {
    override val providerName = "Jamendo"

    override suspend fun search(query: String): Result<List<SongModel>> = withContext(Dispatchers.IO) {
        try {
            val response = if (query.equals("trending", ignoreCase = true) || query.isBlank()) {
                api.getPopularTracks(limit = 25)
            } else {
                api.searchTracks(query = query, limit = 25)
            }

            val rawData = response.results ?: emptyList()
            val songs = rawData.filter { !it.id.isNullOrBlank() && !it.audio.isNullOrBlank() }
                .map { track ->
                    val durationMs = (track.duration ?: 180L) * 1000L // convert seconds to ms

                    SongModel(
                        id = "jamendo_${track.id}",
                        title = track.name ?: "Bilinmeyen Başlık",
                        artist = track.artistName ?: "Bilinmeyen Sanatçı",
                        album = track.albumName ?: "Single",
                        thumbnail = track.image,
                        duration = durationMs,
                        provider = providerName,
                        sourceId = track.audio ?: "",
                        streamAvailable = true
                    )
                }

            if (songs.isNotEmpty()) {
                Result.success(songs)
            } else {
                Result.failure(Exception("Jamendo search yielded no results for '$query'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
