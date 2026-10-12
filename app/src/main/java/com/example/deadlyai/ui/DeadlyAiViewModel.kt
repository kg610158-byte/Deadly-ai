package com.example.deadlyai.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.deadlyai.data.local.ChatMessageEntity
import com.example.deadlyai.data.local.DeadlyDatabase
import com.example.deadlyai.data.local.NoteEntity
import com.example.deadlyai.data.local.ReminderEntity
import com.example.deadlyai.data.local.UserMemoryEntity
import com.example.deadlyai.data.repository.DeadlyRepository
import com.example.deadlyai.engine.BatteryMonitor
import com.example.deadlyai.engine.BatteryStatus
import com.example.deadlyai.engine.DeviceActionHandler
import com.example.deadlyai.engine.NetworkMonitor
import com.example.deadlyai.engine.OfflineCommandEngine
import com.example.deadlyai.engine.SpeechEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeadlySystemState(val label: String) {
    ONLINE("SYSTEM ONLINE"),
    OFFLINE("SYSTEM OFFLINE"),
    LISTENING("LISTENING"),
    THINKING("THINKING"),
    SPEAKING("SPEAKING"),
    ACTION_COMPLETED("ACTION COMPLETED"),
    ACTION_FAILED("ACTION FAILED")
}

data class ActiveInAppTimer(
    val initialSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val label: String = "",
    val isRunning: Boolean = false
)

class DeadlyAiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DeadlyDatabase.getInstance(application)
    val repository = DeadlyRepository(database)

    val deviceActionHandler = DeviceActionHandler(application)
    val speechEngine = SpeechEngine(application)
    val batteryMonitor = BatteryMonitor(application)
    val networkMonitor = NetworkMonitor(application)
    val offlineEngine = OfflineCommandEngine(deviceActionHandler)

    // System State
    private val _systemState = MutableStateFlow(DeadlySystemState.ONLINE)
    val systemState: StateFlow<DeadlySystemState> = _systemState.asStateFlow()

    // Last status message
    private val _statusDetail = MutableStateFlow("Subsystems synchronized. Awaiting directive.")
    val statusDetail: StateFlow<String> = _statusDetail.asStateFlow()

    // Settings
    private val prefs = application.getSharedPreferences("deadly_ai_settings", Context.MODE_PRIVATE)
    val userTitle = MutableStateFlow(prefs.getString("user_title", "Sir") ?: "Sir")
    val speechPitch = MutableStateFlow(prefs.getFloat("speech_pitch", 0.82f))
    val speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 0.96f))
    val languagePref = MutableStateFlow(prefs.getString("pref_language", "Hinglish / English / Hindi") ?: "Hinglish / English / Hindi")
    val forceOfflineMode = MutableStateFlow(prefs.getBoolean("force_offline", false))
    val highFidelityVisuals = MutableStateFlow(prefs.getBoolean("high_fidelity_visuals", true))
    val hapticFeedbackEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")

    // Real-time battery and network
    val batteryStatus: StateFlow<BatteryStatus> = batteryMonitor.batteryStatus
    val isNetworkOnline: StateFlow<Boolean> = networkMonitor.isOnline

    // In-app Countdown Timer
    private val _activeTimer = MutableStateFlow<ActiveInAppTimer?>(null)
    val activeTimer: StateFlow<ActiveInAppTimer?> = _activeTimer.asStateFlow()
    private var timerJob: Job? = null

    // Room Database Streams
    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories: StateFlow<List<UserMemoryEntity>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Live audio level from SpeechEngine
    val rmsAudioLevel: StateFlow<Float> = speechEngine.rmsAudioLevel
    val isVoiceSpeaking: StateFlow<Boolean> = speechEngine.isSpeaking
    val isVoiceListening: StateFlow<Boolean> = speechEngine.isListening

    init {
        // Synchronize initial speech parameters
        speechEngine.speechPitch = speechPitch.value
        speechEngine.speechRate = speechRate.value

        // Monitor speech state changes to update system state
        viewModelScope.launch {
            combine(speechEngine.isSpeaking, speechEngine.isListening, isNetworkOnline) { speaking, listening, online ->
                when {
                    listening -> DeadlySystemState.LISTENING
                    speaking -> DeadlySystemState.SPEAKING
                    !online || forceOfflineMode.value -> DeadlySystemState.OFFLINE
                    else -> DeadlySystemState.ONLINE
                }
            }.collect { calculatedState ->
                if (_systemState.value != DeadlySystemState.THINKING &&
                    _systemState.value != DeadlySystemState.ACTION_COMPLETED &&
                    _systemState.value != DeadlySystemState.ACTION_FAILED
                ) {
                    _systemState.value = calculatedState
                }
            }
        }

        // Insert initial briefing message if history is empty
        viewModelScope.launch {
            delay(400)
            if (messages.value.isEmpty()) {
                val greeting = "Good day, ${userTitle.value}. DEADLY AI operational systems are online. Arc reactor output nominal. Audio visualizers and offline tactical protocols armed."
                repository.saveMessage(
                    role = "assistant",
                    content = greeting,
                    isOffline = false
                )
            }
        }
    }

    fun submitPrompt(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank()) return

        if (hapticFeedbackEnabled.value) {
            deviceActionHandler.triggerHapticFeedback()
        }

        viewModelScope.launch {
            // Save user message to database
            repository.saveMessage(role = "user", content = trimmed)

            val isOnline = isNetworkOnline.value && !forceOfflineMode.value
            val effectiveApiKey = if (customApiKey.value.isNotBlank()) customApiKey.value else BuildConfig.GEMINI_API_KEY

            val isLikelyLocalCommand = isExplicitLocalIntent(trimmed)

            if (!isOnline || isLikelyLocalCommand || effectiveApiKey.isBlank()) {
                // Execute Offline Command Engine
                processOfflineDirective(trimmed)
            } else {
                // Execute Online Gemini 3.5 Flash Brain
                processOnlineDirective(trimmed, effectiveApiKey)
            }
        }
    }

    private suspend fun processOfflineDirective(input: String) {
        _systemState.value = DeadlySystemState.THINKING
        _statusDetail.value = "Executing local tactical protocol..."

        delay(150) // Subtle tactical processing interval

        val parsed = offlineEngine.execute(
            input = input,
            userTitle = userTitle.value,
            currentBatteryLevel = batteryStatus.value.levelPercentage,
            isCharging = batteryStatus.value.isCharging,
            onSaveNote = { title, content -> repository.createNote(title, content) },
            onSaveReminder = { title, time -> repository.createReminder(title, time) }
        )

        // Handle specific parsed actions
        when (parsed.actionType) {
            "NOTE" -> {
                val parts = parsed.actionPayload?.split("|||")
                val title = parts?.getOrNull(0) ?: "Tactical Note"
                val content = parts?.getOrNull(1) ?: input
                repository.createNote(title, content)
                _systemState.value = DeadlySystemState.ACTION_COMPLETED
                _statusDetail.value = "Note recorded in local memory."
            }
            "REMINDER" -> {
                val parts = parsed.actionPayload?.split("|||")
                val title = parts?.getOrNull(0) ?: "Reminder"
                val time = parts?.getOrNull(1)?.toLongOrNull() ?: (System.currentTimeMillis() + 3600000)
                repository.createReminder(title, time)
                _systemState.value = DeadlySystemState.ACTION_COMPLETED
                _statusDetail.value = "Reminder logged in offline memory."
            }
            "TIMER" -> {
                val seconds = parsed.actionPayload?.toIntOrNull() ?: 60
                startInAppTimer(seconds, "DEADLY AI Timer")
                _systemState.value = DeadlySystemState.ACTION_COMPLETED
                _statusDetail.value = "Countdown timer active."
            }
            "ALARM", "APP", "CALC" -> {
                _systemState.value = DeadlySystemState.ACTION_COMPLETED
                _statusDetail.value = "Directive executed successfully."
            }
            else -> {
                _systemState.value = if (!isNetworkOnline.value) DeadlySystemState.OFFLINE else DeadlySystemState.ONLINE
                _statusDetail.value = "Offline protocol processed."
            }
        }

        // Save assistant message to database
        repository.saveMessage(
            role = "assistant",
            content = parsed.responseText,
            isOffline = true,
            actionType = parsed.actionType,
            actionPayload = parsed.actionPayload
        )

        // Speak aloud
        val isHindi = isHindiText(input) || isHindiText(parsed.responseText)
        speechEngine.speak(parsed.responseText, isHindiOrHinglish = isHindi)

        delay(1500)
        resetIdleState()
    }

    private suspend fun processOnlineDirective(input: String, apiKey: String) {
        _systemState.value = DeadlySystemState.THINKING
        _statusDetail.value = "Transmitting to Gemini 3.5 Flash neural core..."

        val systemInstruction = buildSystemPrompt()
        val result = repository.queryGemini(
            apiKey = apiKey,
            userPrompt = input,
            systemInstructionText = systemInstruction,
            recentContext = messages.value
        )

        result.fold(
            onSuccess = { responseText ->
                _systemState.value = DeadlySystemState.SPEAKING
                _statusDetail.value = "Neural output received."

                // Check for embedded action tags like [ACTION:SET_ALARM:06:00:Wake up]
                val (cleanedText, actionType, actionPayload) = parseAndExecuteActionTag(responseText)

                // Save message
                repository.saveMessage(
                    role = "assistant",
                    content = cleanedText,
                    isOffline = false,
                    actionType = actionType,
                    actionPayload = actionPayload
                )

                val isHindi = isHindiText(input) || isHindiText(cleanedText)
                speechEngine.speak(cleanedText, isHindiOrHinglish = isHindi)

                if (actionType != null) {
                    _systemState.value = DeadlySystemState.ACTION_COMPLETED
                }
                delay(2000)
                resetIdleState()
            },
            onFailure = { error ->
                // Fallback to offline engine seamlessly if API fails
                _statusDetail.value = "Neural uplink failed (${error.localizedMessage?.take(30)}...). Engaging local backup."
                processOfflineDirective(input)
            }
        )
    }

    private fun isExplicitLocalIntent(text: String): Boolean {
        val q = text.lowercase()
        return q.contains("set alarm") || q.contains("alarm laga") ||
                q.contains("set timer") || q.contains("start timer") ||
                q.contains("note:") || q.contains("take note") || q.contains("note banao") ||
                q.contains("battery status") || q.contains("battery kitni") ||
                q.startsWith("open ") || q.startsWith("launch ") || q.startsWith("kholo ") ||
                q.contains("calculate ") || q.contains("solve ")
    }

    private fun isHindiText(text: String): Boolean {
        val hindiKeywords = listOf("kaisa", "kaise", "kya", "baje", "samay", "mausam", "kholo", "laga", "banao", "yaad", "namaste", "aaj", "kal", "subah", "shaam")
        val lower = text.lowercase()
        return hindiKeywords.any { lower.contains(it) } || text.any { it in '\u0900'..'\u097F' }
    }

    private fun parseAndExecuteActionTag(response: String): Triple<String, String?, String?> {
        val actionRegex = Regex("\\[ACTION:([A-Z_]+):([^\\]]+)\\]")
        val match = actionRegex.find(response)
        if (match != null) {
            val type = match.groupValues[1]
            val payload = match.groupValues[2]
            val cleaned = response.replace(match.value, "").trim()

            when (type) {
                "SET_ALARM" -> {
                    val parts = payload.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 6
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    val msg = parts.getOrNull(2) ?: "DEADLY AI Alert"
                    deviceActionHandler.setSystemAlarm(h, m, msg)
                }
                "SET_TIMER" -> {
                    val parts = payload.split(":")
                    val sec = parts.getOrNull(0)?.toIntOrNull() ?: 60
                    val msg = parts.getOrNull(1) ?: "DEADLY AI Timer"
                    deviceActionHandler.startSystemTimer(sec, msg)
                    startInAppTimer(sec, msg)
                }
                "CREATE_NOTE" -> {
                    val parts = payload.split(":")
                    val title = parts.getOrNull(0) ?: "Tactical Note"
                    val content = parts.getOrNull(1) ?: payload
                    viewModelScope.launch { repository.createNote(title, content) }
                }
                "OPEN_APP" -> {
                    deviceActionHandler.launchAppByName(payload)
                }
            }
            return Triple(cleaned, type, payload)
        }
        return Triple(response, null, null)
    }

    private fun buildSystemPrompt(): String {
        val title = userTitle.value
        val battery = batteryStatus.value.levelPercentage
        val charging = if (batteryStatus.value.isCharging) "Charging" else "Discharging"
        val timeNow = SimpleDateFormat("hh:mm a, EEEE, MMM dd yyyy", Locale.getDefault()).format(Date())

        val rememberedFacts = memories.value.take(5).joinToString("; ") { "${it.key}: ${it.value}" }
        val memorySection = if (rememberedFacts.isNotBlank()) "User Profile Memory: $rememberedFacts." else ""

        return """
            You are DEADLY AI, an ultra-advanced personal AI assistant inspired by Tony Stark's futuristic assistants (J.A.R.V.I.S. / F.R.I.D.A.Y.).
            Personality: Highly intelligent, calm, confident, composed, and futuristic. You address the user respectfully as '$title'.
            Language capabilities: You understand and fluently speak English, Hindi, and Hinglish. Adapt naturally to the user's language style.
            Current telemetry:
            - Device Time: $timeNow
            - Power cells: $battery% ($charging)
            $memorySection
            
            Guidelines:
            - Be concise by default; provide deep tactical explanations only when asked.
            - Never pretend to perform an action that fails.
            - When the user asks you to set an alarm, start a timer, create a note, or open an app, you can include action tags at the very end of your response:
              * For alarm: [ACTION:SET_ALARM:HH:MM:Label] (e.g. [ACTION:SET_ALARM:06:30:Wake up])
              * For timer: [ACTION:SET_TIMER:Seconds:Label] (e.g. [ACTION:SET_TIMER:300:Tea])
              * For note: [ACTION:CREATE_NOTE:Title:Content]
              * For open app: [ACTION:OPEN_APP:AppName] (e.g. camera, calculator, browser)
            - Keep your tone composed, powerful, and cinematic.
        """.trimIndent()
    }

    private fun resetIdleState() {
        if (!speechEngine.isSpeaking.value && !speechEngine.isListening.value) {
            _systemState.value = if (isNetworkOnline.value && !forceOfflineMode.value) DeadlySystemState.ONLINE else DeadlySystemState.OFFLINE
            _statusDetail.value = "Systems operational. Awaiting next command."
        }
    }

    fun startVoiceInput() {
        if (hapticFeedbackEnabled.value) {
            deviceActionHandler.triggerHapticFeedback()
        }
        _systemState.value = DeadlySystemState.LISTENING
        _statusDetail.value = "Audio sensors active. Listening..."

        val preferredLang = when {
            languagePref.value.contains("Hindi") -> "hi-IN"
            else -> "en-IN"
        }

        speechEngine.startListening(
            preferredLanguage = preferredLang,
            onResult = { spokenText ->
                _statusDetail.value = "Audio parsed: \"$spokenText\""
                submitPrompt(spokenText)
            },
            onError = { error ->
                _statusDetail.value = error
                _systemState.value = if (isNetworkOnline.value) DeadlySystemState.ONLINE else DeadlySystemState.OFFLINE
            }
        )
    }

    fun stopSpeaking() {
        speechEngine.stopSpeaking()
        resetIdleState()
    }

    fun replaySpeech(text: String) {
        val isHindi = isHindiText(text)
        speechEngine.speak(text, isHindiOrHinglish = isHindi)
    }

    // In-app countdown timer
    fun startInAppTimer(seconds: Int, label: String) {
        timerJob?.cancel()
        _activeTimer.value = ActiveInAppTimer(
            initialSeconds = seconds,
            remainingSeconds = seconds,
            label = label,
            isRunning = true
        )

        timerJob = viewModelScope.launch {
            while (true) {
                val current = _activeTimer.value ?: break
                if (!current.isRunning || current.remainingSeconds <= 0) break
                delay(1000)
                val remaining = current.remainingSeconds - 1
                if (remaining <= 0) {
                    _activeTimer.value = current.copy(remainingSeconds = 0, isRunning = false)
                    speechEngine.speak("Attention $userTitle: Tactical timer for ${current.label} has elapsed.")
                    deviceActionHandler.triggerHapticFeedback()
                    break
                } else {
                    _activeTimer.value = current.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    fun cancelInAppTimer() {
        timerJob?.cancel()
        _activeTimer.value = null
    }

    fun clearChatHistory() {
        viewModelScope.launch { repository.clearChatHistory() }
    }

    // Notes
    fun addNote(title: String, content: String, category: String = "General") {
        viewModelScope.launch { repository.createNote(title, content, category) }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    // Reminders
    fun addReminder(title: String, timestamp: Long) {
        viewModelScope.launch { repository.createReminder(title, timestamp) }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch { repository.toggleReminder(reminder) }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch { repository.deleteReminder(id) }
    }

    // Memories
    fun addMemory(key: String, value: String, category: String = "Preference") {
        viewModelScope.launch { repository.saveMemory(key, value, category) }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch { repository.deleteMemory(id) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _statusDetail.value = "All tactical databases purged."
        }
    }

    // Settings update
    fun updateUserTitle(title: String) {
        userTitle.value = title
        prefs.edit().putString("user_title", title).apply()
    }

    fun updateSpeechPitch(pitch: Float) {
        speechPitch.value = pitch
        speechEngine.speechPitch = pitch
        prefs.edit().putFloat("speech_pitch", pitch).apply()
    }

    fun updateSpeechRate(rate: Float) {
        speechRate.value = rate
        speechEngine.speechRate = rate
        prefs.edit().putFloat("speech_rate", rate).apply()
    }

    fun updateLanguagePref(lang: String) {
        languagePref.value = lang
        prefs.edit().putString("pref_language", lang).apply()
    }

    fun updateForceOffline(offline: Boolean) {
        forceOfflineMode.value = offline
        prefs.edit().putBoolean("force_offline", offline).apply()
        _systemState.value = if (offline) DeadlySystemState.OFFLINE else DeadlySystemState.ONLINE
    }

    fun updateHighFidelityVisuals(enabled: Boolean) {
        highFidelityVisuals.value = enabled
        prefs.edit().putBoolean("high_fidelity_visuals", enabled).apply()
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        hapticFeedbackEnabled.value = enabled
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }

    fun updateCustomApiKey(key: String) {
        customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine.release()
        batteryMonitor.unregister()
        networkMonitor.unregister()
        timerJob?.cancel()
    }
}
