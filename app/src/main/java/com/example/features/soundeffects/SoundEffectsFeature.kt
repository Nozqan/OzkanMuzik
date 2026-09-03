package com.example.features.soundeffects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun SoundEffectsScreen(onBack: () -> Unit = {}) {
    var selectedReverb by remember { mutableStateOf("Kapalı") }
    var surroundStrength by remember { mutableStateOf(50f) }
    var vocalClarity by remember { mutableStateOf(70f) }
    var concertHallMode by remember { mutableStateOf(false) }

    val reverbTypes = listOf("Kapalı", "Stüdyo", "Konser Salonu", "Katedral", "Oda Akustiği")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SurroundSound, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ses Efektleri & Akustik", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Reverb / Ortam Akustiği", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            reverbTypes.take(3).forEach { type ->
                val isSelected = type == selectedReverb
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) YouTubeRed else SurfaceColor)
                        .clickable { selectedReverb = type }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3D Surround & Derinlik", color = TextPrimary, fontSize = 13.sp)
                        Text("${surroundStrength.toInt()}%", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = surroundStrength,
                        onValueChange = { surroundStrength = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vokal Netliği & İzolasyon", color = TextPrimary, fontSize = 13.sp)
                        Text("${vocalClarity.toInt()}%", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = vocalClarity,
                        onValueChange = { vocalClarity = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Canlı Konser Atmosferi", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = concertHallMode,
                        onCheckedChange = { concertHallMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = YouTubeRed)
                    )
                }
            }
        }
    }
}
