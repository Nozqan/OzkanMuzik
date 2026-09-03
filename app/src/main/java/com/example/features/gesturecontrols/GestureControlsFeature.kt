package com.example.features.gesturecontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun GestureControlsScreen(onBack: () -> Unit = {}) {
    var doubleTapSeekEnabled by remember { mutableStateOf(true) }
    var swipeToSkipEnabled by remember { mutableStateOf(true) }
    var verticalVolumeGesture by remember { mutableStateOf(true) }
    var shakeToShuffle by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Gesture, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hareket & Dokunma Kontrolleri", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Çift Dokunma ile İleri/Geri Sar (10s)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Sol veya sağ kenara iki kez dokunulduğunda 10 saniye atlar.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = doubleTapSeekEnabled,
                        onCheckedChange = { doubleTapSeekEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = YouTubeRed)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kaydırarak Şarkı Değiştir (Swipe to Skip)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Kapak resmini sola veya sağa kaydırarak sonraki/önceki şarkıya geçin.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = swipeToSkipEnabled,
                        onCheckedChange = { swipeToSkipEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = YouTubeRed)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dikey Ses Seviyesi Hareketi", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Ekranın sağ kenarından yukarı-aşağı kaydırarak sesi ayarlayın.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = verticalVolumeGesture,
                        onCheckedChange = { verticalVolumeGesture = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = YouTubeRed)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sallayarak Karıştır (Shake to Shuffle)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Telefonu sallayarak rastgele yeni bir şarkıya geçin.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = shakeToShuffle,
                        onCheckedChange = { shakeToShuffle = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = YouTubeRed)
                    )
                }
            }
        }
    }
}
