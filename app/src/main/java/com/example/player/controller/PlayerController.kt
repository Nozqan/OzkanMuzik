package com.example.player.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.domain.model.SongModel
import com.example.player.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class PlayerController(private val context: Context) {

    companion object {
        private const val TAG = "PlayerController"
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionJob: Job? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val pendingCommands = ConcurrentLinkedQueue<(MediaController) -> Unit>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _currentTitle = MutableStateFlow<String?>(null)
    val currentTitle: StateFlow<String?> = _currentTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow<String?>(null)
    val currentArtist: StateFlow<String?> = _currentArtist.asStateFlow()

    private val _currentAlbumArtUri = MutableStateFlow<Uri?>(null)
    val currentAlbumArtUri: StateFlow<Uri?> = _currentAlbumArtUri.asStateFlow()

    private val _currentMediaMetadata = MutableStateFlow(MediaMetadata.EMPTY)
    val currentMediaMetadata: StateFlow<MediaMetadata> = _currentMediaMetadata.asStateFlow()

    private val _currentSong = MutableStateFlow<SongModel?>(null)
    val currentSong: StateFlow<SongModel?> = _currentSong.asStateFlow()

    var onMetadataExtractedListener: ((title: String, artist: String, artworkUri: String?, durationMs: Long) -> Unit)? = null

    fun initialize() {
        if (controller != null || controllerFuture != null) return
        try {
            val appContext = context.applicationContext
            val serviceIntent = Intent(appContext, PlaybackService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    appContext.startForegroundService(serviceIntent)
                } else {
                    appContext.startService(serviceIntent)
                }
            } catch (e: Exception) {
                appContext.startService(serviceIntent)
            }

            val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
            val future = MediaController.Builder(appContext, sessionToken).buildAsync()
            controllerFuture = future

            future.addListener({
                try {
                    val mediaCtrl = future.get()
                    controller = mediaCtrl
                    _isConnected.value = true
                    setupListeners(mediaCtrl)
                    processPendingCommands(mediaCtrl)
                    Log.d(TAG, "MediaController successfully connected to PlaybackService")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect MediaController to PlaybackService", e)
                    _isConnected.value = false
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Log.e(TAG, "Exception during MediaController initialization", e)
        }
    }

    private fun withController(action: (MediaController) -> Unit) {
        val ctrl = controller
        if (ctrl != null) {
            action(ctrl)
        } else {
            pendingCommands.add(action)
            initialize()
        }
    }

    private fun processPendingCommands(ctrl: MediaController) {
        while (!pendingCommands.isEmpty()) {
            val cmd = pendingCommands.poll()
            cmd?.invoke(ctrl)
        }
    }

    private fun setupListeners(ctrl: MediaController) {
        ctrl.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _isBuffering.value = true
                    }
                    Player.STATE_READY -> {
                        _isBuffering.value = false
                        val actualDuration = ctrl.duration
                        if (actualDuration > 0) {
                            _durationMs.value = actualDuration
                        }
                    }
                    Player.STATE_ENDED -> {
                        _isBuffering.value = false
                    }
                    Player.STATE_IDLE -> {
                        _isBuffering.value = false
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                extractAndPropagateMetadata(mediaItem?.mediaMetadata, ctrl.duration)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                extractAndPropagateMetadata(mediaMetadata, ctrl.duration)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error in MediaController: ${error.errorCodeName} - ${error.message}", error)
                _isBuffering.value = false
            }
        })

        // Initial metadata extraction if already populated
        ctrl.currentMediaItem?.let {
            extractAndPropagateMetadata(it.mediaMetadata, ctrl.duration)
        }
    }

    private fun extractAndPropagateMetadata(metadata: MediaMetadata?, playerDuration: Long) {
        if (metadata == null) return
        _currentMediaMetadata.value = metadata
        val title = metadata.title?.toString() ?: ""
        val artist = metadata.artist?.toString() ?: ""
        val artworkUri = metadata.artworkUri

        if (title.isNotBlank()) _currentTitle.value = title
        if (artist.isNotBlank()) _currentArtist.value = artist
        _currentAlbumArtUri.value = artworkUri

        val effectiveDuration = if (playerDuration > 0) playerDuration else _durationMs.value

        if (title.isNotBlank()) {
            _currentSong.value = SongModel(
                id = metadata.title?.hashCode()?.toString() ?: "unknown",
                title = title,
                artist = artist.ifBlank { "Bilinmeyen Sanatçı" },
                album = metadata.albumTitle?.toString() ?: "",
                thumbnail = artworkUri?.toString() ?: "",
                duration = effectiveDuration,
                provider = "media3_session",
                sourceId = "",
                streamAvailable = true
            )

            onMetadataExtractedListener?.invoke(title, artist, artworkUri?.toString(), effectiveDuration)
        }
    }

    fun play(song: SongModel) {
        val streamUrl = song.sourceId
        val artworkUri = if (!song.thumbnail.isNullOrBlank()) Uri.parse(song.thumbnail) else null

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(artworkUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(Uri.parse(streamUrl))
            .setMediaMetadata(mediaMetadata)
            .build()

        _currentSong.value = song
        _currentTitle.value = song.title
        _currentArtist.value = song.artist
        _currentAlbumArtUri.value = artworkUri

        withController { ctrl ->
            ctrl.setMediaItem(mediaItem)
            ctrl.prepare()
            ctrl.play()
        }
    }

    fun play(url: String, title: String, artist: String, coverUri: String?, album: String? = null) {
        val artworkUri = if (!coverUri.isNullOrBlank()) Uri.parse(coverUri) else null
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(artworkUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaId(url)
            .setMediaMetadata(mediaMetadata)
            .build()

        _currentTitle.value = title
        _currentArtist.value = artist
        _currentAlbumArtUri.value = artworkUri

        withController { ctrl ->
            ctrl.setMediaItem(mediaItem)
            ctrl.prepare()
            ctrl.play()
        }
    }

    fun togglePlayPause() {
        withController { ctrl ->
            if (ctrl.isPlaying) {
                ctrl.pause()
            } else {
                ctrl.play()
            }
        }
    }

    fun pause() {
        withController { ctrl -> ctrl.pause() }
    }

    fun play() {
        withController { ctrl -> ctrl.play() }
    }

    fun seekTo(positionMs: Long) {
        withController { ctrl ->
            ctrl.seekTo(positionMs.coerceAtLeast(0L))
            _currentPositionMs.value = positionMs
        }
    }

    fun seekForward(deltaMs: Long = 10000L) {
        withController { ctrl ->
            val newPos = (ctrl.currentPosition + deltaMs).coerceAtMost(ctrl.duration.coerceAtLeast(0L))
            seekTo(newPos)
        }
    }

    fun seekBackward(deltaMs: Long = 10000L) {
        withController { ctrl ->
            val newPos = (ctrl.currentPosition - deltaMs).coerceAtLeast(0L)
            seekTo(newPos)
        }
    }

    fun skipToNext() {
        withController { ctrl ->
            if (ctrl.hasNextMediaItem()) {
                ctrl.seekToNextMediaItem()
            } else {
                ctrl.seekToNext()
            }
        }
    }

    fun skipToPrevious() {
        withController { ctrl ->
            if (ctrl.hasPreviousMediaItem()) {
                ctrl.seekToPreviousMediaItem()
            } else {
                ctrl.seekToPrevious()
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        withController { ctrl ->
            ctrl.playbackParameters = PlaybackParameters(speed)
        }
    }

    fun setRepeatMode(repeatMode: Int) {
        withController { ctrl ->
            ctrl.repeatMode = repeatMode
        }
    }

    fun setShuffleModeEnabled(shuffleMode: Boolean) {
        withController { ctrl ->
            ctrl.shuffleModeEnabled = shuffleMode
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionJob = scope.launch {
            while (isActive) {
                controller?.let { ctrl ->
                    if (ctrl.isPlaying) {
                        _currentPositionMs.value = ctrl.currentPosition
                        val dur = ctrl.duration
                        if (dur > 0) {
                            _durationMs.value = dur
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    fun release() {
        stopPositionUpdates()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
        _isConnected.value = false
    }
}

