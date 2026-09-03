package com.example.domain.model

data class SongModel(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val thumbnail: String?,
    val duration: Long,
    val provider: String,
    val sourceId: String,
    val streamAvailable: Boolean
)

data class AudioStreamInfo(
    val streamUrl: String,
    val mimeType: String,
    val codec: String,
    val bitrate: Int,
    val sampleRate: Int,
    val duration: Long,
    val expiration: Long,
    val provider: String
)
