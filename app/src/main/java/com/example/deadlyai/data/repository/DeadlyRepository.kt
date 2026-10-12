package com.example.deadlyai.data.repository

import com.example.deadlyai.data.local.ChatMessageEntity
import com.example.deadlyai.data.local.DeadlyDatabase
import com.example.deadlyai.data.local.NoteEntity
import com.example.deadlyai.data.local.ReminderEntity
import com.example.deadlyai.data.local.UserMemoryEntity
import com.example.deadlyai.data.remote.GeminiContent
import com.example.deadlyai.data.remote.GeminiGenerateRequest
import com.example.deadlyai.data.remote.GeminiPart
import com.example.deadlyai.data.remote.RetrofitGeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DeadlyRepository(private val database: DeadlyDatabase) {

    val allMessages: Flow<List<ChatMessageEntity>> = database.messageDao().getAllMessages()
    val allNotes: Flow<List<NoteEntity>> = database.noteDao().getAllNotes()
    val allReminders: Flow<List<ReminderEntity>> = database.reminderDao().getAllReminders()
    val allMemories: Flow<List<UserMemoryEntity>> = database.memoryDao().getAllMemories()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = database.noteDao().searchNotes(query)

    suspend fun saveMessage(
        role: String,
        content: String,
        isOffline: Boolean = false,
        actionType: String? = null,
        actionPayload: String? = null
    ): Long = withContext(Dispatchers.IO) {
        database.messageDao().insertMessage(
            ChatMessageEntity(
                role = role,
                content = content,
                isOfflineMode = isOffline,
                actionType = actionType,
                actionPayload = actionPayload
            )
        )
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        database.messageDao().clearAllMessages()
    }

    // Notes
    suspend fun createNote(title: String, content: String, category: String = "General"): Long =
        withContext(Dispatchers.IO) {
            database.noteDao().insertNote(
                NoteEntity(title = title, content = content, category = category)
            )
        }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        database.noteDao().deleteNoteById(id)
    }

    suspend fun clearNotes() = withContext(Dispatchers.IO) {
        database.noteDao().clearAllNotes()
    }

    // Reminders
    suspend fun createReminder(title: String, dueTimestamp: Long): Long =
        withContext(Dispatchers.IO) {
            database.reminderDao().insertReminder(
                ReminderEntity(title = title, dueTimestamp = dueTimestamp)
            )
        }

    suspend fun toggleReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        database.reminderDao().updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        database.reminderDao().deleteReminderById(id)
    }

    // Memory
    suspend fun saveMemory(key: String, value: String, category: String = "Preference"): Long =
        withContext(Dispatchers.IO) {
            database.memoryDao().insertMemory(
                UserMemoryEntity(key = key, value = value, category = category)
            )
        }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        database.memoryDao().deleteMemoryById(id)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.messageDao().clearAllMessages()
        database.noteDao().clearAllNotes()
        database.reminderDao().clearAllReminders()
        database.memoryDao().clearAllMemories()
    }

    // Online Gemini Generation
    suspend fun queryGemini(
        apiKey: String,
        userPrompt: String,
        systemInstructionText: String,
        recentContext: List<ChatMessageEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build conversation history (up to last 6 messages)
            val contents = mutableListOf<GeminiContent>()
            val trimmedHistory = recentContext.takeLast(6)
            for (msg in trimmedHistory) {
                val geminiRole = if (msg.role == "user") "user" else "model"
                contents.add(
                    GeminiContent(
                        role = geminiRole,
                        parts = listOf(GeminiPart(text = msg.content))
                    )
                )
            }
            // Add current message
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userPrompt))
                )
            )

            val request = GeminiGenerateRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstructionText))
                )
            )

            val response = RetrofitGeminiClient.apiService.generateContent(
                apiKey = apiKey,
                request = request
            )

            val candidateText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.mapNotNull { it.text }
                ?.joinToString("")

            if (!candidateText.isNullOrBlank()) {
                Result.success(candidateText)
            } else {
                val blockReason = response.promptFeedback?.blockReason
                Result.failure(Exception(blockReason ?: "Empty response from neural network."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
