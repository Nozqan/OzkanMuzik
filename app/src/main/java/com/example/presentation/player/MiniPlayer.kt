package com.example.presentation.player

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.SurfaceVariantColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MiniPlayer(onExpand: () -> Unit) {
    val currentSong by PlayerStateHolder.currentSong.collectAsState()
    val isPlaying by PlayerStateHolder.isPlaying.collectAsState()
    val isDismissed by PlayerStateHolder.isMiniPlayerDismissed.collectAsState()
    val currentPos by PlayerStateHolder.currentPositionMs.collectAsState()
    val duration by PlayerStateHolder.durationMs.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    // Reset offset when track changes or player is un-dismissed
    LaunchedEffect(currentSong?.id, isDismissed) {
        if (!isDismissed) {
            offsetX.snapTo(0f)
        }
    }

    if (currentSong == null || isDismissed) return

    val progress = if (duration > 0) (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val swipeAlpha = (1f - (abs(offsetX.value) / 450f)).coerceIn(0.1f, 1f)

    // Animated equalizing bars for active playback
    val infiniteTransition = rememberInfiniteTransition(label = "mini_eq")
    val eqHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), AnimRepeatMode.Reverse),
        label = "h1"
    )
    val eqHeight2 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), AnimRepeatMode.Reverse),
        label = "h2"
    )
    val eqHeight3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), AnimRepeatMode.Reverse),
        label = "h3"
    )

    AnimatedVisibility(
        visible = !isDismissed,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .alpha(swipeAlpha)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (abs(offsetX.value) > 160f) {
                                    val target = if (offsetX.value > 0) 1000f else -1000f
                                    offsetX.animateTo(target, tween(200, easing = FastOutSlowInEasing))
                                    PlayerStateHolder.dismissMiniPlayer()
                                    offsetX.snapTo(0f)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 400f))
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    )
                }
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF282828),
                            Color(0xFF1E1E1E)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.09f),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onExpand() }
                .testTag("mini_player_container")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(start = 8.dp, end = 4.dp)
                ) {
                    // Album Artwork with soft rounded corners
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF121212)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentSong?.thumbnail.isNullOrBlank()) {
                            AsyncImage(
                                model = currentSong?.thumbnail,
                                contentDescription = "Albüm Kapağı",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Equalizer badge overlay when playing
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(16.dp)
                                ) {
                                    Box(modifier = Modifier.width(2.5.dp).height(eqHeight1.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                    Box(modifier = Modifier.width(2.5.dp).height(eqHeight2.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                    Box(modifier = Modifier.width(2.5.dp).height(eqHeight3.dp).background(Color.White, RoundedCornerShape(1.dp)))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Artist Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong?.title ?: "Bilinmeyen Parça",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong?.artist ?: "Sanatçı",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }



                    // Skip Previous Button
                    IconButton(
                        onClick = { PlayerStateHolder.skipPrevious() },
                        modifier = Modifier.size(36.dp).testTag("mini_player_skip_previous")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Önceki Parça",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Play / Pause Button with circle accent
                    IconButton(
                        onClick = { PlayerStateHolder.togglePlayPause() },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("mini_player_play_pause")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Durdur" else "Oynat",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Skip Next Button
                    IconButton(
                        onClick = { PlayerStateHolder.skipNext() },
                        modifier = Modifier.size(36.dp).testTag("mini_player_skip_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Sonraki Parça",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Quick Dismiss 'X' Button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                offsetX.animateTo(800f, tween(180))
                                PlayerStateHolder.dismissMiniPlayer()
                                offsetX.snapTo(0f)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Sleek YouTube Red Progress Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(SurfaceVariantColor.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(YouTubeRed, Color(0xFFFF5252))
                                )
                            )
                    )
                }
            }
        }
    }
}
