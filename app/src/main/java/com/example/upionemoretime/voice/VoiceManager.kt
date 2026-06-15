package com.example.upionemoretime.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController

class VoiceManager(
    private val context: Context
) {

    private val speechManager = SpeechRecognitionManager(context)
    private val ttsManager = TextToSpeechManager(context)
    private val wakeWordManager = WakeWordManager(speechManager)
    private val followUpSessionManager = FollowUpSessionManager()

    private var currentNavController: NavController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentState = VoiceState.IDLE
    private var retryCount = 0
    private val MAX_RETRIES = 3

    init {
        Log.d("VOICE_MANAGER", "VoiceManager Created: ${hashCode()}")

        ttsManager.setOnSpeechDoneListener {
            // CRITICAL: TTS callbacks are on a background thread. 
            // We must move to the Main Thread for SpeechRecognizer.
            mainHandler.post {
                Log.d("VOICE_DEBUG", "TTS Finished. State: $currentState")

                when (currentState) {
                    VoiceState.PROMPTING -> {
                        transitionTo(VoiceState.LISTENING)
                    }
                    VoiceState.RESPONDING -> {
                        retryCount = 0 
                        transitionTo(VoiceState.LISTENING)
                    }
                    VoiceState.CLOSING -> {
                        transitionTo(VoiceState.WAKING)
                    }
                    else -> {
                        if (currentState != VoiceState.LISTENING && currentState != VoiceState.WAKING) {
                            transitionTo(VoiceState.WAKING)
                        }
                    }
                }
            }
        }
    }

    private fun transitionTo(newState: VoiceState) {
        Log.d("VOICE_DEBUG", "Transitioning: $currentState -> $newState")
        currentState = newState

        when (newState) {
            VoiceState.WAKING -> {
                followUpSessionManager.stopTimeout()
                retryCount = 0
                wakeWordManager.startListening {
                    Log.d("VOICE_DEBUG", "Wake Word Detected!")
                    mainHandler.post {
                        transitionTo(VoiceState.PROMPTING)
                        ttsManager.speak("How can I help you?")
                    }
                }
            }
            VoiceState.PROMPTING, VoiceState.RESPONDING, VoiceState.CLOSING -> {
                wakeWordManager.stopListening()
            }
            VoiceState.LISTENING -> {
                wakeWordManager.stopListening()
                mainHandler.postDelayed({
                    startListeningForCommand()
                }, 400)

                followUpSessionManager.startTimeout(timeoutMillis = 10_000) {
                    Log.d("VOICE_SESSION", "Session timeout.")
                    transitionTo(VoiceState.WAKING)
                }
            }
            VoiceState.PROCESSING -> {
                followUpSessionManager.stopTimeout()
            }
            VoiceState.IDLE -> {
                wakeWordManager.stopListening()
                speechManager.stopListening()
                followUpSessionManager.stopTimeout()
            }
        }
    }

    private fun startListeningForCommand() {
        val nav = currentNavController ?: run {
            transitionTo(VoiceState.WAKING)
            return
        }

        speechManager.startListening(
            onResult = { result ->
                Log.d("VOICE_DEBUG", "Speech Result: $result")
                currentState = VoiceState.PROCESSING

                val command = VoiceCommandParser.parse(result)
                if (command != VoiceCommand.Unknown) {
                    transitionTo(VoiceState.RESPONDING)
                    VoiceNavigationHandler.handleCommand(
                        command = command,
                        navController = nav,
                        ttsManager = ttsManager
                    )
                } else {
                    handleUnknownCommand()
                }
            },
            onError = { error ->
                Log.d("VOICE_DEBUG", "Speech Error: $error")
                handleUnknownCommand(isError = true)
            }
        )
    }

    private fun handleUnknownCommand(isError: Boolean = false) {
        retryCount++
        if (retryCount >= MAX_RETRIES) {
            Log.d("VOICE_DEBUG", "Max retries reached.")
            transitionTo(VoiceState.CLOSING)
            ttsManager.speak("I'm having trouble understanding. Let's try again later.")
        } else {
            transitionTo(VoiceState.PROMPTING)
            val message = if (isError) "Sorry, I didn't hear that." else "I didn't catch that. Could you repeat?"
            ttsManager.speak(message)
        }
    }

    fun updateNavController(navController: NavController) {
        this.currentNavController = navController
    }

    fun startWakeWordDetection() {
        transitionTo(VoiceState.WAKING)
    }

    fun listenAndHandle(navController: NavController) {
        this.currentNavController = navController
        transitionTo(VoiceState.PROMPTING)
        ttsManager.speak("Listening")
    }

    fun speak(text: String) {
        ttsManager.speak(text)
    }

    fun destroy() {
        Log.d("VOICE_MANAGER", "VoiceManager Destroyed")
        transitionTo(VoiceState.IDLE)
        ttsManager.shutdown()
        speechManager.destroy()
    }
}
