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

    fun speak(
        text: String
    ) {

        if (!initialized) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    fun shutdown() {

        tts?.stop()

        tts?.shutdown()
    }
}