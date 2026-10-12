package com.example.deadlyai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.deadlyai.data.local.NoteEntity
import com.example.deadlyai.data.local.ReminderEntity
import com.example.deadlyai.data.local.UserMemoryEntity
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
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ToolsMemoryScreen(
    viewModel: DeadlyAiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("NOTES", "REMINDERS & TIMERS", "LONG-TERM MEMORY")

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val activeTimer by viewModel.activeTimer.collectAsStateWithLifecycle()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TACTICAL MEMORY VAULT",
                color = ElectricBlueGlow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )

            // Export button
            IconButton(
                onClick = {
                    val exportContent = buildExportString(notes, reminders, memories)
                    viewModel.deviceActionHandler.shareText("DEADLY AI Tactical Vault Export", exportContent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Data",
                    tint = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkGraphite,
            contentColor = ElectricBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = if (selectedTab == 0) ElectricBlue else NeonRed
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == index) TextWhite else TextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> NotesTabContent(
                notes = notes,
                onAddNote = { showAddNoteDialog = true },
                onDeleteNote = { viewModel.deleteNote(it) }
            )
            1 -> RemindersTabContent(
                reminders = reminders,
                activeTimer = activeTimer,
                onAddReminder = { showAddReminderDialog = true },
                onToggleReminder = { viewModel.toggleReminder(it) },
                onDeleteReminder = { viewModel.deleteReminder(it) },
                onStartTimer = { sec, lbl -> viewModel.startInAppTimer(sec, lbl) },
                onCancelTimer = { viewModel.cancelInAppTimer() }
            )
            2 -> MemoryTabContent(
                memories = memories,
                onAddMemory = { showAddMemoryDialog = true },
                onDeleteMemory = { viewModel.deleteMemory(it) }
            )
        }
    }

    // Dialogs
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content, cat ->
                viewModel.addNote(title, content, cat)
                showAddNoteDialog = false
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { title, time ->
                viewModel.addReminder(title, time)
                showAddReminderDialog = false
            }
        )
    }

    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onConfirm = { key, value ->
                viewModel.addMemory(key, value)
                showAddMemoryDialog = false
            }
        )
    }
}

@Composable
fun NotesTabContent(
    notes: List<NoteEntity>,
    onAddNote: () -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notes.size} LOGGED NOTES",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = onAddNote,
                colors = ButtonDefaults.buttonColors(containerColor = DarkGraphite),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW NOTE", color = ElectricBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notes.isEmpty()) {
            EmptyPlaceholder("NO OFFLINE NOTES FOUND", "Tell DEADLY AI 'Note: ...' or tap NEW NOTE to record.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkGraphite,
                        border = BorderStroke(1.dp, BorderCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    color = ElectricBlueGlow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { onDeleteNote(note.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Note",
                                        tint = TextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content,
                                color = TextWhite,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersTabContent(
    reminders: List<ReminderEntity>,
    activeTimer: com.example.deadlyai.ui.ActiveInAppTimer?,
    onAddReminder: () -> Unit,
    onToggleReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onStartTimer: (Int, String) -> Unit,
    onCancelTimer: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Timer Buttons
        Text(
            text = "QUICK COUNTDOWN TIMERS",
            color = NeonRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                60 to "1 Min",
                300 to "5 Min",
                600 to "10 Min",
                1800 to "30 Min"
            )
            presets.forEach { (sec, label) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkGraphite,
                    border = BorderStroke(1.dp, BorderRed),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStartTimer(sec, "Tactical $label Timer") }
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = label, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reminders Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${reminders.size} SCHEDULED REMINDERS",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = onAddReminder,
                colors = ButtonDefaults.buttonColors(containerColor = DarkGraphite),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW REMINDER", color = ElectricBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (reminders.isEmpty()) {
            EmptyPlaceholder("NO REMINDERS SCHEDULED", "Say 'Remind me to ...' to schedule an offline reminder.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    val dueStr = remember(reminder.dueTimestamp) {
                        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(reminder.dueTimestamp))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkGraphite,
                        border = BorderStroke(1.dp, if (reminder.isCompleted) BorderCyan else BorderRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onToggleReminder(reminder) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = "Toggle",
                                    tint = if (reminder.isCompleted) StatusOnline else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    color = if (reminder.isCompleted) TextMuted else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Scheduled: $dueStr",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(onClick = { onDeleteReminder(reminder.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryTabContent(
    memories: List<UserMemoryEntity>,
    onAddMemory: () -> Unit,
    onDeleteMemory: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${memories.size} PERSISTENT FACTS",
                color = ElectricBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = onAddMemory,
                colors = ButtonDefaults.buttonColors(containerColor = DarkGraphite),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD MEMORY", color = ElectricBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (memories.isEmpty()) {
            EmptyPlaceholder("NO LONG-TERM MEMORIES STORED", "Add facts or preferences that DEADLY AI should remember about you.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories, key = { it.id }) { memory ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkGraphite,
                        border = BorderStroke(1.dp, BorderCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = memory.key.uppercase(),
                                    color = ElectricBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = memory.value,
                                    color = TextWhite,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(onClick = { onDeleteMemory(memory.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Memory",
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

// Dialogs
@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("RECORD TACTICAL NOTE", color = ElectricBlue, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (content.isNotBlank()) onConfirm(title.ifBlank { "Untitled Note" }, content, "General") }
            ) {
                Text("SAVE", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
        containerColor = DarkGraphite
    )
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var hoursAhead by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SCHEDULE REMINDER", color = NeonRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder description", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hoursAhead,
                    onValueChange = { hoursAhead = it },
                    label = { Text("Hours from now (e.g. 1, 2, 24)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val hrs = hoursAhead.toLongOrNull() ?: 1L
                        val timestamp = System.currentTimeMillis() + (hrs * 3600 * 1000)
                        onConfirm(title, timestamp)
                    }
                }
            ) {
                Text("SCHEDULE", color = NeonRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
        containerColor = DarkGraphite
    )
}

@Composable
fun AddMemoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("STORE USER MEMORY", color = ElectricBlue, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key (e.g. Favorite Language, City)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value (e.g. Hinglish, Mumbai)", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (key.isNotBlank() && value.isNotBlank()) onConfirm(key, value) }
            ) {
                Text("STORE", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
        containerColor = DarkGraphite
    )
}

private fun buildExportString(
    notes: List<NoteEntity>,
    reminders: List<ReminderEntity>,
    memories: List<UserMemoryEntity>
): String {
    val sb = StringBuilder()
    sb.appendLine("=== DEADLY AI TACTICAL VAULT EXPORT ===")
    sb.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
    sb.appendLine()
    sb.appendLine("-- NOTES (${notes.size}) --")
    notes.forEach {
        sb.appendLine("[${it.title}] ${it.content}")
    }
    sb.appendLine()
    sb.appendLine("-- REMINDERS (${reminders.size}) --")
    reminders.forEach {
        val completed = if (it.isCompleted) "[COMPLETED]" else "[PENDING]"
        sb.appendLine("$completed ${it.title} (Due: ${Date(it.dueTimestamp)})")
    }
    sb.appendLine()
    sb.appendLine("-- LONG-TERM USER MEMORY (${memories.size}) --")
    memories.forEach {
        sb.appendLine("${it.key}: ${it.value}")
    }
    return sb.toString()
}
