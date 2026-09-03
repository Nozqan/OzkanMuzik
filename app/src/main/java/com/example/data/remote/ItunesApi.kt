package com.example.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ItunesResponse(val results: List<ItunesTrack>)

@JsonClass(generateAdapter = true)
data class ItunesTrack(
    val trackId: Long,
    val trackName: String?,
    val artistName: String?,
    val collectionName: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?,
    val previewUrl: String?
)

interface ItunesApi {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song"
    ): ItunesResponse
}
