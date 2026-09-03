package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class JamendoResponse(
    @Json(name = "results") val results: List<JamendoTrackDto>?
)

@JsonClass(generateAdapter = true)
data class JamendoTrackDto(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "duration") val duration: Long?,
    @Json(name = "artist_name") val artistName: String?,
    @Json(name = "album_name") val albumName: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "audio") val audio: String?
)

interface JamendoApi {
    @GET("v3.0/tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = "56d30c95",
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("search") query: String,
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String = "56d30c95",
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("boost") boost: String = "popularity_month",
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse
}
