package com.example.upionemoretime.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceManager(
    private val context: Context
) {

    private val speechManager = SpeechRecognitionManager(context)
    private val ttsManager = TextToSpeechManager(context)
    private val wakeWordManager = WakeWordManager(speechManager)
    private val followUpSessionManager = FollowUpSessionManager()

    private var currentNavController: NavController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private var currentState: VoiceState
        get() = _state.value
        set(value) {
            _state.value = value
        }

    private var retryCount = 0
    private val MAX_RETRIES = 2 // Reduced to 2 for faster exit on silence

    init {
        Log.d("VOICE_MANAGER", "VoiceManager Created: ${hashCode()}")

        ttsManager.setOnSpeechDoneListener {
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
                        if (currentState != VoiceState.LISTENING && currentState != VoiceState.WAKING && currentState != VoiceState.IDLE) {
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
                speechManager.stopListening() // Ensure command mic is OFF
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
                speechManager.stopListening() // Explicitly stop the mic to avoid late beeps
                followUpSessionManager.stopTimeout()
            }
            VoiceState.LISTENING -> {
                wakeWordManager.stopListening()
                mainHandler.postDelayed({
                    startListeningForCommand()
                }, 400)

                // 10 Second Inactivity Timeout
                followUpSessionManager.startTimeout(timeoutMillis = 10_000) {
                    Log.d("VOICE_SESSION", "10s Inactivity Timeout. Closing assistant.")
                    transitionTo(VoiceState.CLOSING)
                    ttsManager.speak("Goodbye")
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
                    handleUnknownCommand(errorType = "UNKNOWN")
                }
            },
            onError = { error ->
                Log.d("VOICE_DEBUG", "Speech Error: $error")
                handleUnknownCommand(errorType = error)
            }
        )
    }

    private fun handleUnknownCommand(errorType: String) {
        // If the error is a timeout (silence), we handle it more strictly
        val isSilence = errorType.contains("TIMEOUT", ignoreCase = true) || errorType.contains("NO_MATCH", ignoreCase = true)
        
        retryCount++
        
        if (retryCount >= MAX_RETRIES || (isSilence && retryCount >= 1)) {
            Log.d("VOICE_DEBUG", "Exit condition met. Returning to Wake Word.")
            transitionTo(VoiceState.CLOSING)
            ttsManager.speak("I'll stop listening now. Say 'Hey Assistant' if you need anything.")
        } else {
            transitionTo(VoiceState.PROMPTING)
            val message = if (isSilence) "I didn't hear anything. Could you repeat?" else "I didn't catch that. Could you repeat?"
            ttsManager.speak(message)
        }
    }

    fun updateNavController(navController: NavController) {
        this.currentNavController = navController
    }

    fun startWakeWordDetection() {
        if (currentState == VoiceState.IDLE || currentState == VoiceState.WAKING) {
            transitionTo(VoiceState.WAKING)
        }
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
