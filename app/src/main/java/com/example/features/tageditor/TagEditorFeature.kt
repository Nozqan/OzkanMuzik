package com.example.features.tageditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SongModel
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun TagEditorScreen(song: SongModel?, onSave: (SongModel) -> Unit = {}, onBack: () -> Unit = {}) {
    var title by remember { mutableStateOf(song?.title ?: "") }
    var artist by remember { mutableStateOf(song?.artist ?: "") }
    var album by remember { mutableStateOf(song?.album ?: "") }
    var genre by remember { mutableStateOf("Pop / Türkçe") }
    var year by remember { mutableStateOf("2024") }
    var isSaved by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.Edit, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Etiket & Metadata Düzenleyici", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it; isSaved = false },
            label = { Text("Şarkı Adı", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = YouTubeRed,
                unfocusedBorderColor = SurfaceVariantColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = artist,
            onValueChange = { artist = it; isSaved = false },
            label = { Text("Sanatçı", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = YouTubeRed,
                unfocusedBorderColor = SurfaceVariantColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = album,
            onValueChange = { album = it; isSaved = false },
            label = { Text("Albüm", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = YouTubeRed,
                unfocusedBorderColor = SurfaceVariantColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = genre,
                onValueChange = { genre = it; isSaved = false },
                label = { Text("Tür", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = YouTubeRed,
                    unfocusedBorderColor = SurfaceVariantColor
                )
            )

            OutlinedTextField(
                value = year,
                onValueChange = { year = it; isSaved = false },
                label = { Text("Yıl", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = YouTubeRed,
                    unfocusedBorderColor = SurfaceVariantColor
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (song != null) {
                    val updated = song.copy(title = title, artist = artist, album = album)
                    onSave(updated)
                    isSaved = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(if (isSaved) Icons.Default.Check else Icons.Default.Save, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSaved) "Etiketler Güncellendi!" else "Bilgileri Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
