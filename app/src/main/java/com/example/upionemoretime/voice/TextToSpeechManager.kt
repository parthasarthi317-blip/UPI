package com.example.upionemoretime.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(
    context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        tts = TextToSpeech(
            context,
            this
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts?.language = Locale.US

            initialized = true
        }
    }

    fun speak(text: String) {

        android.util.Log.d(
            "TTS_DEBUG",
            "initialized = $initialized"
        )

        if (!initialized) return

        val result = tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "history_test"
        )

        android.util.Log.d(
            "TTS_RESULT",
            "result = $result"
        )
    }

    fun shutdown() {

        tts?.stop()

        tts?.shutdown()
    }
}