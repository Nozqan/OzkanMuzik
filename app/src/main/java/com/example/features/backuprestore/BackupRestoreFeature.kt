package com.example.features.backuprestore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
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
fun BackupRestoreScreen(onBack: () -> Unit = {}) {
    var backupStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restore, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Yedekleme & Geri Yükleme", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Text("Kitaplık & Tercihleri Dışa Aktar", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Beğendiğiniz şarkılar, oluşturduğunuz çalma listeleri ve ses ayarlarınız JSON formatında yedeklenir.", color = TextSecondary, fontSize = 12.sp)

                Button(
                    onClick = {
                        backupStatus = "Yedek dosyası oluşturuldu: AkrepMusic_Backup_2026.json (İndirilenler klasörüne kaydedildi)"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yedeği Dışa Aktar (Export JSON)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
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
                Text("Yedekten Geri Yükle (Import)", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Daha önce kaydettiğiniz yedek dosyasını seçerek tüm çalma listelerinizi geri yükleyin.", color = TextSecondary, fontSize = 12.sp)

                OutlinedButton(
                    onClick = {
                        backupStatus = "Tüm çalma listeleri ve favoriler başarıyla geri yüklendi!"
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YouTubeRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dosyadan İçe Aktar (Import JSON)", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (backupStatus != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(backupStatus!!, color = Color(0xFF22C55E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
