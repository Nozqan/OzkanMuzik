package com.example.features.carmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun CarModeScreen(onExit: () -> Unit = {}) {
    val currentSong by PlayerStateHolder.currentSong.collectAsState()
    val isPlaying by PlayerStateHolder.isPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Car Mode Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("SÜRÜŞ MODU", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "Çıkış", tint = TextPrimary, modifier = Modifier.size(32.dp))
            }
        }

        // Giant Track Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentSong?.title ?: "Şarkı Çalmıyor",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = currentSong?.artist ?: "AKREP MÜZİK",
                color = TextSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        // Ultra-Large Touch Targets for Safety
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { PlayerStateHolder.skipPrevious() },
                modifier = Modifier
                    .size(80.dp)
                    .background(SurfaceColor, CircleShape)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Önceki", tint = TextPrimary, modifier = Modifier.size(48.dp))
            }

            IconButton(
                onClick = { PlayerStateHolder.togglePlayPause() },
                modifier = Modifier
                    .size(100.dp)
                    .background(YouTubeRed, CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Oynat/Durdur",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            IconButton(
                onClick = { PlayerStateHolder.skipNext() },
                modifier = Modifier
                    .size(80.dp)
                    .background(SurfaceColor, CircleShape)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Sonraki", tint = TextPrimary, modifier = Modifier.size(48.dp))
            }
        }

        // Quick Drive Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { PlayerStateHolder.toggleShuffle() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = YouTubeRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Karıştır", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { PlayerStateHolder.toggleFavorite() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = YouTubeRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Beğen", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
