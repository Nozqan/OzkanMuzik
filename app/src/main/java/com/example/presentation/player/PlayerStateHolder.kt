package com.example.presentation.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.example.di.DependencyProvider
import com.example.domain.model.SongModel
import com.example.player.controller.PlayerController
import com.example.player.service.CrossfadeFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RepeatMode {
    OFF, ALL, ONE
}

object PlayerStateHolder {
    private const val TAG = "PlayerStateHolder"
    
    private var primaryPlayer: ExoPlayer? = null
    private var secondaryPlayer: ExoPlayer? = null
    private var isUsingPrimary = true

    var playerController: PlayerController? = null
        private set

    val activePlayer: ExoPlayer?
        get() = if (isUsingPrimary) primaryPlayer else secondaryPlayer

    val standbyPlayer: ExoPlayer?
        get() = if (isUsingPrimary) secondaryPlayer else primaryPlayer

    val crossfadeFeature = CrossfadeFeature.getInstance()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _currentSong = MutableStateFlow<SongModel?>(null)
    val currentSong: StateFlow<SongModel?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playlist = MutableStateFlow<List<SongModel>>(emptyList())
    val playlist: StateFlow<List<SongModel>> = _playlist.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    val isRepeat: StateFlow<Boolean> = _isRepeatDerived()

    private fun _isRepeatDerived(): StateFlow<Boolean> {
        val flow = MutableStateFlow(false)
        scope.launch {
            _repeatMode.collect { mode ->
                flow.value = mode != RepeatMode.OFF
            }
        }
        return flow.asStateFlow()
    }

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemainingMs = MutableStateFlow(0L)
    val sleepTimerRemainingMs: StateFlow<Long> = _sleepTimerRemainingMs.asStateFlow()

    private val _currentEqualizerPreset = MutableStateFlow("Varsayılan")
    val currentEqualizerPreset: StateFlow<String> = _currentEqualizerPreset.asStateFlow()

    private val _isMiniPlayerDismissed = MutableStateFlow(false)
    val isMiniPlayerDismissed: StateFlow<Boolean> = _isMiniPlayerDismissed.asStateFlow()

    fun resolveStreamUri(song: SongModel): String {
        return song.sourceId
    }

    fun dismissMiniPlayer() {
        _isMiniPlayerDismissed.value = true
        activePlayer?.pause()
        playerController?.pause()
        _isPlaying.value = false
    }

    fun showMiniPlayer() {
        _isMiniPlayerDismissed.value = false
    }

