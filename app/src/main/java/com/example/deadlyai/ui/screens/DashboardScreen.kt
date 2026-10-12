package com.example.deadlyai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deadlyai.ui.DeadlyAiViewModel
import com.example.deadlyai.ui.DeadlySystemState
import com.example.deadlyai.ui.components.ArcReactor
import com.example.deadlyai.ui.components.GlassCard
import com.example.deadlyai.ui.components.HudHeader
import com.example.deadlyai.ui.components.QuickActionChips
import com.example.deadlyai.ui.components.WaveformVisualizer
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderRed
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGlow
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun DashboardScreen(
    viewModel: DeadlyAiViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemState by viewModel.systemState.collectAsStateWithLifecycle()
    val statusDetail by viewModel.statusDetail.collectAsStateWithLifecycle()
    val batteryStatus by viewModel.batteryStatus.collectAsStateWithLifecycle()
    val isOnline by viewModel.isNetworkOnline.collectAsStateWithLifecycle()
    val audioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isVoiceSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.isVoiceListening.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val activeTimer by viewModel.activeTimer.collectAsStateWithLifecycle()
    val highFidelityVisuals by viewModel.highFidelityVisuals.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        }
    }

    val latestMessage = messages.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 14.dp)
            .imePadding()
    ) {
        // 1. HUD Header
        HudHeader(
            systemState = systemState,
            batteryStatus = batteryStatus,
            isOnline = isOnline && !viewModel.forceOfflineMode.value,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
        )

        // Main Scrollable Tactical Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 2. Animated Arc Reactor
            ArcReactor(
                systemState = systemState,
                audioLevel = audioLevel,
                isHighFidelity = highFidelityVisuals,
                size = 180.dp,
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else if (isListening) {
                        viewModel.speechEngine.stopListening()
                    } else {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            viewModel.startVoiceInput()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )

            Text(
                text = if (isSpeaking) "TAP REACTOR TO SILENCE VOICE" else if (isListening) "LISTENING... TAP TO CANCEL" else "TAP REACTOR FOR VOICE DIRECTIVE",
                color = if (isSpeaking) NeonRed else ElectricBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Audio Waveform Visualizer
            WaveformVisualizer(
                systemState = systemState,
                audioLevel = audioLevel,
                isHighFidelity = highFidelityVisuals,
                height = 36.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 4. In-App Tactical Countdown Banner (if active)
            activeTimer?.let { timer ->
                Spacer(modifier = Modifier.height(10.dp))
                val mins = timer.remainingSeconds / 60
                val secs = timer.remainingSeconds % 60
                val displayTime = String.format("%02d:%02d", mins, secs)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkGraphite,
                    border = BorderStroke(1.dp, BorderRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("in_app_timer_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱ TACTICAL TIMER: ",
                                color = NeonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = displayTime,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { viewModel.cancelInAppTimer() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Timer",
                                tint = StatusAlert,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Tactical Status Banner & Latest Briefing Card
            GlassCard(
                borderColor = if (systemState == DeadlySystemState.SPEAKING) BorderRed else BorderCyan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS PROTOCOL",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        if (latestMessage != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSpeaking) {
                                    IconButton(
                                        onClick = { viewModel.stopSpeaking() },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop Speech",
                                            tint = NeonRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { viewModel.replaySpeech(latestMessage.content) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Replay Speech",
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = statusDetail,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (latestMessage != null && latestMessage.role == "assistant") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepBlack.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = latestMessage.content,
                                color = TextWhite,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Quick Action Chips
            QuickActionChips(
                onActionSelected = { command ->
                    viewModel.submitPrompt(command)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 7. Tactical Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Button
            val micBorderColor = if (isListening) NeonRedGlow else BorderCyan
            val micBgColor = if (isListening) NeonRed.copy(alpha = 0.25f) else DarkGraphite

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(micBgColor)
                    .border(BorderStroke(1.5.dp, micBorderColor), CircleShape)
                    .clickable {
                        if (isListening) {
                            viewModel.speechEngine.stopListening()
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                viewModel.startVoiceInput()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                    .testTag("microphone_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                    contentDescription = "Voice Directive",
                    tint = if (isListening) NeonRed else ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Directive in Hindi, English, Hinglish...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkGraphite,
                    unfocusedContainerColor = DarkGraphite,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderCyan,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                maxLines = 3,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitPrompt(inputText)
                            inputText = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) NeonRed else DarkGraphite)
                    .border(BorderStroke(1.dp, if (inputText.isNotBlank()) BorderRed else BorderCyan), CircleShape)
                    .clickable(enabled = inputText.isNotBlank()) {
                        viewModel.submitPrompt(inputText)
                        inputText = ""
                    }
                    .testTag("send_prompt_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Directive",
                    tint = if (inputText.isNotBlank()) TextWhite else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
