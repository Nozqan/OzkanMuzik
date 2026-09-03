package com.example.data.provider

import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.SongModel

interface SearchProvider {
    val providerName: String
    suspend fun search(query: String): Result<List<SongModel>>
}

interface StreamProvider {
    val providerName: String
    suspend fun getStreamInfo(sourceId: String): Result<AudioStreamInfo>
}
