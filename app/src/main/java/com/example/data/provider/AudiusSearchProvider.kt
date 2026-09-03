package com.example.data.provider

import com.example.data.remote.AudiusApi
import com.example.domain.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudiusSearchProvider(private val api: AudiusApi) : SearchProvider {
    override val providerName = "Audius"

    override suspend fun search(query: String): Result<List<SongModel>> = withContext(Dispatchers.IO) {
        try {
            val response = if (query.equals("trending", ignoreCase = true) || query.isBlank()) {
                api.getTrendingTracks(limit = 25)
            } else {
                api.searchTracks(query = query, limit = 25)
            }

            val rawData = response.data ?: emptyList()
            val songs = rawData.filter { !it.id.isNullOrBlank() && !it.title.isNullOrBlank() }
                .map { track ->
                    val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/${track.id}/stream?app_name=AKREP_MUSIC"
                    val thumb = track.artwork?.large ?: track.artwork?.medium ?: track.artwork?.small
                    val durationMs = (track.duration ?: 180L) * 1000L // convert seconds to ms

                    SongModel(
                        id = "audius_${track.id}",
                        title = track.title ?: "Bilinmeyen Başlık",
                        artist = track.user?.name ?: "Bilinmeyen Sanatçı",
                        album = track.genre ?: "Single",
                        thumbnail = thumb,
                        duration = durationMs,
                        provider = providerName,
                        sourceId = streamUrl,
                        streamAvailable = true
                    )
                }

            if (songs.isNotEmpty()) {
                Result.success(songs)
            } else {
                Result.failure(Exception("Audius search yielded no results for '$query'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
