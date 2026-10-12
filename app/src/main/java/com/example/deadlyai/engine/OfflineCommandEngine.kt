package com.example.deadlyai.engine

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ParsedOfflineAction(
    val handled: Boolean,
    val responseText: String,
    val actionType: String? = null, // "ALARM", "TIMER", "NOTE", "APP", "REMINDER", "CALC"
    val actionPayload: String? = null
)

class OfflineCommandEngine(
    private val deviceActionHandler: DeviceActionHandler
) {

    fun execute(
        input: String,
        userTitle: String = "Sir",
        currentBatteryLevel: Int = 100,
        isCharging: Boolean = false,
        onSaveNote: suspend (String, String) -> Unit,
        onSaveReminder: suspend (String, Long) -> Unit
    ): ParsedOfflineAction {
        val raw = input.trim()
        val query = raw.lowercase()

        // 1. Greetings & System Identity
        if (query.matches(Regex("^(hello|hi|hey|namaste|salaam|deadly|hey deadly|hello deadly).*"))) {
            val greeting = getDayPeriodGreeting()
            return ParsedOfflineAction(
                handled = true,
                responseText = "$greeting, $userTitle. DEADLY AI tactical subsystems are operational. How may I assist you?"
            )
        }

        // 2. Weather query offline check (User requirement example)
        if (query.contains("mausam") || query.contains("weather") || query.contains("temperature") || query.contains("baarish") || query.contains("rain")) {
            return ParsedOfflineAction(
                handled = true,
                responseText = "$userTitle, I need an active neural uplink (internet connection) to retrieve real-time atmospheric and weather telemetry."
            )
        }

        // 3. Time Query
        if (!query.contains("timer") && (Regex("\\b(time|samay|waqt)\\b").containsMatchIn(query) || query.contains("kitne baje"))) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = sdf.format(Date())
            return ParsedOfflineAction(
                handled = true,
                responseText = "$userTitle, current device time is $time."
            )
        }

        // 4. Date Query
        if (query.contains("date") || query.contains("taarikh") || query.contains("tarikh") || query.contains("aaj ka din") || query.contains("which day")) {
            val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            val date = sdf.format(Date())
            return ParsedOfflineAction(
                handled = true,
                responseText = "$userTitle, today is $date."
            )
        }

        // 5. Battery Status
        if (query.contains("battery") || query.contains("charging") || query.contains("power status")) {
            val state = if (isCharging) "plugged in and actively charging" else "running on internal cell power"
            return ParsedOfflineAction(
                handled = true,
                responseText = "$userTitle, power cells are at $currentBatteryLevel% capacity. Subsystem is $state."
            )
        }

        // 6. Math Calculation
        val mathMatch = extractMathExpression(query)
        if (mathMatch != null) {
            val calcResult = evaluateMath(mathMatch)
            if (calcResult != null) {
                return ParsedOfflineAction(
                    handled = true,
                    responseText = "Tactical calculation result: $mathMatch = $calcResult.",
                    actionType = "CALC",
                    actionPayload = "$calcResult"
                )
            }
        }

        // 7. Alarm Command (English, Hindi, Hinglish)
        // e.g. "alarm laga do 6 baje", "set alarm for 7:30 am", "kal subah 6 baje alarm laga do"
        if (query.contains("alarm")) {
            val alarmTime = parseTimeFromText(query)
            if (alarmTime != null) {
                val (hour, minute, display) = alarmTime
                val result = deviceActionHandler.setSystemAlarm(hour, minute, "DEADLY AI Protocol")
                return when (result) {
                    is DeviceActionResult.Success -> ParsedOfflineAction(
                        handled = true,
                        responseText = "Alarm successfully scheduled for $display, $userTitle.",
                        actionType = "ALARM",
                        actionPayload = "$hour:$minute"
                    )
                    is DeviceActionResult.Failure -> ParsedOfflineAction(
                        handled = true,
                        responseText = "$userTitle, ${result.reason} ${result.suggestion ?: ""}"
                    )
                }
            } else {
                return ParsedOfflineAction(
                    handled = true,
                    responseText = "$userTitle, please specify the exact time for the alarm (e.g. 'alarm for 6:30 AM' or 'subah 6 baje alarm')."
                )
            }
        }

        // 8. Timer Command
        // e.g. "timer for 5 minutes", "10 second timer", "5 minute ka timer"
        if (query.contains("timer")) {
            val seconds = parseTimerSeconds(query)
            if (seconds > 0) {
                val result = deviceActionHandler.startSystemTimer(seconds, "DEADLY AI Tactical Timer")
                val mins = seconds / 60
                val secs = seconds % 60
                val timeLabel = if (mins > 0) "$mins minute(s)" else "$secs seconds"
                return when (result) {
                    is DeviceActionResult.Success -> ParsedOfflineAction(
                        handled = true,
                        responseText = "Timer activated for $timeLabel, $userTitle.",
                        actionType = "TIMER",
                        actionPayload = "$seconds"
                    )
                    is DeviceActionResult.Failure -> ParsedOfflineAction(
                        handled = true,
                        responseText = "In-app tactical countdown initiated for $timeLabel.",
                        actionType = "TIMER",
                        actionPayload = "$seconds"
                    )
                }
            } else {
                return ParsedOfflineAction(
                    handled = true,
                    responseText = "$userTitle, please state the timer duration (e.g., 'timer for 5 minutes' or 'timer 30 seconds')."
                )
            }
        }

        // 9. Notes Command
        // e.g. "note: buy groceries", "take a note buy milk", "note banao meeting at 4"
        if (query.startsWith("note:") || query.startsWith("take note") || query.startsWith("note lelo") || query.startsWith("note banao") || query.startsWith("add note")) {
            val cleanContent = raw
                .replaceFirst(Regex("^(?i)(note:|take note of|take note|note lelo|note banao|add note|save note)\\s*"), "")
                .trim()
            if (cleanContent.isNotBlank()) {
                val title = if (cleanContent.length > 25) cleanContent.take(25) + "..." else cleanContent
                return ParsedOfflineAction(
                    handled = true,
                    responseText = "Log saved to tactical offline memory: '$cleanContent'.",
                    actionType = "NOTE",
                    actionPayload = "$title|||$cleanContent"
                )
            }
        }

        // 10. Reminder Command
        if (query.contains("remind me") || query.contains("reminder") || query.contains("yaad dilana")) {
            val reminderText = raw
                .replaceFirst(Regex("^(?i)(remind me to|reminder for|reminder:|mujhe yaad dilana ki|yaad dilana)\\s*"), "")
                .trim()
            if (reminderText.isNotBlank()) {
                val dueTime = System.currentTimeMillis() + (3600 * 1000) // Default 1 hour if unspecified
                return ParsedOfflineAction(
                    handled = true,
                    responseText = "Reminder created: '$reminderText'. Scheduled in offline memory.",
                    actionType = "REMINDER",
                    actionPayload = "$reminderText|||$dueTime"
                )
            }
        }

        // 11. App Launching
        if (query.startsWith("open ") || query.startsWith("launch ") || query.startsWith("kholo ")) {
            val appTarget = query
                .replaceFirst(Regex("^(?i)(open|launch|kholo)\\s+"), "")
                .trim()
            val result = deviceActionHandler.launchAppByName(appTarget)
            return when (result) {
                is DeviceActionResult.Success -> ParsedOfflineAction(
                    handled = true,
                    responseText = "Opening $appTarget, $userTitle.",
                    actionType = "APP",
                    actionPayload = appTarget
                )
                is DeviceActionResult.Failure -> ParsedOfflineAction(
                    handled = true,
                    responseText = "$userTitle, ${result.reason} ${result.suggestion ?: ""}"
                )
            }
        }

        // 12. Diagnostics / Status / Help
        if (query.contains("help") || query.contains("command") || query.contains("kya kar sakte ho") || query.contains("what can you do") || query.contains("diagnostics") || query.contains("status")) {
            val helpText = """
                DEADLY AI Subsystems Overview:
                • Time & Date: "What time is it", "Aaj ki date kya hai"
                • Alarms: "Set alarm for 6:00 AM", "Subah 7 baje alarm"
                • Timers: "Timer for 5 minutes", "30 seconds timer"
                • Calculations: "Calculate 25 * 40", "Solve 1200 / 4"
                • Notes: "Note: Buy circuit boards", "Note lelo Groceries"
                • Reminders: "Remind me to call Mark at 4 PM"
                • Battery Telemetry: "Battery status", "Power level"
                • App Launch: "Open Camera", "Open Calculator", "Open Settings"
                • Online Uplink: Full conversational AI, coding, study, translation, and Hinglish understanding via Gemini 3.5 Flash.
            """.trimIndent()
            return ParsedOfflineAction(
                handled = true,
                responseText = helpText
            )
        }

        // If not recognized offline
        return ParsedOfflineAction(
            handled = false,
            responseText = "$userTitle, offline command not recognized in local protocols. Connect to network for full Gemini 3.5 Flash neural brain, or say 'help' for offline protocols."
        )
    }

    private fun getDayPeriodGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "System operational"
        }
    }

    private fun parseTimeFromText(text: String): Triple<Int, Int, String>? {
        // Match patterns like "6:30 am", "6:30 pm", "6 am", "18:00", "6 baje", "subah 6 baje", "shaam 7 baje"
        val regex = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm|baje)?", Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)

        var isMorning = text.contains("subah") || text.contains("morning")
        var isEvening = text.contains("shaam") || text.contains("sham") || text.contains("raat") || text.contains("night") || text.contains("evening")

        while (matcher.find()) {
            val hourStr = matcher.group(1) ?: continue
            var hour = hourStr.toIntOrNull() ?: continue
            val minStr = matcher.group(2)
            val minute = minStr?.toIntOrNull() ?: 0
            val ampm = matcher.group(3)?.lowercase()

            if (ampm == "pm" || isEvening) {
                if (hour < 12) hour += 12
            } else if (ampm == "am" || isMorning) {
                if (hour == 12) hour = 0
            }

            if (hour in 0..23 && minute in 0..59) {
                val displayAmPm = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                val display = String.format("%02d:%02d %s", displayHour, minute, displayAmPm)
                return Triple(hour, minute, display)
            }
        }
        return null
    }

    private fun parseTimerSeconds(text: String): Int {
        val minRegex = Regex("(\\d+)\\s*(?:min|minute|minutes|m)")
        val secRegex = Regex("(\\d+)\\s*(?:sec|second|seconds|s)")
        val hourRegex = Regex("(\\d+)\\s*(?:hour|hours|hr|hrs|h)")

        var total = 0
        hourRegex.find(text)?.let { total += (it.groupValues[1].toIntOrNull() ?: 0) * 3600 }
        minRegex.find(text)?.let { total += (it.groupValues[1].toIntOrNull() ?: 0) * 60 }
        secRegex.find(text)?.let { total += (it.groupValues[1].toIntOrNull() ?: 0) }

        if (total == 0) {
            // Check standalone number: "timer 5" -> default minutes
            val numRegex = Regex("timer\\s+(?:for\\s+)?(\\d+)")
            numRegex.find(text)?.let { total = (it.groupValues[1].toIntOrNull() ?: 0) * 60 }
        }
        return total
    }

    private fun extractMathExpression(text: String): String? {
        val clean = text.replace("calculate", "").replace("solve", "").replace("what is", "").replace("x", "*").trim()
        val mathRegex = Regex("[0-9\\.\\+\\-\\*/\\(\\)\\s]{3,}")
        val match = mathRegex.find(clean)?.value?.trim()
        return if (match != null && match.any { it in "+-*/" }) match else null
    }

    private fun evaluateMath(expr: String): Double? {
        return try {
            val sanitized = expr.replace(" ", "")
            // Simple 2-operand or basic tokenizer evaluator
            val parts = mutableListOf<String>()
            var cur = StringBuilder()
            for (ch in sanitized) {
                if (ch in "+-*/") {
                    if (cur.isNotEmpty()) {
                        parts.add(cur.toString())
                        cur = StringBuilder()
                    }
                    parts.add(ch.toString())
                } else if (ch.isDigit() || ch == '.') {
                    cur.append(ch)
                }
            }
            if (cur.isNotEmpty()) parts.add(cur.toString())

            if (parts.size >= 3) {
                var acc = parts[0].toDoubleOrNull() ?: return null
                var i = 1
                while (i < parts.size) {
                    val op = parts[i]
                    val nextVal = parts.getOrNull(i + 1)?.toDoubleOrNull() ?: return null
                    acc = when (op) {
                        "+" -> acc + nextVal
                        "-" -> acc - nextVal
                        "*" -> acc * nextVal
                        "/" -> if (nextVal != 0.0) acc / nextVal else return null
                        else -> acc
                    }
                    i += 2
                }
                Math.round(acc * 100.0) / 100.0
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
