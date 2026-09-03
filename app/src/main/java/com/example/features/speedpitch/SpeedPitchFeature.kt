package com.example.features.speedpitch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Locale

@Composable
fun SpeedPitchScreen(onBack: () -> Unit = {}) {
    val speed by PlayerStateHolder.playbackSpeed.collectAsState()
    var pitchSemitones by remember { mutableStateOf(0f) }

    val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Oynatma Hızı & Ton Değiştirici", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {
                PlayerStateHolder.setPlaybackSpeed(1.0f)
                pitchSemitones = 0f
            }) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Sıfırla", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Playback Speed Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Oynatma Hızı", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${String.format(Locale.getDefault(), "%.2f", speed)}x", color = YouTubeRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = speed,
                    onValueChange = { PlayerStateHolder.setPlaybackSpeed(it) },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speedPresets.forEach { p ->
                        val isSelected = (speed * 100).toInt() == (p * 100).toInt()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) YouTubeRed else DarkSpaceBackground)
                                .clickable { PlayerStateHolder.setPlaybackSpeed(p) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${p}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pitch Shift Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Perde / Ton Değişimi (Pitch)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${if (pitchSemitones > 0) "+" else ""}${pitchSemitones.toInt()} Yarım Ton", color = YouTubeRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = pitchSemitones,
                    onValueChange = { pitchSemitones = it },
                    valueRange = -12f..12f,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Text("Şarkının temposunu değiştirmeden ses tonunu inceleştirip kalınlaştırın.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
