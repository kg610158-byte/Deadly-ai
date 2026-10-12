package com.example.deadlyai.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    private val _lastSpokenText = MutableStateFlow("")
    val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

    var speechPitch: Float = 0.82f // Deep, mature robotic pitch
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var speechRate: Float = 0.96f // Controlled cinematic pacing
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var speechVolume: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.language = Locale.ENGLISH
            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechRate)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String, isHindiOrHinglish: Boolean = false) {
        if (!isTtsInitialized || text.isBlank()) return

        stopSpeaking()
        _lastSpokenText.value = text

        // Language tone selection
        if (isHindiOrHinglish) {
            val hindiLocale = Locale("hi", "IN")
            if (tts?.isLanguageAvailable(hindiLocale) == TextToSpeech.LANG_AVAILABLE ||
                tts?.isLanguageAvailable(hindiLocale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                tts?.language = hindiLocale
            } else {
                tts?.language = Locale.ENGLISH
            }
        } else {
            tts?.language = Locale.US
        }

        tts?.setPitch(speechPitch)
        tts?.setSpeechRate(speechRate)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, speechVolume)
        }

        // Clean out action brackets or markdown formatting from spoken speech
        val cleanedText = text
            .replace(Regex("\\[ACTION:[^\\]]+\\]"), "")
            .replace(Regex("[*#_`~]"), "")
            .trim()

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, params, "DEADLY_SPEECH_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun replayLast() {
        val text = _lastSpokenText.value
        if (text.isNotBlank()) {
            speak(text)
        }
    }

    fun startListening(
        preferredLanguage: String = "en-IN",
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition hardware/service is unavailable on this device.")
            return
        }

        stopSpeaking()
        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS to 0.0 - 1.0 range for HUD waveform animation
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsAudioLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsAudioLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsAudioLevel.value = 0f
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client recognition error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                        SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error code: $error"
                    }
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsAudioLevel.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        onResult(spokenText)
                    } else {
                        onError("No audible words detected.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguage)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, preferredLanguage)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, preferredLanguage)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Failed to initiate voice listener: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        _rmsAudioLevel.value = 0f
    }

    fun release() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
        tts = null
    }
}
