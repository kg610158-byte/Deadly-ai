package com.example.deadlyai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.deadlyai.ui.DeadlySystemState
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.DeepCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGlow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactor(
    systemState: DeadlySystemState,
    audioLevel: Float,
    isHighFidelity: Boolean = true,
    size: Dp = 190.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorInfinite")

    // Clockwise outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (systemState) {
                    DeadlySystemState.THINKING -> 2000
                    DeadlySystemState.LISTENING -> 4000
                    else -> 10000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    // Counter-clockwise inner rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (systemState) {
                    DeadlySystemState.THINKING -> 1500
                    DeadlySystemState.LISTENING -> 3500
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    // Core pulsing glow
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (systemState) {
                    DeadlySystemState.SPEAKING -> 500
                    DeadlySystemState.THINKING -> 600
                    else -> 1500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CoreGlowPulse"
    )

    Box(
        modifier = Modifier
            .size(size)
            .testTag("arc_reactor_interactive")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f - 8.dp.toPx()

            // 1. Outermost guide circle
            drawCircle(
                color = ElectricBlue.copy(alpha = 0.25f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. Rotating Outer Segmented Arcs (Cyan Blue)
            val outerAnimAngle = if (isHighFidelity) outerRotation else 0f
            rotate(outerAnimAngle, pivot = center) {
                val segments = 8
                val sweep = 360f / segments
                for (i in 0 until segments) {
                    val startAngle = i * sweep
                    drawArc(
                        color = if (i % 2 == 0) ElectricBlue else NeonRed.copy(alpha = 0.8f),
                        startAngle = startAngle + 6f,
                        sweepAngle = sweep - 12f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = Size(baseRadius * 2, baseRadius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Tick marks on outer rim
                for (deg in 0 until 360 step 30) {
                    val rad = Math.toRadians(deg.toDouble())
                    val p1 = Offset(
                        (center.x + (baseRadius - 2.dp.toPx()) * cos(rad)).toFloat(),
                        (center.y + (baseRadius - 2.dp.toPx()) * sin(rad)).toFloat()
                    )
                    val p2 = Offset(
                        (center.x + (baseRadius + 4.dp.toPx()) * cos(rad)).toFloat(),
                        (center.y + (baseRadius + 4.dp.toPx()) * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = ElectricBlueGlow.copy(alpha = 0.7f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // 3. Middle Secondary Ring (Neon Red)
            val midRadius = baseRadius * 0.76f
            val innerAnimAngle = if (isHighFidelity) innerRotation else 45f
            rotate(innerAnimAngle, pivot = center) {
                drawCircle(
                    color = NeonRed.copy(alpha = 0.35f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                val midSegments = 6
                val midSweep = 360f / midSegments
                for (i in 0 until midSegments) {
                    drawArc(
                        color = NeonRed,
                        startAngle = i * midSweep + 10f,
                        sweepAngle = midSweep - 20f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Central Energy Core (Pulsing Glow)
            val effectiveAudioBoost = if (systemState == DeadlySystemState.LISTENING || systemState == DeadlySystemState.SPEAKING) {
                (audioLevel * 0.4f)
            } else 0f

            val coreRadius = (baseRadius * 0.46f) * (corePulse + effectiveAudioBoost)
            val coreBrush = Brush.radialGradient(
                colors = listOf(
                    NeonRedGlow,
                    NeonRed,
                    DarkCrimson,
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius * 1.3f
            )

            drawCircle(
                brush = coreBrush,
                radius = coreRadius,
                center = center
            )

            // 5. Innermost Cyan AI Core Ring
            val innermostRadius = baseRadius * 0.28f
            drawCircle(
                color = ElectricBlue,
                radius = innermostRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Inner core dot
            drawCircle(
                color = ElectricBlueGlow,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
