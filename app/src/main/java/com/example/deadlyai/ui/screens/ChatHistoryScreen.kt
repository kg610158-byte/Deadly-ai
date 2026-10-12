package com.example.deadlyai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.deadlyai.data.local.ChatMessageEntity
import com.example.deadlyai.ui.DeadlyAiViewModel
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.BorderRed
import com.example.ui.theme.DarkGraphite
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryScreen(
    viewModel: DeadlyAiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("PURGE LOG ARCHIVES", color = NeonRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you certain you want to erase all conversation logs from local memory?", color = TextWhite)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("PURGE", color = StatusAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = ElectricBlue)
                }
            },
            containerColor = DarkGraphite
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNICATION ARCHIVE",
                color = ElectricBlueGlow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )

            IconButton(
                onClick = { showClearDialog = true },
                enabled = messages.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear All Logs",
                    tint = if (messages.isNotEmpty()) StatusAlert else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs...", color = TextMuted, fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_input"),
            shape = RoundedCornerShape(12.dp),
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

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO ARCHIVED LOGS FOUND",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Transcripts will synchronize as you interact with DEADLY AI.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMessages.reversed(), key = { it.id }) { msg ->
                    MessageHistoryCard(
                        message = msg,
                        onReplay = { viewModel.replaySpeech(msg.content) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Deadly AI Message", msg.content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageHistoryCard(
    message: ChatMessageEntity,
    onReplay: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.role == "user"
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val cardBorder = if (isUser) BorderCyan else if (message.isOfflineMode) BorderCyan else BorderRed
    val roleBadgeColor = if (isUser) ElectricBlue else if (message.isOfflineMode) StatusOffline else NeonRed
    val roleLabel = if (isUser) "USER" else if (message.isOfflineMode) "DEADLY AI (LOCAL)" else "DEADLY AI (NEURAL)"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isUser) ElevatedGraphite else DarkGraphite,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.95f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header: Role Badge & Time & Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(roleBadgeColor.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = roleLabel,
                                color = roleBadgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeStr,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        if (!isUser) {
                            IconButton(onClick = onReplay, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Replay",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.content,
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Action Pill if action was triggered
                if (!message.actionType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, BorderCyan), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTION: ${message.actionType}",
                            color = ElectricBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
