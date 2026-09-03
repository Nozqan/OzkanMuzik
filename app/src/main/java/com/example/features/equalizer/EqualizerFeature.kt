package com.example.features.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
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
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object EqualizerManager {
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled = _isEnabled.asStateFlow()

    private val _bassBoost = MutableStateFlow(60f)
    val bassBoost = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(40f)
    val virtualizer = _virtualizer.asStateFlow()

    // 5-Band Equalizer (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz) in range -10 to +10 dB
    private val _bands = MutableStateFlow(listOf(4f, 2f, 0f, 3f, 5f))
    val bands = _bands.asStateFlow()

    fun setEnabled(enabled: Boolean) { _isEnabled.value = enabled }
    fun setBassBoost(value: Float) { _bassBoost.value = value }
    fun setVirtualizer(value: Float) { _virtualizer.value = value }
    fun setBand(index: Int, value: Float) {
        val current = _bands.value.toMutableList()
        if (index in current.indices) {
            current[index] = value
            _bands.value = current
        }
    }

    fun applyPreset(presetName: String) {
        when (presetName) {
            "Düz (Flat)" -> _bands.value = listOf(0f, 0f, 0f, 0f, 0f)
            "Bas Güçlendirici" -> {
                _bands.value = listOf(8f, 5f, 1f, 0f, 0f)
                _bassBoost.value = 90f
            }
            "Rock & Metal" -> _bands.value = listOf(6f, 3f, -1f, 4f, 7f)
            "Pop" -> _bands.value = listOf(2f, 4f, 5f, 3f, 2f)
            "Vokal & Akustik" -> _bands.value = listOf(-2f, 1f, 6f, 5f, 3f)
            "Elektronik" -> {
                _bands.value = listOf(7f, 4f, 0f, 5f, 8f)
                _virtualizer.value = 75f
            }
        }
    }
}

@Composable
fun EqualizerScreen(onBack: () -> Unit = {}) {
    val isEnabled by EqualizerManager.isEnabled.collectAsState()
    val bassBoost by EqualizerManager.bassBoost.collectAsState()
    val virtualizer by EqualizerManager.virtualizer.collectAsState()
    val bands by EqualizerManager.bands.collectAsState()

    val presets = listOf("Düz (Flat)", "Bas Güçlendirici", "Rock & Metal", "Pop", "Vokal & Akustik", "Elektronik")
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

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
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ekolayzer & Ses Ayarları", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { EqualizerManager.setEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = YouTubeRed)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Selector
        Text("Hazır Önayarlar (Presets)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceColor)
                        .clickable { EqualizerManager.applyPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(preset, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5-Band Sliders
        Text("Frekans Bantları (-10dB / +10dB)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bands.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${if (value > 0) "+" else ""}${value.toInt()}dB",
                        color = if (value != 0f) YouTubeRed else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = value,
                        onValueChange = { EqualizerManager.setBand(index, it) },
                        valueRange = -10f..10f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed),
                        modifier = Modifier.height(140.dp)
                    )
                    Text(bandLabels.getOrElse(index) { "" }, color = TextSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bass Boost & Virtualizer
        Text("Özel Efektler", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bas Güçlendirici (Bass Boost)", color = TextPrimary, fontSize = 13.sp)
                    Text("${bassBoost.toInt()}%", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = bassBoost,
                    onValueChange = { EqualizerManager.setBassBoost(it) },
                    valueRange = 0f..100f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("3D Sanallaştırıcı (Virtualizer)", color = TextPrimary, fontSize = 13.sp)
                    Text("${virtualizer.toInt()}%", color = YouTubeRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = virtualizer,
                    onValueChange = { EqualizerManager.setVirtualizer(it) },
                    valueRange = 0f..100f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )
            }
        }
    }
}
