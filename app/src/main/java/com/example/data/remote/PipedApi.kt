package com.example.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Url
import retrofit2.http.Query

interface PipedApi {
    @GET
    suspend fun search(@Url url: String, @Query("q") query: String, @Query("filter") filter: String = "music_songs"): PipedSearchResponse

    @GET
    suspend fun getStreams(@Url url: String): PipedStreamResponse
}

@JsonClass(generateAdapter = true)
data class PipedSearchResponse(
    val items: List<PipedSearchItem>?
)

@JsonClass(generateAdapter = true)
data class PipedSearchItem(
    val title: String?,
    val url: String?,
    val uploaderName: String?,
    val thumbnail: String?,
    val duration: Long?
)

@JsonClass(generateAdapter = true)
data class PipedStreamResponse(
    val audioStreams: List<PipedAudioStream>?
)

@JsonClass(generateAdapter = true)
data class PipedAudioStream(
    val url: String?,
    val format: String?,
    val quality: String?,
    val bitrate: Int?
)
