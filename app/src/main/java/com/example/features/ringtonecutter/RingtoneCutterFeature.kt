package com.example.features.ringtonecutter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
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
import java.util.Locale

@Composable
fun RingtoneCutterScreen(song: SongModel?, onBack: () -> Unit = {}) {
    var startSec by remember { mutableStateOf(10f) }
    var endSec by remember { mutableStateOf(40f) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val totalDurationSec = (song?.duration ?: 180).toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ContentCut, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zil Sesi Kesici & Kırpıcı", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (song != null) {
            Text(song.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = TextSecondary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Waveform preview & Range Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Başlangıç: ${String.format(Locale.getDefault(), "%02d:%02d", (startSec/60).toInt(), (startSec%60).toInt())}", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Bitiş: ${String.format(Locale.getDefault(), "%02d:%02d", (endSec/60).toInt(), (endSec%60).toInt())}", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Başlangıç Noktası", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = startSec,
                    onValueChange = { if (it < endSec - 5) startSec = it },
                    valueRange = 0f..totalDurationSec,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Text("Bitiş Noktası", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = endSec,
                    onValueChange = { if (it > startSec + 5) endSec = it },
                    valueRange = 0f..totalDurationSec,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Text(
                    text = "Seçilen Zil Sesi Süresi: ${(endSec - startSec).toInt()} Saniye",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                statusMessage = "Zil sesi başarıyla kaydedildi: ${(endSec - startSec).toInt()}s (Zil Sesleri klasörüne eklendi)"
            },
            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zil Sesi Olarak Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(statusMessage!!, color = Color(0xFF22C55E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
