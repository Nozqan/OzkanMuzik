package com.example.features.offlinecache

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.repository.MusicRepository
import com.example.domain.model.SongModel
import com.example.presentation.home.LibraryItem
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor


import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@Composable
fun LocalAudioScreen(repository: MusicRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val localSongs by repository.localSongs.collectAsState(initial = emptyList())
    var isScanning by remember { mutableStateOf(false) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            coroutineScope.launch {
                isScanning = true
                try {
                    repository.syncLocalFolder(context, uri)
                    Toast.makeText(context, "Klasör başarıyla tarandı", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Klasör tarama hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isScanning = false
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            coroutineScope.launch {
                isScanning = true
                try {
                    repository.syncLocalMusic(context)
                    Toast.makeText(context, "Yerel müzikler başarıyla tarandı ve Room veritabanına indekslendi", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Tarama hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isScanning = false
                }
            }
        } else {
            Toast.makeText(context, "Yerel müzik taraması için depolama izni gerekiyor", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            coroutineScope.launch {
                isScanning = true
                try {
                    repository.syncLocalMusic(context)
                } finally {
                    isScanning = false
                }
            }
        }
    }

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
                Icon(Icons.Default.Storage, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yerel Ses Dosyaları (Room DB)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        coroutineScope.launch {
                            isScanning = true
                            try {
                                repository.syncLocalMusic(context)
                                Toast.makeText(context, "Tarama tamamlandı", Toast.LENGTH_SHORT).show()
                            } finally {
                                isScanning = false
                            }
                        }
                    } else {
                        launcher.launch(permission)
                    }
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Yeniden Tara", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text("Çevrimdışı Depolama İndeksi", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cihazınızda bulunan ses dosyaları Room SQLite veritabanında saklanır ve internetsiz oynatılır.", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Toplam İndekslenen: ${localSongs.size} parça", color = YouTubeRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { folderLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Özel Klasör Seç (SAF)", color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YouTubeRed)
            }
        } else if (localSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cihazda ses dosyası bulunamadı veya izin verilmedi.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { launcher.launch(permission) },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Text("Depolama İznini Ver ve Tara", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(localSongs) { song ->
                    LibraryItem(
                        title = song.title,
                        subtitle = "${song.artist} • ${song.album ?: "Yerel Depolama"}",
                        thumbnailUrl = song.thumbnail,
                        onClick = { PlayerStateHolder.playSong(song, localSongs) }
                    )
                }
            }
        }
    }
}
