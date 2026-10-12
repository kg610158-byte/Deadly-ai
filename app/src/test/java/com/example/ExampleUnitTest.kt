package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.deadlyai.engine.DeviceActionHandler
import com.example.deadlyai.engine.OfflineCommandEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    private lateinit var commandEngine: OfflineCommandEngine

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deviceActionHandler = DeviceActionHandler(context)
        commandEngine = OfflineCommandEngine(deviceActionHandler)
    }

    @Test
    fun testOfflineGreeting() {
        val result = commandEngine.execute(
            input = "Hello Deadly",
            userTitle = "Sir",
            currentBatteryLevel = 90,
            isCharging = false,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        assertTrue(result.responseText.contains("DEADLY AI"))
    }

    @Test
    fun testOfflineWeatherResponse() {
        val result = commandEngine.execute(
            input = "Aaj mausam kaisa hai?",
            userTitle = "Sir",
            currentBatteryLevel = 90,
            isCharging = false,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        assertTrue(result.responseText.contains("active neural uplink") || result.responseText.contains("internet connection"))
    }

    @Test
    fun testOfflineMathCalculation() {
        val result = commandEngine.execute(
            input = "calculate 45 * 10",
            userTitle = "Sir",
            currentBatteryLevel = 85,
            isCharging = true,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        assertEquals("CALC", result.actionType)
        assertTrue(result.responseText.contains("450"))
    }

    @Test
    fun testOfflineTimerParsing() {
        val result = commandEngine.execute(
            input = "timer for 5 minutes",
            userTitle = "Sir",
            currentBatteryLevel = 80,
            isCharging = false,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        assertEquals("TIMER", result.actionType)
        assertEquals("300", result.actionPayload)
    }

    @Test
    fun testOfflineAlarmParsingHinglish() {
        val result = commandEngine.execute(
            input = "kal subah 6 baje alarm laga do",
            userTitle = "Sir",
            currentBatteryLevel = 75,
            isCharging = false,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        // Verified alarm intent attempt
        assertTrue(result.responseText.contains("Alarm") || result.responseText.contains("alarm"))
    }

    @Test
    fun testOfflineNoteDirective() {
        val result = commandEngine.execute(
            input = "Note: inspect thrusters and arc reactor core",
            userTitle = "Sir",
            currentBatteryLevel = 95,
            isCharging = true,
            onSaveNote = { _, _ -> },
            onSaveReminder = { _, _ -> }
        )
        assertTrue(result.handled)
        assertEquals("NOTE", result.actionType)
        assertNotNull(result.actionPayload)
    }
}
