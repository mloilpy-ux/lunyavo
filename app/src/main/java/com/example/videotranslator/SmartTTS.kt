package com.example.videotranslator

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SmartTTS(context: Context, onReady: () -> Unit) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("ru", "RU")
            isReady = true
        }
    }

    fun applySettings(speed: Float, pitch: Float) {
        if (!isReady) return
        tts.setSpeechRate(speed)
        tts.setPitch(pitch)
    }

    fun speak(text: String) {
        if (isReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TranslationID")
    }

    fun stop() {
        if (isReady) tts.stop()
    }
}
