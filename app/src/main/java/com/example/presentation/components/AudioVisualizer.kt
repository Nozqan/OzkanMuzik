package com.example.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class VisualizerQuality {
    HIGH, MEDIUM, LOW, OFF
}

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    quality: VisualizerQuality = VisualizerQuality.HIGH,
    isPlaying: Boolean = true,
    color: Color = Color.White
) {
    if (quality == VisualizerQuality.OFF) {
        return
    }

    val barCount = when (quality) {
        VisualizerQuality.HIGH -> 40
        VisualizerQuality.MEDIUM -> 20
        VisualizerQuality.LOW -> 10
        VisualizerQuality.OFF -> 0
    }

    val updateInterval = when (quality) {
        VisualizerQuality.HIGH -> 50L
        VisualizerQuality.MEDIUM -> 100L
        VisualizerQuality.LOW -> 200L
        VisualizerQuality.OFF -> Long.MAX_VALUE
    }

    var amplitudes by remember { mutableStateOf(List(barCount) { 0.1f }) }

    LaunchedEffect(isPlaying, quality) {
        if (isPlaying) {
            while (true) {
                // Simulate real audio FFT data from Media3 session.
                // In a real implementation, this would be fed by android.media.audiofx.Visualizer or ExoPlayer's TeeAudioProcessor
                amplitudes = List(barCount) { 
                    (Random.nextFloat() * 0.8f + 0.2f)
                }
                delay(updateInterval)
            }
        } else {
            amplitudes = List(barCount) { 0.1f } // Reset to minimum when paused
        }
    }

    val animatedAmplitudes = amplitudes.map { target ->
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = updateInterval.toInt(), easing = LinearEasing),
            label = "VisualizerBar"
        ).value
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val barWidth = (canvasWidth / barCount) * 0.6f
        val space = (canvasWidth / barCount) * 0.4f

        var startX = space / 2
        
        animatedAmplitudes.forEach { amplitude ->
            val barHeight = canvasHeight * amplitude
            val startY = canvasHeight / 2 - barHeight / 2
            val endY = canvasHeight / 2 + barHeight / 2

            drawLine(
                color = color,
                start = Offset(x = startX, y = startY),
                end = Offset(x = startX, y = endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
            startX += barWidth + space
        }
    }
}
