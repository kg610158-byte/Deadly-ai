# DEADLY AI — Advanced Personal AI Assistant

An advanced, futuristic personal AI assistant inspired by Tony Stark's Iron Man technology (J.A.R.V.I.S. / F.R.I.D.A.Y.). Built with Kotlin, Jetpack Compose, Material 3, Room Database, Android Native Voice & Device Integrations, and Google Gemini 3.5 Flash.

---

## ⚡ Core Identity & Styling

- **Assistant Name**: DEADLY AI
- **Voice Personality**: Deep, mature, robotic, cinematic AI voice (`pitch: 0.82x`, `rate: 0.96x`), composed pacing, capable of speaking and understanding English, Hindi, and Hinglish.
- **Futuristic Palette**:
  - Neon Red (`#FF1744`) & Electric Cyan Blue (`#00E5FF`)
  - Deep Black (`#0A0D14`) background & Dark Graphite (`#121620`) glassmorphic panels
  - Animated Arc Reactor with counter-rotating multi-segment rings and pulsing energy core
  - Dynamic audio waveform visualizer responding to mic RMS levels and speech playback

---

## 🚀 System Capabilities & Feature Matrix

### Features That Work Immediately (Zero Setup / Offline)
1. **Interactive Animated Arc Reactor HUD**:
   - Multi-segment rotating outer and inner rings with central pulsing energy core.
   - Tap reactor to trigger voice listening or silence speaking assistant.
2. **Offline Command Subsystem**:
   - Device time and date queries ("What time is it", "Aaj ki date kya hai").
   - Hindi & Hinglish natural command parsing ("Kal subah 6 baje alarm laga do").
   - Offline math calculation engine ("calculate 45 * 10", "solve 1500 / 12").
   - Offline weather limitation explanation ("Sir, I need an active neural uplink...").
   - Hardware battery telemetry & charging monitor.
3. **Android Device Actions**:
   - **Alarms**: Dispatches Android `AlarmClock.ACTION_SET_ALARM` with label and time.
   - **Timers**: Dispatches `AlarmClock.ACTION_SET_TIMER` with fallback in-app tactical countdown.
   - **Application Launching**: Direct intents to launch Camera, Calculator, Settings, Phone Dialer, Maps, Browser.
   - **Tactical Haptics**: Vibration feedback on taps and command completions.
4. **Local Tactical Memory Vault (Room Database)**:
   - **Notes**: Offline note-taking, search, deletion, and export.
   - **Reminders**: Offline scheduled reminders with toggleable status.
   - **Long-Term Memory**: Persistent store for user profile facts and preferences.
   - **Communication Logs**: Full searchable transcripts with audio replay and copy buttons.
5. **Speech Subsystem**:
   - Android native `SpeechRecognizer` with real-time audio RMS waveform feedback.
   - Android native `TextToSpeech` with deep cinematic pitch, speed sliders, and voice testing.
6. **Performance & Low-End Mode**:
   - Dedicated toggle to switch between Full High-Fidelity HUD animations and low-spec phone optimization.

### Features Connected via Neural Uplink (Cloud AI)
1. **Gemini 3.5 Flash Neural Brain**:
   - Deep conversational abilities, complex reasoning, study assistance, coding, translation, and Hinglish understanding.
   - Automatic tool detection: Gemini attaches `[ACTION:...]` tags which DEADLY AI executes directly on Android device hardware.
   - Automatic fallback: If internet drops or API key is absent, requests are gracefully routed to the offline command engine without crash.

---

## 🔑 API Keys & Configuration Steps

### 1. Gemini API Key Configuration
- DEADLY AI connects to the Gemini REST API via `gemini-3.5-flash`.
- In AI Studio: Open the **Secrets panel** in the AI Studio UI and provide your `GEMINI_API_KEY`. It is injected into `BuildConfig.GEMINI_API_KEY` via `.env.example`.
- Alternatively: Open the **CONFIG** tab inside DEADLY AI and input a custom Gemini API key into the "Custom Gemini API Key Override" field.

### 2. Device Permissions
- **Record Audio (`RECORD_AUDIO`)**: Prompted at runtime when tapping the microphone button or arc reactor.
- **Internet (`INTERNET`, `ACCESS_NETWORK_STATE`)**: Used for online Gemini neural brain.
- **Alarms (`com.android.alarm.permission.SET_ALARM`)**: Used for system alarm intents.
- **Vibrate (`VIBRATE`)**: Used for tactical HUD feedback.

---

## 📦 Android Packaging & Building

- **Compile Applet**: Run `compile_applet` tool or `gradle assembleDebug` to produce the Android APK.
- **Run Unit Tests**: `gradle :app:testDebugUnitTest` executes local JVM and Robolectric tests for all commands and parsing routines.
- **Exporting Project**: Use the AI Studio menu to export the project as a ZIP archive or push to GitHub.