    fun initialize(context: Context) {
        val appContext = context.applicationContext

        // Initialize MediaController tied to PlaybackService
        if (playerController == null) {
            val controller = PlayerController(appContext)
            playerController = controller
            controller.initialize()

            // Connect Media3 session metadata listener to update UI layer
            controller.onMetadataExtractedListener = { title, artist, artworkUri, extractedDuration ->
                val current = _currentSong.value
                val updated = (current ?: SongModel(
                    id = title.hashCode().toString(),
                    title = title,
                    artist = artist,
                    album = "",
                    thumbnail = artworkUri ?: "",
                    duration = extractedDuration,
                    provider = "media3_session",
                    sourceId = "",
                    streamAvailable = true
                )).copy(
                    title = title.ifBlank { current?.title ?: "Bilinmeyen Parça" },
                    artist = artist.ifBlank { current?.artist ?: "Bilinmeyen Sanatçı" },
                    thumbnail = artworkUri ?: current?.thumbnail ?: "",
                    duration = if (extractedDuration > 0) extractedDuration else current?.duration ?: 0L
                )
                _currentSong.value = updated
                if (extractedDuration > 0) {
                    _durationMs.value = extractedDuration
                }
            }
        }

        if (primaryPlayer != null) return
        
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
            .setBackBuffer(
                /* backBufferDurationMs = */ 30_000,
                /* retainBackBufferFromKeyframe = */ true
            )
            .build()

        fun setupPlayerListener(player: ExoPlayer, isPrimary: Boolean) {
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "Player error (primary=$isPrimary): ${error.message}", error)
                    if ((isUsingPrimary && isPrimary) || (!isUsingPrimary && !isPrimary)) {
                        _isBuffering.value = false
                        _isPlaying.value = false
                    }
                    consecutiveFailures++
                    if (consecutiveFailures < 3) {
                        try {
                            skipNext(forceLoop = false)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to skip after error", e)
                        }
                    } else {
                        Log.e(TAG, "Too many consecutive failures, stopping playback.")
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val isCurrent = (isUsingPrimary && isPrimary) || (!isUsingPrimary && !isPrimary)
                    if (!isCurrent) return
                    mediaItem?.mediaMetadata?.let { meta ->
                        extractMetadataToUi(meta, player.duration)
                    }
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    val isCurrent = (isUsingPrimary && isPrimary) || (!isUsingPrimary && !isPrimary)
                    if (!isCurrent) return
                    extractMetadataToUi(mediaMetadata, player.duration)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    val isCurrent = (isUsingPrimary && isPrimary) || (!isUsingPrimary && !isPrimary)
                    if (!isCurrent) return

                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _isBuffering.value = true
                        }
                        Player.STATE_READY -> {
                            _isBuffering.value = false
                            consecutiveFailures = 0
                            // Extract actual media duration from stream without artificial constraints
                            val realDuration = player.duration
                            if (realDuration > 0) {
                                _durationMs.value = realDuration
                                _currentSong.value?.let { current ->
                                    if (current.duration <= 0 || current.duration != realDuration) {
                                        _currentSong.value = current.copy(duration = realDuration)
                                    }
                                }
                            }
                        }
                        Player.STATE_ENDED -> {
                            _isBuffering.value = false
                            if (!crossfadeFeature.isTransitioning.value) {
                                handleSongEnded()
                            }
                        }
                        Player.STATE_IDLE -> {
                            _isBuffering.value = false
                        }
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    val isCurrent = (isUsingPrimary && isPrimary) || (!isUsingPrimary && !isPrimary)
                    if (isCurrent) {
                        _isPlaying.value = playing
                        if (playing) {
                            startPositionUpdates()
                        } else if (!crossfadeFeature.isTransitioning.value) {
                            stopPositionUpdates()
                        }
                    }
                }
            })
        }

        primaryPlayer = ExoPlayer.Builder(appContext)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(loadControl)
            .build().also { setupPlayerListener(it, true) }

        secondaryPlayer = ExoPlayer.Builder(appContext)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(loadControl)
            .build().also { setupPlayerListener(it, false) }

        if (_currentSong.value == null) {
            val defaultSong = SongModel(
                id = "song_hit_1",
                title = "Müzik Dinle",
                artist = "Akrep Music",
                album = "Akrep Music",
                thumbnail = "",
                duration = 0L,
                provider = "stream",
                sourceId = "",
                streamAvailable = false
            )
            _currentSong.value = defaultSong
            _durationMs.value = 0L
            _currentPositionMs.value = 0L
        }
    }

    /**
     * Extracts album art, song title, and artist name from Media3 MediaMetadata and updates UI state.
     */
    private fun extractMetadataToUi(metadata: MediaMetadata, streamDuration: Long) {
        val title = metadata.title?.toString()
        val artist = metadata.artist?.toString()
        val artworkUri = metadata.artworkUri?.toString()
        val album = metadata.albumTitle?.toString()

        val current = _currentSong.value
        val effectiveDuration = if (streamDuration > 0) streamDuration else (_durationMs.value)

        if (!title.isNullOrBlank() || !artist.isNullOrBlank() || !artworkUri.isNullOrBlank()) {
            val updated = (current ?: SongModel(
                id = (title ?: "track").hashCode().toString(),
                title = title ?: "Bilinmeyen Parça",
                artist = artist ?: "Bilinmeyen Sanatçı",
                album = album ?: "",
                thumbnail = artworkUri ?: "",
                duration = effectiveDuration,
                provider = "media3",
                sourceId = "",
                streamAvailable = true
            )).copy(
                title = if (!title.isNullOrBlank()) title else current?.title ?: "Bilinmeyen Parça",
                artist = if (!artist.isNullOrBlank()) artist else current?.artist ?: "Bilinmeyen Sanatçı",
                album = if (!album.isNullOrBlank()) album else current?.album ?: "",
                thumbnail = if (!artworkUri.isNullOrBlank()) artworkUri else current?.thumbnail ?: "",
                duration = effectiveDuration
            )
            _currentSong.value = updated
            if (effectiveDuration > 0) {
                _durationMs.value = effectiveDuration
            }
        }
    }

    private fun handleSongEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                activePlayer?.seekTo(0)
                activePlayer?.play()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                skipNext(forceLoop = true)
            }
            RepeatMode.OFF -> {
                val currentList = _playlist.value
                val current = _currentSong.value
                if (currentList.isNotEmpty() && current != null) {
                    val currentIndex = currentList.indexOfFirst { it.id == current.id }
                    if (_isShuffle.value) {
                        skipNext(forceLoop = false)
                    } else if (currentIndex != -1 && currentIndex < currentList.size - 1) {
                        playSong(currentList[currentIndex + 1])
                    } else {
                        _isPlaying.value = false
                        seekTo(0)
                    }
                }
            }
        }
    }

    private var consecutiveFailures = 0

    fun playSong(song: SongModel, newPlaylist: List<SongModel> = emptyList(), allowCrossfade: Boolean = true) {
        _isMiniPlayerDismissed.value = false
        if (newPlaylist.isNotEmpty()) {
            _playlist.value = newPlaylist
        } else if (!_playlist.value.any { it.id == song.id }) {
            _playlist.value = _playlist.value + song
        }

        scope.launch(Dispatchers.Main) {
            _isBuffering.value = true
            var finalStreamUri = resolveStreamUri(song)
            var finalDuration = if (song.duration > 0) song.duration else 180000L

            if (song.provider == "iTunes") {
                try {
                    val pipedApi = com.example.di.DependencyProvider.pipedApi
                    if (pipedApi != null) {
                        val searchQuery = "${song.artist} ${song.title}"
                        val instances = listOf(
                            "https://pipedapi.nosebs.ru",
                            "https://api.piped.privacydev.net",
                            "https://pipedapi.kavin.rocks",
                            "https://pipedapi.smnz.de"
                        )
                        var success = false
                        for (instance in instances) {
                            try {
                                val searchResponse = kotlinx.coroutines.withContext(Dispatchers.IO) { pipedApi.search("${instance}/search", searchQuery) }
                                val firstVideo = searchResponse.items?.firstOrNull { it.url?.contains("/watch?v=") == true }
                                if (firstVideo != null) {
                                    val videoId = firstVideo.url?.substringAfter("/watch?v=") ?: ""
                                    if (videoId.isNotBlank()) {
                                        val streamResponse = kotlinx.coroutines.withContext(Dispatchers.IO) { pipedApi.getStreams("${instance}/streams/${videoId}") }
                                        val bestAudio = streamResponse.audioStreams?.maxByOrNull { it.bitrate ?: 0 } ?: streamResponse.audioStreams?.firstOrNull()
                                        if (bestAudio?.url != null) {
                                            finalStreamUri = bestAudio.url
                                            finalDuration = (firstVideo.duration ?: 0) * 1000L
                                            success = true
                                            break
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Piped API Error on ${instance}: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Piped API Outer Error: ${e.message}")
                }
            }

            val playableSong = if (song.sourceId.isBlank() || song.provider == "iTunes") song.copy(sourceId = finalStreamUri, duration = finalDuration) else song
            val previousSong = _currentSong.value
            val shouldCrossfade = allowCrossfade && crossfadeFeature.isEnabled.value && _isPlaying.value && previousSong != null && previousSong.id != playableSong.id

            _currentSong.value = playableSong
            _currentPositionMs.value = 0L
            _durationMs.value = finalDuration

            kotlinx.coroutines.withContext(Dispatchers.IO) {
                try {
                    com.example.di.DependencyProvider.repository?.addToHistory(playableSong)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save history: ${e.message}")
                }
            }

            val artworkUri = if (!playableSong.thumbnail.isNullOrBlank()) Uri.parse(playableSong.thumbnail) else null
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(playableSong.title)
                .setArtist(playableSong.artist)
                .setAlbumTitle(playableSong.album)
                .setArtworkUri(artworkUri)
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(playableSong.id)
                .setUri(Uri.parse(finalStreamUri))
                .setMediaMetadata(mediaMetadata)
                .build()

            playerController?.play(playableSong)

            val curActive = activePlayer
            val curStandby = standbyPlayer
            if (curActive != null && curStandby != null) {
                try {
                    if (shouldCrossfade) {
                        curStandby.setMediaItem(mediaItem)
                        curStandby.playbackParameters = PlaybackParameters(_playbackSpeed.value)
                        curStandby.volume = 0.0f
                        curStandby.prepare()
                        curStandby.play()
                        crossfadeFeature.performCrossfade(scope, curActive, curStandby, previousSong?.title, playableSong.title) {
                            isUsingPrimary = !isUsingPrimary
                            curActive.stop()
                            curActive.volume = 1.0f
                            _isPlaying.value = true
                        }
                    } else {
                        crossfadeFeature.cancelTransition()
                        curStandby.stop()
                        curActive.volume = 1.0f
                        curActive.setMediaItem(mediaItem)
                        curActive.playbackParameters = PlaybackParameters(_playbackSpeed.value)
                        curActive.prepare()
                        curActive.play()
                        _isPlaying.value = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing song: ${playableSong.title}", e)
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = activePlayer ?: return
        if (player.isPlaying) {
            player.pause()
            playerController?.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                _currentSong.value?.let { playSong(it, allowCrossfade = false) }
            } else {
                player.play()
                playerController?.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = activePlayer ?: return
        val safePos = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        player.seekTo(safePos)
        playerController?.seekTo(safePos)
        _currentPositionMs.value = safePos
    }

    fun seekForward(deltaMs: Long = 10000L) {
        val player = activePlayer ?: return
        val newPos = (player.currentPosition + deltaMs).coerceAtMost(player.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun seekBackward(deltaMs: Long = 10000L) {
        val player = activePlayer ?: return
        val newPos = (player.currentPosition - deltaMs).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun skipNext(forceLoop: Boolean = true) {
        val currentList = _playlist.value
        val current = _currentSong.value
        if (currentList.isEmpty() || current == null) return

        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val nextIndex = if (_isShuffle.value) {
                val available = currentList.indices.filter { it != currentIndex }
                if (available.isNotEmpty()) available.random() else currentIndex
            } else {
                if (currentIndex + 1 < currentList.size) {
                    currentIndex + 1
                } else {
                    -1
                }
            }
            if (nextIndex != -1) {
                playSong(currentList[nextIndex], allowCrossfade = true)
            } else {
                // Reached the end of the playlist, intelligently fetch a related song
                scope.launch {
                    try {
                        val repository = DependencyProvider.repository
                        if (repository != null) {
                            // Try to find songs by the same artist to continue the mood
                            val result = repository.search(current.artist)
                            if (result.isSuccess) {
                                val songs = result.getOrNull() ?: emptyList()
                                // filter out songs already in the playlist
                                val newSongs = songs.filter { s -> currentList.none { it.id == s.id } }
                                if (newSongs.isNotEmpty()) {
                                    // pick a random related song for variety
                                    val nextSong = newSongs.random()
                                    withContext(Dispatchers.Main) {
                                        playSong(nextSong, allowCrossfade = true)
                                    }
                                    return@launch
                                }
                            }
                        }
                        
                        // Fallback to loop if no new related songs found
                        withContext(Dispatchers.Main) {
                            if (forceLoop || _repeatMode.value == RepeatMode.ALL) {
                                playSong(currentList[0], allowCrossfade = true)
                            } else {
                                _isPlaying.value = false
                                seekTo(0)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            playSong(currentList[0], allowCrossfade = true)
                        }
                    }
                }
            }
        }
    }

    fun skipPrevious() {
        val player = activePlayer
        if (player != null && player.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        val currentList = _playlist.value
        val current = _currentSong.value
        if (currentList.isEmpty() || current == null) return

        val currentIndex = currentList.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val prevIndex = if (_isShuffle.value) {
                val available = currentList.indices.filter { it != currentIndex }
                if (available.isNotEmpty()) available.random() else currentIndex
            } else {
                if (currentIndex - 1 < 0) {
                    if (_repeatMode.value == RepeatMode.ALL) currentList.size - 1 else 0
                } else {
                    currentIndex - 1
                }
            }
            playSong(currentList[prevIndex], allowCrossfade = false)
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        playerController?.setShuffleModeEnabled(_isShuffle.value)
    }

    fun toggleFavorite() {
        _isFavorite.value = !_isFavorite.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        val media3Repeat = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        playerController?.setRepeatMode(media3Repeat)
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        val media3Repeat = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        playerController?.setRepeatMode(media3Repeat)
    }

    fun setShuffle(enabled: Boolean) {
        _isShuffle.value = enabled
        playerController?.setShuffleModeEnabled(enabled)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        primaryPlayer?.playbackParameters = PlaybackParameters(speed)
        secondaryPlayer?.playbackParameters = PlaybackParameters(speed)
        playerController?.setPlaybackSpeed(speed)
    }

    fun setEqualizerPreset(presetName: String) {
        _currentEqualizerPreset.value = presetName
    }

    fun addToQueue(song: SongModel) {
        if (!_playlist.value.any { it.id == song.id }) {
            _playlist.value = _playlist.value + song
        }
    }

    fun removeFromQueue(index: Int) {
        val list = _playlist.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _playlist.value = list
        }
    }

    fun clearQueue() {
        val current = _currentSong.value
        _playlist.value = if (current != null) listOf(current) else emptyList()
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerJob = scope.launch {
            var remaining = minutes * 60 * 1000L
            while (remaining > 0) {
                _sleepTimerRemainingMs.value = remaining
                delay(1000)
                remaining -= 1000L
            }
            _sleepTimerRemainingMs.value = 0L
            activePlayer?.pause()
            playerController?.pause()
            _isPlaying.value = false
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMs.value = 0L
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                activePlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = player.currentPosition
                        val dur = if (player.duration > 0) player.duration else _durationMs.value
                        _currentPositionMs.value = pos
                        if (player.duration > 0) {
                            _durationMs.value = dur
                        }

                        // Check for auto-crossfade trigger before track ends
                        if (crossfadeFeature.shouldTriggerCrossfade(pos, dur)) {
                            val currentList = _playlist.value
                            val current = _currentSong.value
                            if (currentList.isNotEmpty() && current != null) {
                                val currentIndex = currentList.indexOfFirst { it.id == current.id }
                                val nextIndex = if (_isShuffle.value) {
                                    val avail = currentList.indices.filter { it != currentIndex }
                                    if (avail.isNotEmpty()) avail.random() else -1
                                } else if (currentIndex != -1 && currentIndex + 1 < currentList.size) {
                                    currentIndex + 1
                                } else if (_repeatMode.value == RepeatMode.ALL) {
                                    0
                                } else {
                                    -1
                                }

                                if (nextIndex != -1) {
                                    playSong(currentList[nextIndex], allowCrossfade = true)
                                }
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
