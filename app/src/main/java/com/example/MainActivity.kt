package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deadlyai.ui.DeadlyAiViewModel
import com.example.deadlyai.ui.screens.ChatHistoryScreen
import com.example.deadlyai.ui.screens.DashboardScreen
import com.example.deadlyai.ui.screens.SettingsScreen
import com.example.deadlyai.ui.screens.ToolsMemoryScreen
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.DeadlyAiTheme
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

enum class DeadlyNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    CONSOLE("REACTOR", Icons.Default.RadioButtonChecked, "nav_item_console"),
    ARCHIVE("ARCHIVE", Icons.Default.ChatBubbleOutline, "nav_item_archive"),
    VAULT("VAULT", Icons.Default.FolderSpecial, "nav_item_vault"),
    CONFIG("CONFIG", Icons.Default.Settings, "nav_item_config")
}

class MainActivity : ComponentActivity() {

    private val viewModel: DeadlyAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DeadlyAiTheme {
                var currentDestination by remember { mutableStateOf(DeadlyNavDestination.CONSOLE) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlack),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        DeadlyBottomNav(
                            currentDestination = currentDestination,
                            onDestinationSelected = { currentDestination = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepBlack)
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            DeadlyNavDestination.CONSOLE -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { currentDestination = DeadlyNavDestination.ARCHIVE }
                            )
                            DeadlyNavDestination.ARCHIVE -> ChatHistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = DeadlyNavDestination.CONSOLE }
                            )
                            DeadlyNavDestination.VAULT -> ToolsMemoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = DeadlyNavDestination.CONSOLE }
                            )
                            DeadlyNavDestination.CONFIG -> SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentDestination = DeadlyNavDestination.CONSOLE }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeadlyBottomNav(
    currentDestination: DeadlyNavDestination,
    onDestinationSelected: (DeadlyNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .background(DarkGraphite)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = DarkGraphite,
        tonalElevation = 6.dp
    ) {
        DeadlyNavDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = if (isSelected) {
                            if (destination == DeadlyNavDestination.CONSOLE) NeonRed else ElectricBlue
                        } else TextMuted
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        color = if (isSelected) TextWhite else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (destination == DeadlyNavDestination.CONSOLE) NeonRed.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f),
                    selectedIconColor = if (destination == DeadlyNavDestination.CONSOLE) NeonRed else ElectricBlue,
                    unselectedIconColor = TextMuted
                ),
                modifier = Modifier.testTag(destination.testTag)
            )
        }
    }
}
