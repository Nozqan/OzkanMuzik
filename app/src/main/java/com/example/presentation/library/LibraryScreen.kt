package com.example.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SongModel
import com.example.presentation.home.LibraryItem
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.*

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onNavigateToLocalAudio: () -> Unit = {}) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val history by viewModel.history.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // User Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle, 
                    contentDescription = "Profil", 
                    tint = TextPrimary, 
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (profile != null) {
                    Text(profile!!.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("AKREP MÜZİK", color = YouTubeRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("Kullanıcı Profili", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showCreateProfileDialog = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("Profil Adı Ekle", color = YouTubeRed, fontSize = 13.sp)
                    }
                }
            }

            IconButton(
                onClick = {
                    viewModel.syncLocalMusic(context)
                    scanStatusMessage = "Cihazdaki müzikler senkronize edildi"
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceColor, CircleShape)
            ) {
                Icon(Icons.Default.Sync, contentDescription = "Cihazı Tara", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
        }

        if (scanStatusMessage != null) {
            Text(scanStatusMessage!!, color = Color(0xFF22C55E), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceColor)
                        .clickable { onNavigateToLocalAudio() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YouTubeRed.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Yerel Klasörler", tint = YouTubeRed)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cihaz Klasörleri (SAF)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Yerel ses dosyalarını Room DB ile senkronize et", color = TextSecondary, fontSize = 13.sp)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
                }
            }
            // Favoriler / Beğenilenler
            item {
                var isExpanded by remember { mutableStateOf(false) }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favoriler", tint = YouTubeRed)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Beğenilen Şarkılar", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("${favorites.size} Şarkı", color = TextSecondary, fontSize = 13.sp)
                        }
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                    if (isExpanded) {
                        if (favorites.isEmpty()) {
                            Text("Henüz beğenilen şarkı yok.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                        } else {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                favorites.forEach { song ->
                                    LibraryItem(
                                        title = song.title,
                                        subtitle = song.artist,
                                        thumbnailUrl = song.thumbnail,
                                        onClick = { PlayerStateHolder.playSong(song, favorites) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dinleme Geçmişi
            item {
                var isExpanded by remember { mutableStateOf(false) }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Dinleme Geçmişi", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dinleme Geçmişi", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("${history.size} Şarkı", color = TextSecondary, fontSize = 13.sp)
                        }
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                    if (isExpanded) {
                        if (history.isEmpty()) {
                            Text("Dinleme geçmişi boş.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                        } else {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                history.forEach { song ->
                                    LibraryItem(
                                        title = song.title,
                                        subtitle = song.artist,
                                        thumbnailUrl = song.thumbnail,
                                        onClick = { PlayerStateHolder.playSong(song, history) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Çalma Listeleri Başlık ve Ekleme Butonu
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Çalma Listelerim", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showCreatePlaylistDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Yeni Çalma Listesi", tint = YouTubeRed, modifier = Modifier.size(28.dp))
                    }
                }
            }

            if (playlists.isEmpty()) {
                item {
                    Text("Henüz oluşturulmuş bir çalma listeniz yok. + butonuna basarak oluşturabilirsiniz.", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                items(playlists) { plWithSongs ->
                    var isExpanded by remember { mutableStateOf(false) }
                    val songModels = plWithSongs.songs.map { s ->
                        SongModel(s.id, s.title, s.artist, s.album, s.thumbnail, s.duration, s.provider, s.sourceId, s.streamAvailable)
                    }

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plWithSongs.playlist.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("${songModels.size} Şarkı", color = TextSecondary, fontSize = 13.sp)
                            }
                            if (songModels.isNotEmpty()) {
                                IconButton(onClick = { PlayerStateHolder.playSong(songModels.first(), songModels) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Tümünü Çal", tint = YouTubeRed)
                                }
                            }
                        }

                        if (isExpanded && songModels.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                songModels.forEach { song ->
                                    LibraryItem(
                                        title = song.title,
                                        subtitle = song.artist,
                                        thumbnailUrl = song.thumbnail,
                                        onClick = { PlayerStateHolder.playSong(song, songModels) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Profil Oluştur Dialog
    if (showCreateProfileDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = { Text("Profil Adı", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Adınız", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = SurfaceVariantColor
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.createProfile(name)
                        showCreateProfileDialog = false
                    }
                }) {
                    Text("Kaydet", color = YouTubeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProfileDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor
        )
    }

    // Yeni Çalma Listesi Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Yeni Çalma Listesi Oluştur", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Liste Adı", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = SurfaceVariantColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        viewModel.createNewPlaylist(newPlaylistName)
                        newPlaylistName = ""
                        showCreatePlaylistDialog = false
                    }
                }) {
                    Text("Oluştur", color = YouTubeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor
        )
    }
}
