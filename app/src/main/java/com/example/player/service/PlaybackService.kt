package com.example.player.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.presentation.player.SleepTimerManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"
    }

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    lateinit var sleepTimerManager: SleepTimerManager
    lateinit var crossfadeFeature: CrossfadeFeature

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 30_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(30_000, true)
            .build()
            
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer = player

        // Player Listener for Session state
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer playback error: ${error.errorCodeName} - ${error.message}", error)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "MediaItem transition: ${mediaItem?.mediaMetadata?.title} by ${mediaItem?.mediaMetadata?.artist}")
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Log.d(TAG, "MediaMetadata changed: title=${mediaMetadata.title}, artist=${mediaMetadata.artist}, artworkUri=${mediaMetadata.artworkUri}")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d(TAG, "Playback ready. Actual Duration: ${player.duration} ms")
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Playback ended")
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "Playback buffering...")
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "Playback idle")
                    }
                }
            }
        })

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaSession.Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: List<MediaItem>
            ): ListenableFuture<List<MediaItem>> {
                // Ensure media items have their URIs and metadata properly loaded and prepared
                val updatedMediaItems = mediaItems.map { item ->
                    val requestUri = item.requestMetadata.mediaUri ?: item.localConfiguration?.uri
                    val uri = requestUri ?: Uri.parse(item.mediaId)
                    val metadata = item.mediaMetadata.buildUpon()
                        .setTitle(item.mediaMetadata.title ?: "Unknown Track")
                        .setArtist(item.mediaMetadata.artist ?: "Unknown Artist")
                        .setArtworkUri(item.mediaMetadata.artworkUri)
                        .build()

                    item.buildUpon()
                        .setUri(uri)
                        .setMediaMetadata(metadata)
                        .build()
                }
                return Futures.immediateFuture(updatedMediaItems)
            }
        }
            
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(callback)
            .build()
        
        sleepTimerManager = SleepTimerManager(serviceScope) {
            mediaSession?.player?.pause()
        }

        crossfadeFeature = CrossfadeFeature.getInstance()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        sleepTimerManager.cancelTimer()
        crossfadeFeature.release()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}
