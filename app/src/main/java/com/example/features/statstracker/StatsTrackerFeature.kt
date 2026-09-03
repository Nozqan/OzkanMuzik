package com.example.features.statstracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun StatsTrackerScreen(historySongs: List<SongModel>, onBack: () -> Unit = {}) {
    val totalMinutes = historySongs.size * 3.4f
    val topArtist = historySongs.groupBy { it.artist }.maxByOrNull { it.value.size }?.key ?: "Belirlenmedi"
    val streakDays = 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dinleme İstatistikleri & Özet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .padding(14.dp)
            ) {
                Column {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${totalMinutes.toInt()} Dk", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Toplam Dinleme", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .padding(14.dp)
            ) {
                Column {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFFF8C00), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${historySongs.size}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Çalınan Şarkı", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor)
                    .padding(14.dp)
            ) {
                Column {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$streakDays Gün", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Seri Dinleme", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Top Artist Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("En Çok Dinlenen Sanatçı", color = TextSecondary, fontSize = 12.sp)
                    Text(topArtist, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("1 Numaralı Favori", color = YouTubeRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Son Dinlenen Parçalar Analizi", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historySongs.take(15)) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceColor)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(song.artist, color = TextSecondary, fontSize = 12.sp)
                    }
                    Text("3:40 dk", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}
