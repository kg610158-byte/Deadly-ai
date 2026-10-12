package com.example.deadlyai.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

sealed class DeviceActionResult {
    data class Success(val message: String, val details: String? = null) : DeviceActionResult()
    data class Failure(val reason: String, val suggestion: String? = null) : DeviceActionResult()
}

class DeviceActionHandler(private val context: Context) {

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {
            // Non-critical
        }
    }

    fun setSystemAlarm(hour: Int, minute: Int, message: String): DeviceActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message.ifBlank { "DEADLY AI Alert" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                val ampm = if (hour >= 12) "PM" else "AM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val formattedTime = String.format("%02d:%02d %s", displayHour, minute, ampm)
                DeviceActionResult.Success(
                    message = "Alarm successfully configured for $formattedTime.",
                    details = "Alarm label: $message"
                )
            } else {
                DeviceActionResult.Failure(
                    reason = "No compatible System Clock app found to handle alarm intent.",
                    suggestion = "You can use DEADLY AI's built-in in-app timer or reminder instead."
                )
            }
        } catch (e: Exception) {
            DeviceActionResult.Failure(
                reason = "Failed to dispatch alarm intent: ${e.localizedMessage ?: "Unknown error"}",
                suggestion = "Ensure SET_ALARM permission is available or set alarm manually."
            )
        }
    }

    fun startSystemTimer(seconds: Int, message: String): DeviceActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message.ifBlank { "DEADLY AI Timer" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                val mins = seconds / 60
                val secs = seconds % 60
                val timeStr = if (mins > 0) "$mins minutes ${if (secs > 0) "$secs seconds" else ""}" else "$secs seconds"
                DeviceActionResult.Success(
                    message = "Timer activated for $timeStr.",
                    details = "Label: $message"
                )
            } else {
                // In-app fallback available
                DeviceActionResult.Failure(
                    reason = "System clock app not available for external timer.",
                    suggestion = "DEADLY AI has launched the tactical in-app countdown timer."
                )
            }
        } catch (e: Exception) {
            DeviceActionResult.Failure(
                reason = "Timer dispatch failed: ${e.localizedMessage}",
                suggestion = "Switched to in-app timer."
            )
        }
    }

    fun launchAppByName(query: String): DeviceActionResult {
        val q = query.trim().lowercase()
        return try {
            val intent: Intent? = when {
                q.contains("camera") -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                q.contains("calc") -> {
                    getLaunchIntentForCategory(Intent.CATEGORY_APP_CALCULATOR)
                        ?: findPackageMatching(listOf("com.google.android.calculator", "com.android.calculator2"))
                }
                q.contains("browser") || q.contains("chrome") || q.contains("web") -> {
                    getLaunchIntentForCategory(Intent.CATEGORY_APP_BROWSER)
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                q.contains("map") -> {
                    getLaunchIntentForCategory(Intent.CATEGORY_APP_MAPS)
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                q.contains("dial") || q.contains("phone") || q.contains("call") -> {
                    Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                q.contains("setting") -> {
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                q.contains("clock") || q.contains("alarm") -> {
                    Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                else -> {
                    // Try to search installed apps
                    findInstalledApp(q)
                }
            }

            if (intent != null && intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                DeviceActionResult.Success("Opened ${query.replaceFirstChar { it.uppercase() }}.")
            } else {
                DeviceActionResult.Failure(
                    reason = "Application matching '$query' was not found on this device.",
                    suggestion = "Supported shortcuts: Camera, Calculator, Browser, Settings, Phone, Maps, Clock."
                )
            }
        } catch (e: Exception) {
            DeviceActionResult.Failure("Failed to launch application: ${e.localizedMessage}")
        }
    }

    fun shareText(title: String, text: String): DeviceActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            DeviceActionResult.Success("Dispatched data to system share sheet.")
        } catch (e: Exception) {
            DeviceActionResult.Failure("Share operation failed: ${e.localizedMessage}")
        }
    }

    private fun getLaunchIntentForCategory(category: String): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else null
    }

    private fun findPackageMatching(packageNames: List<String>): Intent? {
        val pm = context.packageManager
        for (pkg in packageNames) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) return launchIntent
        }
        return null
    }

    private fun findInstalledApp(keyword: String): Intent? {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        for (pkgInfo in packages) {
            val appName = pkgInfo.applicationInfo?.loadLabel(pm)?.toString()?.lowercase() ?: ""
            if (appName.contains(keyword) || pkgInfo.packageName.lowercase().contains(keyword)) {
                return pm.getLaunchIntentForPackage(pkgInfo.packageName)
            }
        }
        return null
    }
}
