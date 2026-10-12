package com.example.deadlyai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deadlyai.ui.DeadlyAiViewModel
import com.example.deadlyai.ui.components.GlassCard
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderRed
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SettingsScreen(
    viewModel: DeadlyAiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val userTitle by viewModel.userTitle.collectAsStateWithLifecycle()
    val speechPitch by viewModel.speechPitch.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val languagePref by viewModel.languagePref.collectAsStateWithLifecycle()
    val forceOffline by viewModel.forceOfflineMode.collectAsStateWithLifecycle()
    val highFidelityVisuals by viewModel.highFidelityVisuals.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    var tempTitle by remember(userTitle) { mutableStateOf(userTitle) }
    var tempApiKey by remember(customApiKey) { mutableStateOf(customApiKey) }
    var showWipeDialog by remember { mutableStateOf(false) }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("PURGE ALL DATA", color = StatusAlert, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently wipe all notes, reminders, user memories, and conversation history from local device storage.", color = TextWhite) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showWipeDialog = false
                        Toast.makeText(context, "Tactical databases purged.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("PURGE ALL", color = StatusAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) { Text("CANCEL", color = ElectricBlue) }
            },
            containerColor = DarkGraphite
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = "SYSTEM CONFIGURATION & PROTOCOLS",
            color = ElectricBlueGlow,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Personalization & Title
        GlassCard(borderColor = BorderCyan, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "PERSONALIZATION",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Address User As:",
                    color = TextWhite,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sir", "Boss", "Commander", "Doctor").forEach { titleOption ->
                        val isSelected = userTitle == titleOption
                        OutlinedButton(
                            onClick = { viewModel.updateUserTitle(titleOption) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) NeonRed.copy(alpha = 0.2f) else DarkGraphite
                            ),
                            border = BorderStroke(1.dp, if (isSelected) NeonRed else BorderCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = titleOption,
                                color = if (isSelected) TextWhite else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Voice & Audio Subsystem
        GlassCard(borderColor = BorderRed, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICE PERSONALITY (ROBOTIC CINEMATIC)",
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            viewModel.speechEngine.speak("Voice calibration nominal. DEADLY AI audio subsystems functioning at maximum efficiency, $userTitle.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGraphite),
                        border = BorderStroke(1.dp, BorderRed),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST", color = NeonRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pitch slider
                Text(
                    text = "Tone Pitch: ${String.format("%.2f", speechPitch)}x (Deep & Mature)",
                    color = TextWhite,
                    fontSize = 12.sp
                )
                Slider(
                    value = speechPitch,
                    onValueChange = { viewModel.updateSpeechPitch(it) },
                    valueRange = 0.5f..1.3f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonRed,
                        activeTrackColor = NeonRed,
                        inactiveTrackColor = DarkGraphite
                    )
                )

                // Rate slider
                Text(
                    text = "Controlled Pacing: ${String.format("%.2f", speechRate)}x",
                    color = TextWhite,
                    fontSize = 12.sp
                )
                Slider(
                    value = speechRate,
                    onValueChange = { viewModel.updateSpeechRate(it) },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricBlue,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = DarkGraphite
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Language selection
                Text(
                    text = "Language Protocol: Hindi / English / Hinglish supported",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Online AI Brain & Gemini Provider
        GlassCard(borderColor = BorderCyan, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "NEURAL UPLINK (ONLINE AI BRAIN)",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Provider Model: gemini-3.5-flash",
                    color = ElectricBlueGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Force Local Offline Mode",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Disable all external network calls; run offline command engine only.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = forceOffline,
                        onCheckedChange = { viewModel.updateForceOffline(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StatusAlert,
                            checkedTrackColor = BorderRed
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempApiKey,
                    onValueChange = {
                        tempApiKey = it
                        viewModel.updateCustomApiKey(it)
                    },
                    label = { Text("Custom Gemini API Key Override (Optional)", color = TextMuted, fontSize = 11.sp) },
                    placeholder = { Text("AIzaSy...", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkGraphite,
                        unfocusedContainerColor = DarkGraphite,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )

                Text(
                    text = "By default uses secure platform secrets via BuildConfig. Custom key is optional.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Visuals & Performance (Low-end device support)
        GlassCard(borderColor = BorderCyan, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "PERFORMANCE & ACCESSIBILITY",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "High Fidelity HUD Animations",
                            color = TextWhite,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Disable to conserve battery and boost speed on budget devices.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = highFidelityVisuals,
                        onCheckedChange = { viewModel.updateHighFidelityVisuals(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricBlue,
                            checkedTrackColor = BorderCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tactical Haptic Feedback",
                            color = TextWhite,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Vibrations for user touches and directives.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { viewModel.updateHapticsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricBlue,
                            checkedTrackColor = BorderCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Capabilities Matrix (Honest Implementation Summary)
        GlassCard(borderColor = BorderCyan, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "CAPABILITIES MATRIX & STATUS",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                val matrix = listOf(
                    "Arc Reactor Animated HUD" to "Active (Native Compose)",
                    "Speech Input & Text-to-Speech" to "Active (Android Native)",
                    "Multilingual (English, Hindi, Hinglish)" to "Supported",
                    "Offline Command Engine" to "Active (Local Parser)",
                    "Alarms & Timers Dispatch" to "Active (System Intent + In-App)",
                    "Notes & Reminders Storage" to "Active (Room Database)",
                    "Battery Telemetry" to "Active (Android BatteryManager)",
                    "Gemini 3.5 Flash Online Uplink" to "Active (REST API)",
                    "Long-Term Persistent Memory" to "Active (Local SQLite)"
                )

                matrix.forEach { (feature, status) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• $feature", color = TextWhite, fontSize = 12.sp)
                        Text(text = status, color = StatusOnline, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Data Management & Purge
        Button(
            onClick = { showWipeDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = DarkGraphite),
            border = BorderStroke(1.dp, BorderRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wipe_all_data_button")
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusAlert)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PURGE ALL LOCAL STORAGE & MEMORY", color = StatusAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
