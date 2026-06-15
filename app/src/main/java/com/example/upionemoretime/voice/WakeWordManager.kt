package com.example.upionemoretime.voice

import android.content.Context

class WakeWordManager(
    private val speechManager: SpeechRecognitionManager
) {
    private var isListening = false

    fun startListening(
        onWakeWordDetected: () -> Unit
    ) {

        if (isListening) return

        isListening = true

        listenForWakeWord(
            onWakeWordDetected
        )
    }

    private fun listenForWakeWord(
        onWakeWordDetected: () -> Unit
    ) {

        if (!isListening) return

        speechManager.startListening(

            onResult = { result ->

                val text =
                    result.lowercase().trim()

                if (
                    text.contains("hey assistant")
                ) {

                    isListening = false

                    onWakeWordDetected()

                } else {

                    listenForWakeWord(
                        onWakeWordDetected
                    )
                }
            },

            onError = { error ->

                if (
                    error.contains("ERROR_CLIENT") ||
                    error.contains("ERROR_BUSY")
                ) {
                    return@startListening
                }

                if (isListening) {

                    listenForWakeWord(
                        onWakeWordDetected
                    )
                }
            }
        )
    }

    fun stopListening() {
        isListening = false
        speechManager.stopListening()
    }
}