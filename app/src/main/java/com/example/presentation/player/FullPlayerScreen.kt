package com.example.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.di.DependencyProvider
import com.example.domain.model.SongModel
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch
import java.util.Locale

enum class PlayerViewTab {
    SONG, VIDEO, QUEUE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(onCollapse: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DependencyProvider.repository }
    
    val currentSong by PlayerStateHolder.currentSong.collectAsState()
    val isPlaying by PlayerStateHolder.isPlaying.collectAsState()
    val isBuffering by PlayerStateHolder.isBuffering.collectAsState()
    val currentPos by PlayerStateHolder.currentPositionMs.collectAsState()
    val duration by PlayerStateHolder.durationMs.collectAsState()
    val isShuffle by PlayerStateHolder.isShuffle.collectAsState()
    val repeatMode by PlayerStateHolder.repeatMode.collectAsState()
    val isRepeat by PlayerStateHolder.isRepeat.collectAsState()
    val playbackSpeed by PlayerStateHolder.playbackSpeed.collectAsState()
    val sleepTimerRemaining by PlayerStateHolder.sleepTimerRemainingMs.collectAsState()
    val currentPreset by PlayerStateHolder.currentEqualizerPreset.collectAsState()
    val playlist by PlayerStateHolder.playlist.collectAsState()

