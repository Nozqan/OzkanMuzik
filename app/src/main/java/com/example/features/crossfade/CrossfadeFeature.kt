package com.example.features.crossfade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.service.CrossfadeCurve
import com.example.player.service.CrossfadeFeature
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun CrossfadeScreen(onBack: () -> Unit = {}) {
    val crossfadeFeature = remember { PlayerStateHolder.crossfadeFeature }
    val coroutineScope = rememberCoroutineScope()

    val isCrossfadeEnabled by crossfadeFeature.isEnabled.collectAsState()
    val crossfadeSeconds by crossfadeFeature.durationSeconds.collectAsState()
    val gaplessPlayback by crossfadeFeature.gaplessPlayback.collectAsState()
    val selectedCurve by crossfadeFeature.selectedCurve.collectAsState()
    val isTransitioning by crossfadeFeature.isTransitioning.collectAsState()
    val transitionProgress by crossfadeFeature.transitionProgress.collectAsState()
    val fadingOutTitle by crossfadeFeature.fadingOutTitle.collectAsState()
    val fadingInTitle by crossfadeFeature.fadingInTitle.collectAsState()

    val presets = listOf(2f, 4f, 6f, 8f, 12f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Kesintisiz Çalma & Crossfade",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Şarkılar arası akıcı ses geçişi",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Switch(
                    checked = isCrossfadeEnabled,
                    onCheckedChange = { crossfadeFeature.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = YouTubeRed
                    )
                )
            }
        }

        // Live Transition Visualizer Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isTransitioning) YouTubeRed.copy(alpha = 0.25f) else SurfaceColor,
                                SurfaceColor
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (isTransitioning) YouTubeRed.copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Canlı Geçiş Simülatörü",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isTransitioning) {
                            Text(
                                "GEÇİŞ YAPILIYOR: %${(transitionProgress * 100).toInt()}",
                                color = YouTubeRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Beklemede",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Overlapping Visualizer Bars
                    val (volOut, volIn) = crossfadeFeature.calculateVolumes(
                        if (isTransitioning) transitionProgress else 0.5f,
                        selectedCurve
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Song A Volume Bar (Fading Out)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    fadingOutTitle ?: "1. Şarkı (Çıkış - Fade Out)",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    "%${(volOut * 100).toInt()}",
                                    color = if (isTransitioning) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (isTransitioning) volOut else 1.0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = YouTubeRed,
                                trackColor = DarkSpaceBackground
                            )
                        }

                        // Song B Volume Bar (Fading In)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    fadingInTitle ?: "2. Şarkı (Giriş - Fade In)",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    "%${(volIn * 100).toInt()}",
                                    color = if (isTransitioning) YouTubeRed else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (isTransitioning) volIn else 0.0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonRed,
                                trackColor = DarkSpaceBackground
                            )
                        }
                    }

                    // Test Simulation Button
                    Button(
                        onClick = {
                            if (!isTransitioning) {
                                crossfadeFeature.simulateTestTransition(coroutineScope)
                            } else {
                                crossfadeFeature.cancelTransition()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTransitioning) SurfaceVariantColor else YouTubeRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isTransitioning) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isTransitioning) "Geçişi İptal Et" else "Crossfade Geçişini Test Et",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Duration Settings
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Şarkı Geçiş Süresi",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${crossfadeSeconds.toInt()} Saniye",
                            color = YouTubeRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = crossfadeSeconds,
                        onValueChange = { crossfadeFeature.setDurationSeconds(it) },
                        valueRange = 1f..12f,
                        enabled = isCrossfadeEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = YouTubeRed,
                            activeTrackColor = YouTubeRed,
                            inactiveTrackColor = DarkSpaceBackground
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { s ->
                            val isSelected = crossfadeSeconds.toInt() == s.toInt()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) YouTubeRed else DarkSpaceBackground)
                                    .clickable(enabled = isCrossfadeEnabled) {
                                        crossfadeFeature.setDurationSeconds(s)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${s.toInt()}s",
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Curve Type Selection
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Geçiş Eğrisi Modu",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    CrossfadeCurve.values().forEach { curve ->
                        val isSelected = selectedCurve == curve
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkSpaceBackground else Color.Transparent)
                                .clickable(enabled = isCrossfadeEnabled) {
                                    crossfadeFeature.setCurve(curve)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { crossfadeFeature.setCurve(curve) },
                                enabled = isCrossfadeEnabled,
                                colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    curve.label,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    curve.description,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gapless Playback Switch
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Boşluksuz Çalma (Gapless Playback)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Canlı konser albümlerinde parçalar arası sessizlik duraklamalarını kaldırır.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = gaplessPlayback,
                        onCheckedChange = { crossfadeFeature.setGaplessPlayback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = YouTubeRed
                        )
                    )
                }
            }
        }
    }
}
