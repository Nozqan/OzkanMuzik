package com.example.features.offlinecache

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.OfflinePin
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object OfflineCacheManager {
    private val _cachedSongs = MutableStateFlow<List<SongModel>>(emptyList())
    val cachedSongs = _cachedSongs.asStateFlow()

    fun cacheSong(song: SongModel) {
        if (_cachedSongs.value.none { it.id == song.id }) {
            _cachedSongs.value = _cachedSongs.value + song
        }
    }

    fun removeSong(songId: String) {
        _cachedSongs.value = _cachedSongs.value.filter { it.id != songId }
    }

    fun clearAllCache() {
        _cachedSongs.value = emptyList()
    }
}

@Composable
fun OfflineCacheScreen(availableSongs: List<SongModel>, onBack: () -> Unit = {}) {
    val cachedList by OfflineCacheManager.cachedSongs.collectAsState()

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
                Icon(Icons.Default.OfflinePin, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çevrimdışı İndirme & Önbellek", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (cachedList.isNotEmpty()) {
                IconButton(onClick = { OfflineCacheManager.clearAllCache() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Tümünü Sil", tint = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage Usage Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kullanılan Önbellek Alanı", color = TextSecondary, fontSize = 13.sp)
                    Text("${cachedList.size * 4.2f} MB", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (cachedList.size * 0.05f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = YouTubeRed,
                    trackColor = DarkSpaceBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("${cachedList.size} şarkı internetsiz dinlemeye hazır", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("İndirilen Şarkılar", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (cachedList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Henüz çevrimdışı kaydedilmiş şarkı yok.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (availableSongs.isNotEmpty()) {
                        Button(
                            onClick = { availableSongs.take(5).forEach { OfflineCacheManager.cacheSong(it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("İlk 5 Şarkıyı İndir", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cachedList) { song ->
                    LibraryItem(
                        title = song.title,
                        subtitle = "${song.artist} • Çevrimdışı Hazır",
                        thumbnailUrl = song.thumbnail,
                        onClick = { PlayerStateHolder.playSong(song, cachedList) }
                    )
                }
            }
        }
    }
}
