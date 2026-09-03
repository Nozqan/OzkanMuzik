package com.example.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SongModel
import com.example.features.audioconverter.AudioConverterScreen
import com.example.features.backuprestore.BackupRestoreScreen
import com.example.features.carmode.CarModeScreen
import com.example.features.crossfade.CrossfadeScreen
import com.example.features.equalizer.EqualizerScreen
import com.example.features.folderbrowser.FolderBrowserScreen
import com.example.features.gesturecontrols.GestureControlsScreen
import com.example.features.lyrics.LyricsScreen
import com.example.features.offlinecache.OfflineCacheScreen
import com.example.features.offlinecache.LocalAudioScreen
import com.example.features.radiostation.RadioStationScreen
import com.example.features.ringtonecutter.RingtoneCutterScreen
import com.example.features.sleeptimer.SleepTimerScreen
import com.example.features.smartmix.SmartMixScreen
import com.example.features.soundeffects.SoundEffectsScreen
import com.example.features.speedpitch.SpeedPitchScreen
import com.example.features.statstracker.StatsTrackerScreen
import com.example.features.tageditor.TagEditorScreen
import com.example.features.themeengine.ThemeEngineScreen
import com.example.features.visualizer.VisualizerScreen
import com.example.features.volumenormalizer.VolumeNormalizerScreen
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

data class FeatureItem(
    val id: Int,
    val title: String,
    val category: String,
    val icon: ImageVector,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureHubScreen(
    allSongs: List<SongModel>,
    onBack: () -> Unit = {}
) {
    var activeFeatureId by remember { mutableStateOf<Int?>(null) }
    val currentSong by PlayerStateHolder.currentSong.collectAsState()

    val featuresList = remember {
        listOf(
            FeatureItem(1, "Şarkı Sözleri (LRC)", "Ses & Dinleme", Icons.Default.Mic, "Canlı"),
            FeatureItem(2, "10-Bant Ekolayzer", "Ses & Dinleme", Icons.Default.GraphicEq, "M3"),
            FeatureItem(3, "Akıllı Karışım", "Çalma & Listeler", Icons.Default.AutoAwesome, "AI"),
            FeatureItem(4, "Uyku Zamanlayıcı", "Oynatma", Icons.Default.Bedtime),
            FeatureItem(5, "Etiket Düzenleyici", "Dosya & Araç", Icons.Default.Edit),
            FeatureItem(6, "Ses Görselleştirici", "Görsel", Icons.Default.BarChart, "Canlı"),
            FeatureItem(7, "Çevrimdışı Önbellek", "Depolama", Icons.Default.OfflinePin),
            FeatureItem(8, "Zil Sesi Kırpıcı", "Dosya & Araç", Icons.Default.ContentCut),
            FeatureItem(9, "Hız & Ton Değiştirici", "Ses & Dinleme", Icons.Default.Speed),
            FeatureItem(10, "3D Ses & Akustik", "Ses & Dinleme", Icons.Default.SurroundSound),
            FeatureItem(11, "Sanatçı Radyosu", "Çalma & Listeler", Icons.Default.Radio),
            FeatureItem(12, "Ses Dönüştürücü", "Dosya & Araç", Icons.Default.Transform),
            FeatureItem(13, "Dinleme İstatistikleri", "Kişisel", Icons.Default.Equalizer),
            FeatureItem(14, "Hareket Kontrolleri", "Kullanılabilirlik", Icons.Default.Gesture),
            FeatureItem(15, "Sürüş Modu (Car Mode)", "Kullanılabilirlik", Icons.Default.DirectionsCar, "Büyük"),
            FeatureItem(16, "Klasör Tarayıcı", "Depolama", Icons.Default.FolderOpen),
            FeatureItem(17, "Yedekle & Geri Yükle", "Kişisel", Icons.Default.Restore),
            FeatureItem(18, "Ses Dengeleme (Gain)", "Ses & Dinleme", Icons.Default.VolumeUp),
            FeatureItem(19, "Crossfade & Gapless", "Oynatma", Icons.Default.SwapHoriz),
            FeatureItem(20, "Tema & OLED Motoru", "Görsel", Icons.Default.Palette, "OLED"),
            FeatureItem(21, "Yerel Ses İndeksleyici (Room)", "Depolama", Icons.Default.Storage, "Offline")
        )
    }

    if (activeFeatureId != null) {
        Column(modifier = Modifier.fillMaxSize().background(DarkSpaceBackground)) {
            // Header for active sub-feature
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeFeatureId = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Özellikler & Araçlar Menüsü", color = TextSecondary, fontSize = 13.sp)
            }

            Box(modifier = Modifier.weight(1f)) {
                when (activeFeatureId) {
                    1 -> LyricsScreen(song = currentSong ?: allSongs.firstOrNull())
                    2 -> EqualizerScreen()
                    3 -> SmartMixScreen()
                    4 -> SleepTimerScreen()
                    5 -> TagEditorScreen(song = currentSong ?: allSongs.firstOrNull())
                    6 -> VisualizerScreen()
                    7 -> OfflineCacheScreen(availableSongs = allSongs)
                    8 -> RingtoneCutterScreen(song = currentSong ?: allSongs.firstOrNull())
                    9 -> SpeedPitchScreen()
                    10 -> SoundEffectsScreen()
                    11 -> RadioStationScreen(currentSong = currentSong, availableSongs = allSongs)
                    12 -> AudioConverterScreen(song = currentSong ?: allSongs.firstOrNull())
                    13 -> StatsTrackerScreen(historySongs = allSongs)
                    14 -> GestureControlsScreen()
                    15 -> CarModeScreen(onExit = { activeFeatureId = null })
                    16 -> FolderBrowserScreen(allSongs = allSongs)
                    17 -> BackupRestoreScreen()
                    18 -> VolumeNormalizerScreen()
                    19 -> CrossfadeScreen()
                    20 -> ThemeEngineScreen()
                    21 -> LocalAudioScreen(repository = com.example.di.DependencyProvider.repository!!)
                }
            }
        }
    } else {
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
                Icon(Icons.Default.Widgets, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("AKREP MÜZİK — 20 Özellik Merkezi", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Tüm gelişmiş müzik motoru araçları aktif", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(featuresList) { feature ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceColor)
                            .clickable { activeFeatureId = feature.id }
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.title,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(26.dp)
                                )
                                if (feature.badge != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(YouTubeRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(feature.badge, color = YouTubeRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = feature.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = feature.category,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
