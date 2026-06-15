package com.example.upionemoretime.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController

class VoiceManager(
    private val context: Context
) {

    private val speechManager =
        SpeechRecognitionManager(context)

    private val ttsManager =
        TextToSpeechManager(context)

    private val wakeWordManager =
        WakeWordManager(speechManager)

    private val followUpSessionManager =
        FollowUpSessionManager()

    private var currentNavController: NavController? = null

    private var followUpEnabled = false
    private var isWaitingForCommand = false

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        Log.d("VOICE_MANAGER", "VoiceManager Created: ${hashCode()}")
        
        ttsManager.setOnSpeechDoneListener {
            Log.d("VOICE_DEBUG", "TTS Finished. followUpEnabled=$followUpEnabled, isWaitingForCommand=$isWaitingForCommand")

            if (isWaitingForCommand) {
                isWaitingForCommand = false
                // Small delay to ensure mic is released by any system process and TTS
                mainHandler.postDelayed({
                    startListeningForCommand()
                }, 300)
                return@setOnSpeechDoneListener
            }

            if (!followUpEnabled) {
                Log.d("VOICE_DEBUG", "Restarting Wake Word")
                startWakeWordDetection()
                return@setOnSpeechDoneListener
            }

            followUpEnabled = false
            Log.d("VOICE_DEBUG", "Starting Follow Up Listening")
            
            mainHandler.postDelayed({
                startListeningForCommand()
            }, 300)

            followUpSessionManager.startTimeout(timeoutMillis = 10_000) {
                Log.d("VOICE_SESSION", "Follow-up timeout expired")
                startWakeWordDetection()
            }
        }
    }

    private fun startListeningForCommand() {
        val nav = currentNavController ?: return
        
        speechManager.startListening(
            onResult = { result ->
                Log.d("VOICE_DEBUG", "Result = $result")
                followUpSessionManager.stopTimeout()

                val command = VoiceCommandParser.parse(result)
                if (command != VoiceCommand.Unknown) {
                    followUpEnabled = true
                    VoiceNavigationHandler.handleCommand(
                        command = command,
                        navController = nav,
                        ttsManager = ttsManager
                    )
                } else {
                    ttsManager.speak("I didn't catch that. Try again or say 'Hey Assistant' later.")
                    followUpEnabled = false
                }
            },
            onError = { error ->
                Log.d("VOICE_DEBUG", "Error = $error")
                if (followUpEnabled) {
                    // If we were in follow-up, just go back to wake word on error
                    followUpEnabled = false
                    startWakeWordDetection()
                } else {
                    // If it was the first command, maybe notify user
                    ttsManager.speak("Sorry, I had trouble hearing you.")
                }
            }
        )
    }

    fun updateNavController(navController: NavController) {
        this.currentNavController = navController
    }

    fun startWakeWordDetection() {
        Log.d("VOICE_DEBUG", "Starting Wake Word Detection")
        followUpEnabled = false
        isWaitingForCommand = false
        wakeWordManager.startListening {
            Log.d("VOICE_DEBUG", "Wake Word Detected!")
            mainHandler.post {
                wakeWordManager.stopListening()
                isWaitingForCommand = true
                ttsManager.speak("How can I help you?")
            }
        }
    }

    fun listenAndHandle(navController: NavController) {
        this.currentNavController = navController
        wakeWordManager.stopListening()
        isWaitingForCommand = true
        ttsManager.speak("Listening")
    }

    fun speak(text: String) {
        ttsManager.speak(text)
    }

    fun destroy() {
        Log.d("VOICE_MANAGER", "VoiceManager Destroyed")
        followUpSessionManager.stopTimeout()
        wakeWordManager.stopListening()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
