package com.example.features.sleeptimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import java.util.Locale

@Composable
fun SleepTimerScreen(onDismiss: () -> Unit = {}) {
    val remainingMs by PlayerStateHolder.sleepTimerRemainingMs.collectAsState()
    val isTimerActive = remainingMs > 0

    val presetMinutes = listOf(5, 10, 15, 30, 45, 60, 90)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bedtime, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Uyku Zamanlayıcısı", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Timer Dial Display
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(SurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = if (isTimerActive) YouTubeRed else TextSecondary, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                if (isTimerActive) {
                    val minutes = (remainingMs / 1000) / 60
                    val seconds = (remainingMs / 1000) % 60
                    Text(
                        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Kalan Süre", color = YouTubeRed, fontSize = 12.sp)
                } else {
                    Text("Devre Dışı", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("Hızlı Süre Seçenekleri", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            presetMinutes.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { mins ->
                        Button(
                            onClick = { PlayerStateHolder.startSleepTimer(mins) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("$mins dk", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isTimerActive) {
            OutlinedButton(
                onClick = { PlayerStateHolder.cancelSleepTimer() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YouTubeRed),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zamanlayıcıyı İptal Et", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
