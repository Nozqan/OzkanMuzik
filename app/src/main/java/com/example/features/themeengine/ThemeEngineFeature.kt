package com.example.features.themeengine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
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
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

data class ThemeColorPalette(
    val name: String,
    val primaryColor: Color,
    val gradient: List<Color>
)

@Composable
fun ThemeEngineScreen(onBack: () -> Unit = {}) {
    var selectedTheme by remember { mutableStateOf("YouTube Kırmızı & Gece") }
    var pureOledBlack by remember { mutableStateOf(true) }

    val themes = listOf(
        ThemeColorPalette("YouTube Kırmızı & Gece", YouTubeRed, listOf(Color(0xFFE50914), Color(0xFF030303))),
        ThemeColorPalette("Cyberpunk Neon Mor", Color(0xFF9D00FF), listOf(Color(0xFF9D00FF), Color(0xFF00E5FF))),
        ThemeColorPalette("Zümrüt Yeşil", Color(0xFF10B981), listOf(Color(0xFF10B981), Color(0xFF047857))),
        ThemeColorPalette("Gün Batımı Turuncu", Color(0xFFFF8C00), listOf(Color(0xFFFF8C00), Color(0xFFEA580C))),
        ThemeColorPalette("Okyanus Mavisi", Color(0xFF0284C7), listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tema & Renk Paleti Motoru", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saf OLED Siyah Modu (#000000)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("AMOLED ekranlarda maksimum pil tasarrufu sağlar.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = pureOledBlack,
                        onCheckedChange = { pureOledBlack = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = YouTubeRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Vurgu Renkleri & Temalar", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            themes.forEach { theme ->
                val isSelected = theme.name == selectedTheme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceColor)
                        .clickable { selectedTheme = theme.name }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(theme.gradient))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(theme.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = "Seçildi", tint = YouTubeRed)
                    }
                }
            }
        }
    }
}
