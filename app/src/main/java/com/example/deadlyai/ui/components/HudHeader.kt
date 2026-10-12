package com.example.deadlyai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deadlyai.engine.BatteryStatus
import com.example.deadlyai.ui.DeadlySystemState
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderRed
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusThinking
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudHeader(
    systemState: DeadlySystemState,
    batteryStatus: BatteryStatus,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now).uppercase()
            delay(1000)
        }
    }

    val stateColor by animateColorAsState(
        targetValue = when (systemState) {
            DeadlySystemState.ONLINE -> StatusOnline
            DeadlySystemState.OFFLINE -> StatusOffline
            DeadlySystemState.LISTENING -> ElectricBlue
            DeadlySystemState.THINKING -> StatusThinking
            DeadlySystemState.SPEAKING -> NeonRed
            DeadlySystemState.ACTION_COMPLETED -> StatusOnline
            DeadlySystemState.ACTION_FAILED -> StatusAlert
        },
        label = "StateColorAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGraphite.copy(alpha = 0.95f))
            .border(BorderStroke(1.dp, if (systemState == DeadlySystemState.SPEAKING) BorderRed else BorderCyan), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("hud_header")
    ) {
        // Top row: Logo, State Badge & Uplink
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(stateColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEADLY AI",
                    color = NeonRed,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(stateColor.copy(alpha = 0.16f))
                    .border(BorderStroke(1.dp, stateColor.copy(alpha = 0.7f)), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = systemState.label,
                    color = stateColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row: Time, Date & Battery Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clock & Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentTimeString.ifEmpty { "00:00:00" },
                    color = ElectricBlueGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " HRS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " | $currentDateString",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Telemetry: Battery & Network
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Battery
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    if (batteryStatus.isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = ElectricBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${batteryStatus.levelPercentage}%",
                        color = if (batteryStatus.levelPercentage < 20) StatusAlert else TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Network Uplink
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Sensors else Icons.Default.SensorsOff,
                        contentDescription = "Network Uplink",
                        tint = if (isOnline) ElectricBlue else StatusOffline,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isOnline) "ONLINE" else "LOCAL",
                        color = if (isOnline) ElectricBlue else StatusOffline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
