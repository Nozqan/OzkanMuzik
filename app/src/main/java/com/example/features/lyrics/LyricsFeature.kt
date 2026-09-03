package com.example.features.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SongModel
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LyricsManager {
    fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = lrcContent.lines()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        val result = mutableListOf<LyricLine>()

        for (line in lines) {
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msStr = match.groupValues[3]
                val ms = if (msStr.length == 2) (msStr.toLongOrNull() ?: 0L) * 10 else (msStr.toLongOrNull() ?: 0L)
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    result.add(LyricLine(totalMs, text))
                }
            }
        }
        return result.sortedBy { it.timestampMs }
    }

    fun getSampleLyrics(songTitle: String): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ Müziğin ritmine kulak ver ♪"),
            LyricLine(5000L, "$songTitle - AKREP MÜZİK"),
            LyricLine(12000L, "Gözlerimi kapattım dinliyorum sesini"),
            LyricLine(20000L, "Karanlık gecede parlayan yıldızlar gibi"),
            LyricLine(28000L, "Her notada saklı bir anının izi"),
            LyricLine(36000L, "Ruhuma dokunur bu şarkının sözleri"),
            LyricLine(45000L, "Zaman durur, melodiler konuşur"),
            LyricLine(55000L, "Kalp atışları ritimle buluşur"),
            LyricLine(68000L, "♪ (Enstrümantal Melodi) ♪"),
            LyricLine(80000L, "Yine aynı his, yine aynı şarkı"),
            LyricLine(95000L, "Akrep Müzik ile her anın bir farkı"),
            LyricLine(115000L, "Sonsuzluğa uzanan bu sesin yankısı"),
            LyricLine(135000L, "♪ Nakarat tekrarı ♪")
        )
    }
}

@Composable
fun LyricsScreen(song: SongModel?, onBack: () -> Unit = {}) {
    val currentPosMs by PlayerStateHolder.currentPositionMs.collectAsState()
    val isPlaying by PlayerStateHolder.isPlaying.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf(song?.title ?: "") }
    var lyricsList by remember { mutableStateOf(LyricsManager.getSampleLyrics(song?.title ?: "Şarkı")) }

    val activeIndex = lyricsList.indexOfLast { it.timestampMs <= currentPosMs }.coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyricsList.indices) {
            coroutineScope.launch {
                listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
            }
        }
    }

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
                Icon(Icons.Default.Mic, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Senkronize Şarkı Sözleri", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { lyricsList = LyricsManager.getSampleLyrics(song?.title ?: "Şarkı") }) {
                Icon(Icons.Default.Sync, contentDescription = "Yenile", tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yenile", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (song != null) {
            Text(
                text = "${song.title} — ${song.artist}",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lyricsList) { index, line ->
                val isActive = index == activeIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { PlayerStateHolder.seekTo(line.timestampMs) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = line.text,
                        color = if (isActive) YouTubeRed else TextSecondary.copy(alpha = 0.7f),
                        fontSize = if (isActive) 18.sp else 15.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
