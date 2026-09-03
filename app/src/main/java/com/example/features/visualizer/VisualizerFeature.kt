package com.example.features.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlin.math.sin

enum class VisualizerStyle {
    SPECTRUM_BARS, WAVEFORM, CIRCLE_PULSE, NEON_PARTICLES
}

@Composable
fun VisualizerScreen(onBack: () -> Unit = {}) {
    val isPlaying by PlayerStateHolder.isPlaying.collectAsState()
    var currentStyle by remember { mutableStateOf(VisualizerStyle.SPECTRUM_BARS) }

    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ses Dalga & Frekans Görselleştirici", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visualizer Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (currentStyle) {
                    VisualizerStyle.SPECTRUM_BARS -> {
                        val barCount = 32
                        val barWidth = width / (barCount * 1.4f)
                        val spacing = (width - (barWidth * barCount)) / (barCount - 1)

                        for (i in 0 until barCount) {
                            val factor = if (isPlaying) (sin(phase + (i * 0.35f)) + 1.2f) / 2.2f else 0.08f
                            val barHeight = (height * 0.85f * factor).coerceAtLeast(6f)
                            val x = i * (barWidth + spacing)
                            val y = height - barHeight

                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(YouTubeRed, Color(0xFFFF5252), Color(0xFF8B0000)),
                                    startY = y,
                                    endY = height
                                ),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                    VisualizerStyle.WAVEFORM -> {
                        val points = 60
                        val step = width / points
                        for (i in 0 until points) {
                            val factor = if (isPlaying) sin(phase * 2f + (i * 0.2f)) * (height * 0.35f) else 0f
                            val cy = height / 2f + factor
                            drawCircle(
                                color = YouTubeRed,
                                radius = 4.5f,
                                center = Offset(i * step, cy)
                            )
                        }
                    }
                    VisualizerStyle.CIRCLE_PULSE -> {
                        val center = Offset(width / 2f, height / 2f)
                        val pulseFactor = if (isPlaying) (sin(phase * 3f) + 1f) / 2f else 0.1f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(YouTubeRed.copy(alpha = 0.8f), Color.Transparent),
                                center = center,
                                radius = (height * 0.45f) * (0.6f + pulseFactor * 0.4f)
                            ),
                            center = center,
                            radius = (height * 0.45f) * (0.6f + pulseFactor * 0.4f)
                        )
                    }
                    VisualizerStyle.NEON_PARTICLES -> {
                        for (i in 0..24) {
                            val px = (sin(phase + i) + 1f) / 2f * width
                            val py = (sin(phase * 1.5f + i * 2) + 1f) / 2f * height
                            drawCircle(
                                color = if (i % 2 == 0) YouTubeRed else Color(0xFFFF8C00),
                                radius = (i % 5 + 3).toFloat(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Görselleştirici Stili Seçin", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("Spektrum", VisualizerStyle.SPECTRUM_BARS),
                Pair("Dalga", VisualizerStyle.WAVEFORM),
                Pair("Pals Çember", VisualizerStyle.CIRCLE_PULSE),
                Pair("Parçacık", VisualizerStyle.NEON_PARTICLES)
            ).forEach { item ->
                val isSelected = currentStyle == item.second
                Button(
                    onClick = { currentStyle = item.second },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) YouTubeRed else SurfaceColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(44.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text(item.first, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}
