package com.example.deadlyai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderRed
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextWhite

data class QuickActionItem(
    val label: String,
    val commandText: String,
    val icon: ImageVector,
    val isRedAccent: Boolean = false
)

@Composable
fun QuickActionChips(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem("Alarm 6 AM", "Kal subah 6 baje alarm laga do", Icons.Default.Alarm, isRedAccent = true),
        QuickActionItem("5m Timer", "Timer for 5 minutes", Icons.Default.HourglassTop),
        QuickActionItem("Take Note", "Note: Check tactical systems and armor status", Icons.Default.EditNote),
        QuickActionItem("Reminder", "Remind me to review telemetry at 4 PM", Icons.Default.NotificationsActive, isRedAccent = true),
        QuickActionItem("Open Camera", "Open Camera", Icons.Default.CameraAlt),
        QuickActionItem("Calculate", "Calculate 25 * 45", Icons.Default.Calculate),
        QuickActionItem("Battery Telemetry", "Battery status", Icons.Default.BatteryStd),
        QuickActionItem("Offline Protocols", "Help", Icons.Default.HelpOutline)
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            val accentColor = if (item.isRedAccent) NeonRed else ElectricBlue
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkGraphite,
                border = BorderStroke(1.dp, if (item.isRedAccent) BorderRed else BorderCyan),
                modifier = Modifier
                    .testTag("quick_action_chip_$index")
                    .clickable { onActionSelected(item.commandText) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
