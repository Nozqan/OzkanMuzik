package com.example.features.smartmix

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SongModel
import com.example.presentation.home.LibraryItem
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

import com.example.di.DependencyProvider

object SmartMixEngine {
    suspend fun generateMix(mood: String, energyLevel: Float): List<SongModel> {
        val repository = DependencyProvider.repository ?: return emptyList()
        val query = when (mood) {
            "Yüksek Enerji" -> listOf("workout", "edm", "party").random()
            "Gece Sakinliği" -> listOf("lofi", "sleep", "ambient").random()
            "Spor & Motivasyon" -> listOf("gym", "motivation", "rock").random()
            "Odaklanma & Çalışma" -> listOf("study", "focus", "classical").random()
            "Yolculuk" -> listOf("roadtrip", "driving", "acoustic").random()
            else -> "pop"
        }
        
        val result = repository.search(query)
        if (result.isSuccess) {
            val fetched = result.getOrNull() ?: emptyList()
            val limited = fetched.shuffled().take(15)
            // Optional: simulate energy filtering
            return if (energyLevel > 80f) {
                limited.sortedBy { it.title.length } // dummy sort
            } else {
                limited.shuffled()
            }
        }
        return emptyList()
    }
}

@Composable
fun SmartMixScreen(onBack: () -> Unit = {}) {
    var selectedMood by remember { mutableStateOf("Yüksek Enerji") }
    var energySlider by remember { mutableStateOf(75f) }
    var generatedMix by remember { mutableStateOf<List<SongModel>>(emptyList()) }

    var isGenerating by remember { mutableStateOf(false) }
    val moods = listOf("Yüksek Enerji", "Gece Sakinliği", "Spor & Motivasyon", "Odaklanma & Çalışma", "Yolculuk")

    LaunchedEffect(selectedMood, energySlider) {
        isGenerating = true
        generatedMix = SmartMixEngine.generateMix(selectedMood, energySlider)
        isGenerating = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Akıllı Karışım & Mod İstasyonu", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Ruh Hali & Tema Seçin", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moods.take(3).forEach { mood ->
                val isSelected = mood == selectedMood
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) YouTubeRed else SurfaceColor)
                        .clickable { selectedMood = mood }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mood, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Enerji / Tempo Seviyesi", color = TextSecondary, fontSize = 13.sp)
            Text("${energySlider.toInt()}%", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = energySlider,
            onValueChange = { energySlider = it },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isGenerating) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YouTubeRed)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Oluşturulan Çalma Listesi (${generatedMix.size})", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (generatedMix.isNotEmpty()) {
                    Button(
                        onClick = { PlayerStateHolder.playSong(generatedMix.first(), generatedMix) },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tümünü Çal", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(generatedMix) { song ->
                    LibraryItem(
                        title = song.title,
                        subtitle = song.artist,
                        thumbnailUrl = song.thumbnail,
                        onClick = { PlayerStateHolder.playSong(song, generatedMix) }
                    )
                }
            }
        }
    }
}
