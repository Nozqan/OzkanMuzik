package com.example.player.service

import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class CrossfadeCurve(val label: String, val description: String) {
    EQUAL_POWER("Eşit Güç (Önerilen)", "Geçiş sırasında ses seviyesinde düşüş hissettirmeyen profesyonel DJ eğrisi"),
    LINEAR("Doğrusal (Linear)", "Sabit hızda azalan ve artan doğrusal geçiş"),
    S_CURVE("S-Eğrisi (Smooth)", "Başlangıç ve bitişte yumuşatılmış geçiş eğrisi"),
    EXPONENTIAL("Üstel (Eksponansiyel)", "Hızlı başlayıp yumuşak sönen dinamik eğri")
}

/**
 * CrossfadeFeature module for audio playback engine.
 * Enables seamless transitions between tracks by overlapping audio playback,
 * automating volume ramps across players with configurable durations and fade curves.
 */
class CrossfadeFeature private constructor() {

    companion object {
        private const val TAG = "CrossfadeFeature"
        private const val UPDATE_INTERVAL_MS = 25L

        @Volatile
        private var instance: CrossfadeFeature? = null

        fun getInstance(): CrossfadeFeature {
            return instance ?: synchronized(this) {
                instance ?: CrossfadeFeature().also { instance = it }
            }
        }
    }

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _durationSeconds = MutableStateFlow(4.0f)
    val durationSeconds: StateFlow<Float> = _durationSeconds.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _selectedCurve = MutableStateFlow(CrossfadeCurve.EQUAL_POWER)
    val selectedCurve: StateFlow<CrossfadeCurve> = _selectedCurve.asStateFlow()

    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    private val _transitionProgress = MutableStateFlow(0.0f)
    val transitionProgress: StateFlow<Float> = _transitionProgress.asStateFlow()

    private val _fadingOutTitle = MutableStateFlow<String?>(null)
    val fadingOutTitle: StateFlow<String?> = _fadingOutTitle.asStateFlow()

    private val _fadingInTitle = MutableStateFlow<String?>(null)
    val fadingInTitle: StateFlow<String?> = _fadingInTitle.asStateFlow()

    private var crossfadeJob: Job? = null

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    fun setDurationSeconds(seconds: Float) {
        _durationSeconds.value = seconds.coerceIn(1.0f, 12.0f)
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _gaplessPlayback.value = enabled
    }

    fun setCurve(curve: CrossfadeCurve) {
        _selectedCurve.value = curve
    }

    /**
     * Checks if current playback position has entered the crossfade window.
     */
    fun shouldTriggerCrossfade(currentPositionMs: Long, totalDurationMs: Long): Boolean {
        if (!_isEnabled.value) return false
        if (_isTransitioning.value) return false
        if (totalDurationMs <= 0L) return false

        val crossfadeMs = (_durationSeconds.value * 1000L).toLong()
        if (totalDurationMs <= crossfadeMs * 2) return false

        return currentPositionMs >= (totalDurationMs - crossfadeMs)
    }

    /**
     * Calculates volume levels for fading out and fading in players based on selected curve.
     */
    fun calculateVolumes(progress: Float, curve: CrossfadeCurve = _selectedCurve.value): Pair<Float, Float> {
        val clampedProgress = progress.coerceIn(0.0f, 1.0f)
        return when (curve) {
            CrossfadeCurve.EQUAL_POWER -> {
                // Constant power curve: sin(t * pi/2)^2 + cos(t * pi/2)^2 = 1
                val rad = clampedProgress * (PI.toFloat() / 2.0f)
                val volumeOut = cos(rad)
                val volumeIn = sin(rad)
                Pair(volumeOut.coerceIn(0f, 1f), volumeIn.coerceIn(0f, 1f))
            }
            CrossfadeCurve.LINEAR -> {
                Pair(1.0f - clampedProgress, clampedProgress)
            }
            CrossfadeCurve.S_CURVE -> {
                val s = clampedProgress * clampedProgress * (3f - 2f * clampedProgress)
                Pair(1.0f - s, s)
            }
            CrossfadeCurve.EXPONENTIAL -> {
                val out = (1.0f - clampedProgress) * (1.0f - clampedProgress)
                val `in` = clampedProgress * clampedProgress
                Pair(out, `in`)
            }
        }
    }

    /**
     * Performs audio crossfade between two ExoPlayer instances or simulated volume ramps.
     */
    fun performCrossfade(
        scope: CoroutineScope,
        fromPlayer: Player?,
        toPlayer: Player?,
        fadingOutSongTitle: String? = null,
        fadingInSongTitle: String? = null,
        customDurationMs: Long? = null,
        onComplete: () -> Unit = {}
    ) {
        crossfadeJob?.cancel()

        val durationMs = customDurationMs ?: (_durationSeconds.value * 1000L).toLong().coerceAtLeast(500L)
        _fadingOutTitle.value = fadingOutSongTitle
        _fadingInTitle.value = fadingInSongTitle
        _isTransitioning.value = true
        _transitionProgress.value = 0.0f

        crossfadeJob = scope.launch(Dispatchers.Main) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Initialize volumes
                val (initialOut, initialIn) = calculateVolumes(0f)
                fromPlayer?.volume = initialOut
                toPlayer?.volume = initialIn
                if (toPlayer?.isPlaying == false) {
                    toPlayer.play()
                }

                while (isActive) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0.0f, 1.0f)
                    
                    _transitionProgress.value = progress

                    val (volumeOut, volumeIn) = calculateVolumes(progress)
                    fromPlayer?.volume = volumeOut
                    toPlayer?.volume = volumeIn

                    if (progress >= 1.0f) {
                        break
                    }
                    delay(UPDATE_INTERVAL_MS)
                }

                // Finalize crossfade
                toPlayer?.volume = 1.0f
                fromPlayer?.volume = 0.0f
                fromPlayer?.pause()
            } catch (e: Exception) {
                Log.e(TAG, "Crossfade transition interrupted: ${e.message}")
            } finally {
                _isTransitioning.value = false
                _transitionProgress.value = 0.0f
                _fadingOutTitle.value = null
                _fadingInTitle.value = null
                onComplete()
            }
        }
    }

    /**
     * Previews crossfade transition animation and audio demonstration.
     */
    fun simulateTestTransition(
        scope: CoroutineScope,
        songA: String = "Şarkı A (Çalan)",
        songB: String = "Şarkı B (Giren)",
        onComplete: () -> Unit = {}
    ) {
        performCrossfade(
            scope = scope,
            fromPlayer = null,
            toPlayer = null,
            fadingOutSongTitle = songA,
            fadingInSongTitle = songB,
            customDurationMs = (_durationSeconds.value * 1000L).toLong(),
            onComplete = onComplete
        )
    }

    fun cancelTransition() {
        crossfadeJob?.cancel()
        crossfadeJob = null
        _isTransitioning.value = false
        _transitionProgress.value = 0.0f
        _fadingOutTitle.value = null
        _fadingInTitle.value = null
    }

    fun release() {
        cancelTransition()
    }
}
