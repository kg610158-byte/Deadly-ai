package com.example.deadlyai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.deadlyai.ui.DeadlySystemState
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGlow
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    systemState: DeadlySystemState,
    audioLevel: Float,
    isHighFidelity: Boolean = true,
    height: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformPhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalBars = 36
        val barSpacing = 4.dp.toPx()
        val totalAvailableWidth = size.width
        val barWidth = ((totalAvailableWidth - (totalBars - 1) * barSpacing) / totalBars).coerceAtLeast(3.dp.toPx())
        val midY = size.height / 2f

        val isActive = systemState == DeadlySystemState.LISTENING ||
                systemState == DeadlySystemState.SPEAKING ||
                systemState == DeadlySystemState.THINKING

        val effectiveAudio = if (isActive) audioLevel.coerceIn(0.15f, 1.0f) else 0.05f

        for (i in 0 until totalBars) {
            val normalizedX = i.toFloat() / totalBars.toFloat()
            val wave = if (isActive && isHighFidelity) {
                val s1 = sin(normalizedX * 12f + phase)
                val s2 = sin(normalizedX * 6f - phase * 0.7f)
                abs(s1 * 0.6f + s2 * 0.4f)
            } else {
                abs(sin(normalizedX * 3.14f)) * 0.25f
            }

            val maxBarHeight = size.height * 0.9f
            val barH = (maxBarHeight * (wave * effectiveAudio + 0.08f)).coerceIn(4.dp.toPx(), maxBarHeight)

            val x = i * (barWidth + barSpacing)
            val y = midY - (barH / 2f)

            val brush = if (i % 2 == 0) {
                Brush.verticalGradient(
                    colors = listOf(ElectricBlueGlow, ElectricBlue),
                    startY = y,
                    endY = y + barH
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(NeonRedGlow, NeonRed),
                    startY = y,
                    endY = y + barH
                )
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