    var selectedTab by remember { mutableStateOf(PlayerViewTab.SONG) }
    var isFavorite by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val userPlaylists by (repository?.playlists?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    // Check favorite state
    LaunchedEffect(currentSong?.id) {
        currentSong?.let { song ->
            repository?.isFavorite(song.id)?.collect { fav ->
                isFavorite = fav
            }
        }
    }

    // Slider state for smooth user scrubbing
    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedPosition by remember { mutableFloatStateOf(0f) }

    val safeDuration = if (duration > 0) duration.toFloat() else 180000f
    val currentSliderValue = if (isDraggingSlider) {
        draggedPosition
    } else {
        currentPos.toFloat().coerceIn(0f, safeDuration)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar (YouTube Music Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Kapat",
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // YouTube Music Style: Song / Video / Queue Tab Switcher Pill
            Row(
                modifier = Modifier
                    .background(SurfaceColor, RoundedCornerShape(24.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == PlayerViewTab.SONG) SurfaceVariantColor else Color.Transparent)
                        .clickable { selectedTab = PlayerViewTab.SONG }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "ŞARKI",
                        color = if (selectedTab == PlayerViewTab.SONG) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == PlayerViewTab.SONG) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == PlayerViewTab.VIDEO) SurfaceVariantColor else Color.Transparent)
                        .clickable { selectedTab = PlayerViewTab.VIDEO }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "VİDEO",
                        color = if (selectedTab == PlayerViewTab.VIDEO) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == PlayerViewTab.VIDEO) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == PlayerViewTab.QUEUE) SurfaceVariantColor else Color.Transparent)
                        .clickable { selectedTab = PlayerViewTab.QUEUE }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "KUYRUK (${playlist.size})",
                        color = if (selectedTab == PlayerViewTab.QUEUE) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == PlayerViewTab.QUEUE) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Sleep Timer Button with active timer indicator
            IconButton(
                onClick = { showSleepTimerDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Uyku Zamanlayıcısı",
                    tint = if (sleepTimerRemaining > 0) YouTubeRed else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Content depending on selected tab
        when (selectedTab) {
            PlayerViewTab.SONG -> {
                // YouTube Music Style Square Album Artwork with 8dp-12dp rounded corners
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(elevation = 20.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentSong?.thumbnail.isNullOrBlank()) {
                        AsyncImage(
                            model = currentSong?.thumbnail,
                            contentDescription = currentSong?.title ?: "Albüm Kapağı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }
            }
            PlayerViewTab.VIDEO -> {
                // Video Mode: YouTube Stage View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        if (!currentSong?.thumbnail.isNullOrBlank()) {
                            AsyncImage(
                                model = currentSong?.thumbnail,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentSong?.title ?: "",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = currentSong?.artist ?: "",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            PlayerViewTab.QUEUE -> {
                // Queue List Mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor)
                        .padding(12.dp)
                ) {
                    if (playlist.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sırada parça yok", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(playlist) { index, song ->
                                val isCurrent = song.id == currentSong?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) SurfaceVariantColor else Color.Transparent)
                                        .clickable { PlayerStateHolder.playSong(song, playlist) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = if (isCurrent) YouTubeRed else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            song.title,
                                            color = if (isCurrent) YouTubeRed else TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            song.artist,
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { PlayerStateHolder.removeFromQueue(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Kuyruktan Çıkar",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Song Title, Artist Info, Favorite & Playlist Add Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong?.title ?: "Müzik Seçilmedi",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentSong?.artist ?: "Sanatçı",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Add to playlist button (+)
                IconButton(onClick = { showAddToPlaylistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Çalma Listesine Ekle",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Favorite / Like button
                IconButton(
                    onClick = {
                        currentSong?.let { song ->
                            coroutineScope.launch {
                                val newFav = !isFavorite
                                isFavorite = newFav
                                repository?.toggleFavorite(song, newFav)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorilere Ekle",
                        tint = if (isFavorite) YouTubeRed else TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar / Scrubbing Slider (YouTube Style: Smooth and full duration)
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentSliderValue,
                onValueChange = { newValue ->
                    isDraggingSlider = true
                    draggedPosition = newValue
                },
                onValueChangeFinished = {
                    PlayerStateHolder.seekTo(draggedPosition.toLong())
                    isDraggingSlider = false
                },
                valueRange = 0f..safeDuration.coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentSliderValue.toLong()),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(safeDuration.toLong()),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // YouTube Music Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(
                onClick = { PlayerStateHolder.toggleShuffle() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isShuffle) SurfaceVariantColor else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = if (isShuffle) "Karışık Çalma Açık" else "Karışık Çalma Kapalı",
                    tint = if (isShuffle) YouTubeRed else TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous
            IconButton(onClick = { PlayerStateHolder.skipPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Önceki",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play / Pause (YouTube Music Signature Big White Button)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { PlayerStateHolder.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.Black,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Durdur" else "Oynat",
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Next
            IconButton(onClick = { PlayerStateHolder.skipNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Sonraki",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat Button with 3-State cycling (OFF, ALL, ONE)
            IconButton(
                onClick = { PlayerStateHolder.toggleRepeat() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (repeatMode != RepeatMode.OFF) SurfaceVariantColor else Color.Transparent)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = when (repeatMode) {
                        RepeatMode.ONE -> "Tek Şarkı Tekrarı"
                        RepeatMode.ALL -> "Tüm Liste Tekrarı"
                        RepeatMode.OFF -> "Tekrar Kapalı"
                    },
                    tint = if (repeatMode != RepeatMode.OFF) YouTubeRed else TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Action Bar: Speed selector, Equalizer presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Chip
            Button(
                onClick = { showSpeedDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${playbackSpeed}x", color = TextPrimary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Equalizer Chip
            Button(
                onClick = { showEqualizerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Equalizer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(currentPreset, color = TextPrimary, fontSize = 12.sp)
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Uyku Zamanlayıcısı", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sleepTimerRemaining > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Aktif Zamanlayıcı:", color = TextSecondary, fontSize = 12.sp)
                                Text("${formatTime(sleepTimerRemaining)} sonra duracak", color = YouTubeRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                PlayerStateHolder.cancelSleepTimer()
                                showSleepTimerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Zamanlayıcıyı İptal Et", color = Color.White)
                        }
                    }

                    listOf(15, 30, 45, 60, 90).forEach { minutes ->
                        TextButton(
                            onClick = {
                                PlayerStateHolder.startSleepTimer(minutes)
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$minutes Dakika sonra durdur", color = TextPrimary, textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Kapat", color = Color.White)
                }
            },
            containerColor = SurfaceColor
        )
    }

    // Speed Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Oynatma Hızı", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        TextButton(
                            onClick = {
                                PlayerStateHolder.setPlaybackSpeed(speed)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                color = if (playbackSpeed == speed) YouTubeRed else TextPrimary,
                                fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Kapat", color = Color.White)
                }
            },
            containerColor = SurfaceColor
        )
    }

    // Equalizer Presets Dialog
    if (showEqualizerDialog) {
        AlertDialog(
            onDismissRequest = { showEqualizerDialog = false },
            title = { Text("Ekolayzır & Ses Profili", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Varsayılan", "Bass Güçlendirici", "Vokal Netliği", "Akustik / Canlı", "Rock / Sahne", "Club / Elektronik").forEach { preset ->
                        TextButton(
                            onClick = {
                                PlayerStateHolder.setEqualizerPreset(preset)
                                showEqualizerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                preset,
                                color = if (currentPreset == preset) YouTubeRed else TextPrimary,
                                fontWeight = if (currentPreset == preset) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEqualizerDialog = false }) {
                    Text("Kapat", color = Color.White)
                }
            },
            containerColor = SurfaceColor
        )
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog) {
        var newPlaylistName by remember { mutableStateOf("") }
        var isCreatingNew by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = { Text("Çalma Listesine Ekle", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isCreatingNew) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            label = { Text("Yeni Liste Adı", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = YouTubeRed,
                                unfocusedBorderColor = SurfaceVariantColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank() && currentSong != null) {
                                    coroutineScope.launch {
                                        repository?.createPlaylist(newPlaylistName)
                                        showAddToPlaylistDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Oluştur ve Ekle", color = Color.White)
                        }
                    } else {
                        if (userPlaylists.isEmpty()) {
                            Text("Henüz bir çalma listeniz yok.", color = TextSecondary)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(userPlaylists.size) { i ->
                                    val pl = userPlaylists[i]
                                    TextButton(
                                        onClick = {
                                            currentSong?.let { song ->
                                                coroutineScope.launch {
                                                    repository?.addSongToPlaylist(pl.playlist.playlistId, song)
                                                    showAddToPlaylistDialog = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(pl.playlist.name, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { isCreatingNew = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yeni Liste Oluştur", color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("İptal", color = Color.White)
                }
            },
            containerColor = SurfaceColor
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
