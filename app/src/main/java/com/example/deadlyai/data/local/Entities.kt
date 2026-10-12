package com.example.deadlyai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineMode: Boolean = false,
    val actionType: String? = null, // e.g. "ALARM", "TIMER", "NOTE", "APP"
    val actionPayload: String? = null
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General"
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueTimestamp: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "Preference", // "Preference", "Fact", "Profile"
    val timestamp: Long = System.currentTimeMillis()
)
