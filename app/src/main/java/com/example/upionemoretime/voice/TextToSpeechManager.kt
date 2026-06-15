package com.example.upionemoretime.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.speech.tts.UtteranceProgressListener

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
    private var onSpeechDone: (() -> Unit)? = null
    fun setOnSpeechDoneListener(
        listener: () -> Unit
    ) {
        onSpeechDone = listener
    }
    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts?.language = Locale.US

            initialized = true
            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        android.util.Log.d(
                            "TTS_DEBUG",
                            "onDone = $utteranceId"
                        )
                        onSpeechDone?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                }
            )
        }
    }

    fun speak(text: String) {

        android.util.Log.d(
            "TTS_DEBUG",
            "initialized = $initialized"
        )

        if (!initialized) return

        val utteranceId =
            System.currentTimeMillis().toString()

        val result = tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
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