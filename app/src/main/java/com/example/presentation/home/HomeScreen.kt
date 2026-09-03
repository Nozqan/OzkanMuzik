package com.example.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.domain.model.SongModel
import com.example.presentation.player.PlayerStateHolder
import com.example.presentation.search.SearchState
import com.example.presentation.search.SearchViewModel
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    searchViewModel: SearchViewModel
) {
    val context = LocalContext.current
    val history by viewModel.history.collectAsState(initial = emptyList())
    val mostPlayed by viewModel.mostPlayed.collectAsState(initial = emptyList())
    val userProfileViewModel = remember { com.example.presentation.profile.UserProfileViewModel(context) }
    val userName by userProfileViewModel.userName.collectAsState(initial = "Müziksever")
    val userAvatarUri by userProfileViewModel.userAvatarUri.collectAsState(initial = null)
    val recommendations by viewModel.recommendations.collectAsState()
    val searchState by searchViewModel.state.collectAsState()
    val query by searchViewModel.query.collectAsState()
    
    var isSearchActive by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var profileNameInput by remember { mutableStateOf("") }
    var profileAvatarInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "Enerjik", "Rahatlama", "Odaklanma", "Parti", "Egzersiz", 
        "Yolculuk", "Pop", "Hip-Hop", "Rock", "Akustik"
    )
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Dynamic Quick Picks based on user's most played or searched/played history, blended with recommendations
    val quickPicks = remember(mostPlayed, history, recommendations) {
        val combinedHistory = (mostPlayed + history).distinctBy { it.id }.take(4)
        val combinedRecs = recommendations.filter { rec -> combinedHistory.none { it.id == rec.id } }.take(5)
        val combined = (combinedHistory + combinedRecs).shuffled()
        if (combined.isNotEmpty()) {
            combined.take(9)
        } else {
            recommendations.take(9)
        }
    }

    val featuredMixes = remember(mostPlayed) {
        listOf(
            Triple("Süper Karışım", "En Çok Dinlediklerinizden", listOf(Color(0xFFE53935), Color(0xFF8E24AA))),
            Triple("Enerji Karışımı", "Antrenman & Motivasyon", listOf(Color(0xFFFB8C00), Color(0xFFD81B60))),
            Triple("Odaklanma & Çalışma", "Deep Focus & Lofi", listOf(Color(0xFF1E88E5), Color(0xFF00ACC1))),
            Triple("Chill & Akustik", "Rahatlatıcı Tınılar", listOf(Color(0xFF43A047), Color(0xFF00897B)))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground)
            .verticalScroll(scrollState)
            .padding(top = 10.dp, start = 14.dp, end = 14.dp, bottom = 90.dp)
    ) {
        // TOP APP BAR: YouTube Music Red Logo + "Music" + Cast + Search + Profile Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo + App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { 
                    isSearchActive = false
                    selectedCategory = null
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(YouTubeRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AKREP MÜZİK",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
            
            // Right: Search Icon, Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Icon
                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // User Profile Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8E24AA), Color(0xFFD81B60))
                            )
                        )
                        .clickable {
                            profileNameInput = userName
                            profileAvatarInput = userAvatarUri ?: ""
                            showProfileDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!userAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = userAvatarUri,
                            contentDescription = "Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        if (isSearchActive || query.isNotBlank()) {
            TextField(
                value = query,
                onValueChange = { searchViewModel.onQueryChanged(it) },
                placeholder = { Text("Şarkı, sanatçı veya albüm ara...", color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { searchViewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceColor),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    disabledContainerColor = SurfaceColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = YouTubeRed
                ),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // CATEGORY PILLS (Moods & Activities)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color.White else Color(0xFF262626))
                        .clickable {
                            if (isSelected) {
                                selectedCategory = null
                                searchViewModel.onQueryChanged("")
                            } else {
                                selectedCategory = category
                                searchViewModel.onQueryChanged(category)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // SEARCH RESULTS VIEW
        if (query.isNotBlank()) {
            when (val state = searchState) {
                is SearchState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = YouTubeRed)
                    }
                }
                is SearchState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MusicOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sonuç bulunamadı", color = TextSecondary, fontSize = 15.sp)
                    }
                }
                is SearchState.Error -> {
                    Text("Hata: ${state.message}", color = YouTubeRed, modifier = Modifier.padding(16.dp))
                }
                is SearchState.Success -> {
                    SectionHeaderTitle("Arama Sonuçları (${state.results.size})")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.results.forEach { song ->
                            LibraryItem(
                                title = song.title, 
                                subtitle = song.artist, 
                                thumbnailUrl = song.thumbnail, 
                                onClick = { PlayerStateHolder.playSong(song, state.results) }
                            )
                        }
                    }
                }
                is SearchState.Idle -> {}
            }
        } else {
            // SECTION: Sizin İçin Önerilenler (Local Recommendation Engine)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Önerilenler & Hızlı Seçimler",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dinleme ve arama geçmişinizden özel olarak derlendi",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                    Text(
                        text = "Merhaba, $userName",
                        color = YouTubeRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3x3 Grid Layout for Quick Picks
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickPicks.chunked(3).forEach { rowSongs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowSongs.forEach { song ->
                                Box(modifier = Modifier.weight(1f)) {
                                    QuickPickSongCard(
                                        song = song,
                                        onClick = { PlayerStateHolder.playSong(song, quickPicks) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION: Sizin İçin Karışık (Curated Mix Cards)
            SectionHeaderWithAction("Sizin İçin Karışık") {
                PlayerStateHolder.playSong(quickPicks.first(), quickPicks)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(featuredMixes) { (title, subtitle, gradientColors) ->
                    Box(
                        modifier = Modifier
                            .width(170.dp)
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(gradientColors))
                            .clickable {
                                PlayerStateHolder.playSong(quickPicks.random(), quickPicks)
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp).align(Alignment.TopEnd)
                        )
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION: Popüler Parçalar & Trendler (Trending Hits)
            SectionHeaderWithAction("Popüler Parçalar & Trendler") {
                val list = if (recommendations.isNotEmpty()) recommendations else quickPicks
                PlayerStateHolder.playSong(list.first(), list)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalSongList(songs = if (recommendations.isNotEmpty()) recommendations else quickPicks)

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION: Yeniden Dinleyin (Recent History)
            if (history.isNotEmpty()) {
                SectionHeaderWithAction("Yeniden Dinleyin") {
                    PlayerStateHolder.playSong(history.first(), history)
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalSongList(songs = history.take(10))
                Spacer(modifier = Modifier.height(28.dp))
            }

            // SECTION: Kitaplık Kısayolları
            SectionHeaderWithAction("Kitaplık Kısayolları") {
                navController.navigate("library")
            }
            Spacer(modifier = Modifier.height(10.dp))
            LibraryList(navController = navController, historyCount = history.size)
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Kullanıcı Profili", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Adınızı ve profil fotoğrafı URL adresinizi güncelleyin.", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = profileNameInput,
                        onValueChange = { profileNameInput = it },
                        label = { Text("Adınız") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YouTubeRed,
                            unfocusedBorderColor = SurfaceVariantColor,
                            focusedLabelColor = YouTubeRed,
                            unfocusedLabelColor = TextSecondary
                        ),
                        textStyle = TextStyle(color = TextPrimary)
                    )
                    OutlinedTextField(
                        value = profileAvatarInput,
                        onValueChange = { profileAvatarInput = it },
                        label = { Text("Profil Fotoğrafı URL (https://...)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YouTubeRed,
                            unfocusedBorderColor = SurfaceVariantColor,
                            focusedLabelColor = YouTubeRed,
                            unfocusedLabelColor = TextSecondary
                        ),
                        textStyle = TextStyle(color = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfileViewModel.updateProfile(
                            name = profileNameInput,
                            avatarUri = profileAvatarInput
                        )
                        showProfileDialog = false
                        Toast.makeText(context, "Profil güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("İptal", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor
        )
    }
}

@Composable
fun QuickPickSongCard(
    song: SongModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF222222))
            .clickable { onClick() }
    ) {
        if (!song.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Gradient overlay at bottom with title & artist
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Play Button Top-Right Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Oynat",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SectionHeaderTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SectionHeaderWithAction(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tümünü Çal",
            color = YouTubeRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onActionClick() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun HorizontalSongList(songs: List<SongModel>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (songs.isEmpty()) {
            item {
                Text("Şarkılar yükleniyor...", color = TextSecondary, fontSize = 14.sp)
            }
        }
        items(songs) { song ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { PlayerStateHolder.playSong(song, songs) }
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceColor)
                ) {
                    if (!song.thumbnail.isNullOrBlank()) {
                        AsyncImage(
                            model = song.thumbnail,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(SurfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
                        }
                    }

                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Çal",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = song.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryList(navController: NavController, historyCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LibraryItem(
            title = "Favoriler & Beğenilenler", 
            subtitle = "Kayıtlı ve beğenilen parçalar", 
            thumbnailUrl = null, 
            onClick = { navController.navigate("library") }
        )
        LibraryItem(
            title = "Dinleme Geçmişi", 
            subtitle = "$historyCount Şarkı dinlendi", 
            thumbnailUrl = null, 
            onClick = { navController.navigate("library") }
        )
    }
}

@Composable
fun LibraryItem(
    title: String, 
    subtitle: String, 
    thumbnailUrl: String? = null, 
    hasIndicator: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Oynat", tint = TextPrimary, modifier = Modifier.size(24.dp))
        }
    }
}
