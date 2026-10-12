package com.example.deadlyai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.DarkGraphite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderCyan,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        color = DarkGraphite.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(DarkGraphite.copy(alpha = 0.88f))
                .padding(14.dp),
            content = content
        )
    }
}
