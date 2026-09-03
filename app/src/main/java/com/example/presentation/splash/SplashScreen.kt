package com.example.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSpaceBackground
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonGreen

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashTransition")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSpaceBackground),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Draw Stars
            repeat(50) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = (1..3).random().toFloat(),
                    center = Offset(
                        (0..size.width.toInt()).random().toFloat(),
                        (0..size.height.toInt()).random().toFloat()
                    )
                )
            }
            
            // Draw Sun
            val sunRadius = 150f * scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonYellow, NeonRed.copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = sunRadius * 1.5f
                ),
                radius = sunRadius * 1.5f,
                center = center
            )
            drawCircle(
                color = NeonYellow,
                radius = sunRadius * 0.4f,
                center = center
            )
        }
        
        Text(
            text = "AKREP MÜZİK",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )
    }
}
