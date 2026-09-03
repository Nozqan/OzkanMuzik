package com.example.features.radiostation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
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

@Composable
fun RadioStationScreen(currentSong: SongModel?, availableSongs: List<SongModel>, onBack: () -> Unit = {}) {
    val seedArtist = currentSong?.artist ?: "Popüler Sanatçılar"
    val radioSongs = remember(currentSong, availableSongs) {
        if (availableSongs.isEmpty()) emptyList()
        else availableSongs.shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Radio, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sonsuz Sanatçı Radyosu", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Text("Radyo İstasyonu:", color = TextSecondary, fontSize = 12.sp)
                Text("$seedArtist & Benzer Sanatçılar Radyosu", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Müzik hiç durmadan benzer tarzda parçalarla akmaya devam eder.", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (radioSongs.isNotEmpty()) {
                    Button(
                        onClick = { PlayerStateHolder.playSong(radioSongs.first(), radioSongs) },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Radyoyu Başlat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Sıradaki Radyo Parçaları", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(radioSongs) { song ->
                LibraryItem(
                    title = song.title,
                    subtitle = song.artist,
                    thumbnailUrl = song.thumbnail,
                    onClick = { PlayerStateHolder.playSong(song, radioSongs) }
                )
            }
        }
    }
}
