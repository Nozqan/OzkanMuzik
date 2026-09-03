package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SurfaceColor
import com.example.ui.theme.NeonYellow

@Composable
fun NeomorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = NeonYellow.copy(alpha = 0.2f),
                spotColor = NeonYellow.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor.copy(alpha = 0.8f))
            .padding(16.dp),
        content = content
    )
}
