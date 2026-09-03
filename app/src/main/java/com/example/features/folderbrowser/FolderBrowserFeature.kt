package com.example.features.folderbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
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
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

data class DeviceFolder(
    val path: String,
    val name: String,
    val songs: List<SongModel>
)

@Composable
fun FolderBrowserScreen(allSongs: List<SongModel>, onBack: () -> Unit = {}) {
    var selectedFolder by remember { mutableStateOf<DeviceFolder?>(null) }

    val folders = remember(allSongs) {
        listOf(
            DeviceFolder("/storage/emulated/0/Music", "Music Klasörü", allSongs.take(allSongs.size / 2)),
            DeviceFolder("/storage/emulated/0/Download", "İndirilenler (Downloads)", allSongs.drop(allSongs.size / 2)),
            DeviceFolder("/storage/emulated/0/WhatsApp Audio", "WhatsApp Sesleri", emptyList()),
            DeviceFolder("/storage/emulated/0/AkrepMusic", "Akrep Müzik Kayıtları", allSongs.take(5))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Klasör Tarayıcısı & Yerel Dizin", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedFolder == null) {
            Text("Cihaz Ses Klasörleri", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(folders) { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceColor)
                            .clickable { selectedFolder = folder }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(folder.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("${folder.songs.size} Ses Dosyası • ${folder.path}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedFolder!!.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { selectedFolder = null }) {
                    Text("← Klasörler", color = YouTubeRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedFolder!!.songs.isEmpty()) {
                Text("Bu klasörde ses dosyası bulunamadı.", color = TextSecondary, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedFolder!!.songs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceColor)
                                .clickable { PlayerStateHolder.playSong(song, selectedFolder!!.songs) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(song.artist, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
