package com.example.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.SongModel
import com.example.features.FeatureHubScreen
import com.example.presentation.explore.ExploreScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.player.FullPlayerScreen
import com.example.presentation.player.MiniPlayer
import com.example.presentation.splash.SplashScreen
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var isSplashFinished by remember { mutableStateOf(false) }
    var isFullPlayerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1800)
        isSplashFinished = true
    }

    BackHandler(enabled = isFullPlayerExpanded) {
        isFullPlayerExpanded = false
    }

    if (!isSplashFinished) {
        SplashScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize().background(DarkSpaceBackground)) {
            Scaffold(
                containerColor = DarkSpaceBackground,
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    if (currentRoute != "full_player") {
                        Column(modifier = Modifier.background(DarkSpaceBackground)) {
                            MiniPlayer(
                                onExpand = { isFullPlayerExpanded = true }
                            )
                            BottomNavBar(navController = navController)
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("home") {
                        val repository = com.example.di.DependencyProvider.repository!!
                        val homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.presentation.home.HomeViewModel>(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.example.presentation.home.HomeViewModel(repository) as T
                                }
                            }
                        )
                        val searchViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.presentation.search.SearchViewModel>(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.example.presentation.search.SearchViewModel(repository) as T
                                }
                            }
                        )
                        HomeScreen(navController = navController, viewModel = homeViewModel, searchViewModel = searchViewModel)
                    }

                    composable("features") {
                        val playlist by com.example.presentation.player.PlayerStateHolder.playlist.collectAsState()
                        val repository = com.example.di.DependencyProvider.repository!!
                        val history by repository.history.collectAsState(initial = emptyList())
                        val songs = remember(playlist, history) {
                            (playlist + history).ifEmpty {
                                listOf(
                                    SongModel("sample_1", "Akrep Melodisi", "Akrep Müzik", null, null, 210, "local", "sample_1", true),
                                    SongModel("sample_2", "Gece Rüzgarı", "Sanatçı", null, null, 185, "local", "sample_2", true)
                                )
                            }
                        }
                        FeatureHubScreen(allSongs = songs, onBack = { navController.popBackStack() })
                    }
                    composable("local_audio") {
                        val repository = com.example.di.DependencyProvider.repository!!
                        com.example.features.offlinecache.LocalAudioScreen(repository = repository)
                    }
                    composable("library") {
                        val repository = com.example.di.DependencyProvider.repository!!
                        val libraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.presentation.library.LibraryViewModel>(
                            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return com.example.presentation.library.LibraryViewModel(repository) as T
                                }
                            }
                        )
                        com.example.presentation.library.LibraryScreen(libraryViewModel, onNavigateToLocalAudio = { navController.navigate("local_audio") })
                    }
                    composable("full_player") {
                        FullPlayerScreen(
                            onCollapse = { navController.popBackStack() }
                        )
                    }
                }
            }

            // Smooth Slide-Up Full Player Overlay
            AnimatedVisibility(
                visible = isFullPlayerExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 380f
                    )
                ) + fadeIn(animationSpec = tween(180)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(180)),
                modifier = Modifier.fillMaxSize()
            ) {
                FullPlayerScreen(
                    onCollapse = { isFullPlayerExpanded = false }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Triple("Ana Sayfa", Icons.Default.Home, "home"),
        Triple("Sana Özel", Icons.Default.FastForward, "features"),
        Triple("Kitaplık", Icons.Default.Bookmarks, "library")
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSpaceBackground)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.third
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { 
                        navController.navigate(item.third) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = item.second,
                    contentDescription = item.first,
                    tint = if (isSelected) TextPrimary else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.first,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
