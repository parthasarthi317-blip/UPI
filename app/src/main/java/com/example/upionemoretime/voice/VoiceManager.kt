package com.example.upionemoretime.voice

import android.content.Context
import android.util.Log
import androidx.navigation.NavController

class VoiceManager(
    private val context: Context
) {

    private val speechManager =
        SpeechRecognitionManager(context)

    private val ttsManager =
        TextToSpeechManager(context)

    fun listenAndHandle(
        navController: NavController

    ) {
        ttsManager.speak(
            "Listening"
        )

        speechManager.startListening(

            onResult = { result ->
                ttsManager.speak(result)

                val command =
                    VoiceCommandParser.parse(result)
                Log.d("VOICE_DEBUG", "Command = $command")

                if (command == VoiceCommand.Unknown) {

                    ttsManager.speak(
                        "I don't understand that command yet"
                    )

                } else {

                    VoiceNavigationHandler.handleCommand(
                        command = command,
                        navController = navController,
                        ttsManager = ttsManager
                    )
                }
            },

            onError = { error ->

                val message = when {

                    error.contains(
                        "NO_MATCH",
                        ignoreCase = true
                    ) -> {
                        "I didn't hear anything. Please try again."
                    }

                    error.contains(
                        "SPEECH_TIMEOUT",
                        ignoreCase = true
                    ) -> {
                        "I didn't hear anything"
                    }

                    error.contains(
                        "NETWORK",
                        ignoreCase = true
                    ) -> {
                        "Please check your internet connection"
                    }

                    else -> {
                        "Sorry, something went wrong"
                    }
                }

                ttsManager.speak(message)
            }
        )
    }

    fun speak(
        text: String
    ) {
        ttsManager.speak(text)
    }

    fun destroy() {
        speechManager.destroy()
        ttsManager.shutdown()
    }
}