package com.example.data.provider.newpipe

import android.util.Log
import com.example.domain.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Domain and Data models representing the external NewPipe extraction results.
 */
data class NewPipeAudioTrack(
    val url: String,
    val bitrate: Int,
    val format: String
)

data class NewPipeStreamInfo(
    val title: String,
    val uploader: String,
    val duration: Long,
    val thumbnailUrl: String,
    val audioTracks: List<NewPipeAudioTrack>
)

/**
 * Resolver for interacting with NewPipe Extractor.
 * Implements crash-safety with try-catch blocks and maps external results to internal Domain models.
 */
class NewPipeResolver {

    suspend fun resolveStream(videoId: String): Result<NewPipeStreamInfo> = withContext(Dispatchers.IO) {
        try {
            // Placeholder for actual NewPipeExtractor initialization and streaming:
            // e.g. val streamInfo = StreamInfo.getInfo(NewPipe.getService(0), "https://youtube.com/watch?v=$videoId")
            // For now, we simulate extraction safely to avoid crashing if NewPipe Extractor is missing.
            
            Log.d("NewPipeResolver", "Resolving stream for videoId: $videoId")
            
            // Simulating network delay
            Thread.sleep(500)
            
            val simulatedResult = NewPipeStreamInfo(
                title = "Resolved Stream Title",
                uploader = "Artist Name",
                duration = 200000L,
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                audioTracks = listOf(
                    NewPipeAudioTrack(url = "https://example.com/stream.m4a", bitrate = 128, format = "m4a")
                )
            )
            
            Result.success(simulatedResult)
        } catch (e: Exception) {
            Log.e("NewPipeResolver", "Failed to resolve stream using NewPipe", e)
            Result.failure(e)
        }
    }

    /**
     * Safely maps a NewPipeStreamInfo to our internal Domain SongModel
     */
    fun mapToSongModel(streamInfo: NewPipeStreamInfo, originalId: String): SongModel {
        return SongModel(
            id = originalId,
            title = streamInfo.title,
            artist = streamInfo.uploader,
            album = null,
            thumbnail = streamInfo.thumbnailUrl,
            duration = streamInfo.duration,
            provider = "newpipe",
            sourceId = streamInfo.audioTracks.maxByOrNull { it.bitrate }?.url ?: "",
            streamAvailable = streamInfo.audioTracks.isNotEmpty()
        )
    }
}
