package com.example.features.volumenormalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
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
fun VolumeNormalizerScreen(onBack: () -> Unit = {}) {
    var isNormalizerActive by remember { mutableStateOf(true) }
    var targetLoudnessDb by remember { mutableStateOf(-14f) }
    var peakLimiter by remember { mutableStateOf(true) }

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
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Otomatik Ses Dengeleme (ReplayGain)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Switch(
                checked = isNormalizerActive,
                onCheckedChange = { isNormalizerActive = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = YouTubeRed)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Akıllı Ses Seviyesi Kontrolü", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Farklı kayıt kalitesine veya ses şiddetine sahip parçalar arasındaki ani ses patlamalarını önler ve tüm parçaları eşit seviyede çalar.", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hedef Ses Şiddeti (LUFS)", color = TextPrimary, fontSize = 13.sp)
                    Text("${targetLoudnessDb.toInt()} LUFS", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = targetLoudnessDb,
                    onValueChange = { targetLoudnessDb = it },
                    valueRange = -23f..-9f,
                    enabled = isNormalizerActive,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tepe Noktası Sınırlayıcı (Anti-Clipping)", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = peakLimiter,
                        onCheckedChange = { peakLimiter = it },
                        enabled = isNormalizerActive,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = YouTubeRed)
                    )
                }
            }
        }
    }
}
