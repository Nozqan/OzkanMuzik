package com.example.presentation.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.repository.MusicRepository
import com.example.domain.model.SongModel
import com.example.presentation.home.LibraryItem
import com.example.presentation.player.PlayerStateHolder
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ExploreCategory(
    val title: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val query: String
)

@Composable
fun ExploreScreen(navController: NavController, repository: MusicRepository) {
    val coroutineScope = rememberCoroutineScope()
    var selectedGenreQuery by remember { mutableStateOf<String?>(null) }
    var genreSongs by remember { mutableStateOf<List<SongModel>>(emptyList()) }
    var isLoadingGenre by remember { mutableStateOf(false) }

    val categories = listOf(
        ExploreCategory("Yeni Çıkanlar", Icons.Default.NewReleases, listOf(Color(0xFFE50914), Color(0xFF8B0000)), "new release"),
        ExploreCategory("En Çok Dinlenenler", Icons.Default.TrendingUp, listOf(Color(0xFFFF8C00), Color(0xFFCC5500)), "trending"),
        ExploreCategory("Ruh Halleri & Modlar", Icons.Default.Mood, listOf(Color(0xFF8A2BE2), Color(0xFF4B0082)), "chill relaxing"),
        ExploreCategory("Hip-Hop & Rap", Icons.Default.Album, listOf(Color(0xFF2E8B57), Color(0xFF006400)), "hip hop rap"),
        ExploreCategory("Pop & Dans", Icons.Default.MusicNote, listOf(Color(0xFF1E90FF), Color(0xFF00008B)), "pop dance"),
        ExploreCategory("Rock & Metal", Icons.Default.ElectricBolt, listOf(Color(0xFFB22222), Color(0xFF4A0E17)), "rock metal"),
        ExploreCategory("Elektronik & EDM", Icons.Default.GraphicEq, listOf(Color(0xFF20B2AA), Color(0xFF008B8B)), "electronic edm"),
        ExploreCategory("Akustik & Huzur", Icons.Default.Spa, listOf(Color(0xFF9370DB), Color(0xFF483D8B)), "acoustic peaceful")
    )

    fun fetchGenreSongs(query: String) {
        selectedGenreQuery = query
        isLoadingGenre = true
        coroutineScope.launch {
            val result = repository.search(query)
            genreSongs = result.getOrNull() ?: emptyList()
            isLoadingGenre = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Keşfet",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick Explore Buttons (YouTube Music Style Top Pills)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Pair("Yeni Çıkanlar", Icons.Default.NewReleases),
                    Pair("Listeler", Icons.Default.BarChart),
                    Pair("Ruh Halleri", Icons.Default.Mood)
                ).forEach { item ->
                    Button(
                        onClick = { fetchGenreSongs(item.first) },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.second, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.first, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Category Cards Grid
        item {
            Text(
                text = "Türler ve Ruh Halleri",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.chunked(2).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowList.forEach { category ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.horizontalGradient(category.gradient))
                                    .clickable { fetchGenreSongs(category.query) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Display Genre Songs if user selected a category
        if (selectedGenreQuery != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sonuçlar: ${selectedGenreQuery?.uppercase()}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (genreSongs.isNotEmpty()) {
                        Text(
                            text = "Tümünü Çal",
                            color = YouTubeRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    PlayerStateHolder.playSong(genreSongs.first(), genreSongs)
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (isLoadingGenre) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = YouTubeRed)
                    }
                }
            } else if (genreSongs.isEmpty()) {
                item {
                    Text("Bu türe ait şarkı bulunamadı.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                items(genreSongs) { song ->
                    LibraryItem(
                        title = song.title,
                        subtitle = song.artist,
                        thumbnailUrl = song.thumbnail,
                        hasIndicator = false,
                        onClick = { PlayerStateHolder.playSong(song, genreSongs) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
