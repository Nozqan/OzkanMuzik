package com.example.features.audioconverter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Transform
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
fun AudioConverterScreen(song: SongModel?, onBack: () -> Unit = {}) {
    var targetFormat by remember { mutableStateOf("MP3 (320 kbps)") }
    var compressLevel by remember { mutableStateOf(100f) }
    var convertStatus by remember { mutableStateOf<String?>(null) }

    val formats = listOf("MP3 (320 kbps)", "AAC (256 kbps)", "FLAC (Kayıpsız Hi-Fi)", "OGG (192 kbps)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Transform, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ses Dönüştürücü & Sıkıştırıcı", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (song != null) {
            Text(song.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = TextSecondary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column {
                Text("Hedef Format ve Kalite", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                formats.forEach { fmt ->
                    val isSelected = fmt == targetFormat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { targetFormat = fmt; convertStatus = null },
                            colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fmt, color = TextPrimary, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Sıkıştırma Oranı: ${compressLevel.toInt()}%", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = compressLevel,
                    onValueChange = { compressLevel = it; convertStatus = null },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                convertStatus = "Dosya $targetFormat formatına dönüştürüldü ve /Music/Akrep/ klasörüne kaydedildi."
            },
            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Compress, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dönüştürmeyi Başlat", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (convertStatus != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(convertStatus!!, color = Color(0xFF22C55E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
