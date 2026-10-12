package com.example.deadlyai.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryStatus(
    val levelPercentage: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false
)

class BatteryMonitor(private val context: Context) {

    private val _batteryStatus = MutableStateFlow(BatteryStatus())
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.let { updateFromIntent(it) }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(receiver, filter)
        stickyIntent?.let { updateFromIntent(it) }
    }

    private fun updateFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val percentage = if (level >= 0 && scale > 0) {
            ((level / scale.toFloat()) * 100).toInt()
        } else 100

        _batteryStatus.value = BatteryStatus(
            levelPercentage = percentage,
            isCharging = isCharging
        )
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
    }
}
