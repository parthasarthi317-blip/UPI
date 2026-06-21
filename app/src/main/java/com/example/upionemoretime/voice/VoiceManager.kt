package com.example.upionemoretime.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.upionemoretime.voice.biometrics.VoiceBiometricManager

class VoiceManager(
    private val context: Context
) {

    private val speechManager = SpeechRecognitionManager(context)
    private val ttsManager = TextToSpeechManager(context)
    private val wakeWordManager = WakeWordManager(speechManager)
    private val followUpSessionManager = FollowUpSessionManager()
    private val biometricManager = VoiceBiometricManager(context)

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
    private val MAX_RETRIES = 2 

    private var pendingCommand: VoiceCommand? = null
    private var verificationChallenge: String = ""

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
                    VoiceState.AUTHENTICATING -> {
                        startBiometricVerification()
                    }
                    VoiceState.AUTHENTICATING_VOICE -> {
                        mainHandler.postDelayed({ startVoiceVerificationCapture() }, 1000)
                    }
                    VoiceState.ENROLLING -> {
                        startEnrollmentCapture()
                    }
                    VoiceState.ENROLLING_VOICE -> {
                        mainHandler.postDelayed({ startVoiceEnrollmentCapture() }, 1000)
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
            VoiceState.PROCESSING, VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE,
            VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE -> {
                followUpSessionManager.stopTimeout()
            }
            VoiceState.IDLE -> {
                wakeWordManager.stopListening()
                speechManager.stopListening()
                biometricManager.stopCapture()
                followUpSessionManager.stopTimeout()
            }
            VoiceState.UNAUTHORIZED -> {
                wakeWordManager.stopListening()
                ttsManager.speak("Verification failed. Access denied.")
                mainHandler.postDelayed({ transitionTo(VoiceState.WAKING) }, 3000)
            }
        }
    }

    private fun startBiometricVerification() {
        Log.d("BIOMETRIC", "Phase 1: Text Verification")
        speechManager.startListening(
            onResult = { result ->
                Log.d("BIOMETRIC", "Heard during verification: $result")
                val normalizedResult = result.lowercase().replace(Regex("[^a-z0-9]"), "")
                val normalizedChallenge = verificationChallenge.lowercase().replace(Regex("[^a-z0-9]"), "")
                val matched = normalizedResult.contains(normalizedChallenge) || normalizedChallenge.contains(normalizedResult)
                
                if (matched) {
                    mainHandler.post {
                        transitionTo(VoiceState.AUTHENTICATING_VOICE)
                        ttsManager.speak("Phrase matched. Now, say it one more time for your voice print.")
                    }
                } else {
                    mainHandler.post {
                        ttsManager.speak("I didn't hear the correct phrase. Please say: $verificationChallenge")
                    }
                }
            },
            onError = { error ->
                Log.e("BIOMETRIC", "Speech Error: $error")
                mainHandler.post { transitionTo(VoiceState.UNAUTHORIZED) }
            }
        )
    }

    private fun startVoiceVerificationCapture() {
        Log.d("BIOMETRIC", "Phase 2: Voice Verification Capture")
        biometricManager.verifySpeaker { embedding ->
            Log.d("BIOMETRIC", "Voice Verification Capture Finished. Embedding: ${embedding?.size ?: "NULL"}")
            finalizeVerificationInternal(true, embedding)
        }
        
        // Auto-stop after 4 seconds (enough time for one phrase)
        mainHandler.postDelayed({
            biometricManager.stopCapture()
        }, 4000)
    }

    private fun finalizeVerificationInternal(textMatch: Boolean, voiceEmbedding: FloatArray?) {
        mainHandler.post {
            val master = biometricManager.getMasterEmbedding()
            if (textMatch && voiceEmbedding != null && master != null) {
                val similarity = biometricManager.getSimilarity(master, voiceEmbedding)
                Log.d("BIOMETRIC", "Final Similarity: $similarity")
                if (similarity > 0.75f) {
                    val command = pendingCommand
                    pendingCommand = null
                    if (command != null) {
                        transitionTo(VoiceState.RESPONDING)
                        VoiceNavigationHandler.handleCommand(command, currentNavController!!, ttsManager)
                    } else {
                        transitionTo(VoiceState.WAKING)
                    }
                } else {
                    transitionTo(VoiceState.UNAUTHORIZED)
                }
            } else {
                Log.e("BIOMETRIC", "Verification Failed. Text: $textMatch, Voice: ${voiceEmbedding != null}")
                transitionTo(VoiceState.UNAUTHORIZED)
            }
        }
    }

    private fun startEnrollmentCapture() {
        Log.d("BIOMETRIC", "Phase 1: Enrollment Text Verification")
        speechManager.startListening(
            onResult = { result ->
                Log.d("BIOMETRIC", "Heard during enrollment: $result")
                val normalizedResult = result.lowercase().replace(Regex("[^a-z0-9]"), "")
                val normalizedChallenge = verificationChallenge.lowercase().replace(Regex("[^a-z0-9]"), "")
                val matched = normalizedResult.contains(normalizedChallenge) || normalizedChallenge.contains(normalizedResult)
                
                if (matched) {
                    mainHandler.post {
                        transitionTo(VoiceState.ENROLLING_VOICE)
                        ttsManager.speak("Good. Now say it again for the voice sample.")
                    }
                } else {
                    mainHandler.post {
                        ttsManager.speak("I didn't hear that. Please repeat: $verificationChallenge")
                    }
                }
            },
            onError = { error ->
                Log.e("BIOMETRIC", "Enrollment Speech Error: $error")
                mainHandler.post { 
                    // Retry text if failed
                    ttsManager.speak("Let's try that again. Say: $verificationChallenge")
                }
            }
        )
    }

    private fun startVoiceEnrollmentCapture() {
        Log.d("BIOMETRIC", "Phase 2: Enrollment Voice Capture")
        biometricManager.captureEnrollmentSample { embedding ->
            Log.d("BIOMETRIC", "Enrollment Voice Capture Finished. Embedding: ${embedding?.size ?: "NULL"}")
            finalizeEnrollmentStepInternal(true, embedding)
        }
        
        mainHandler.postDelayed({
            biometricManager.stopCapture()
        }, 4000)
    }

    private fun finalizeEnrollmentStepInternal(textMatch: Boolean, embedding: FloatArray?) {
        mainHandler.post {
            if (textMatch && embedding != null) {
                val count = biometricManager.addEnrollmentSample(embedding)
                if (count < 3) {
                    val nextChallenge = generateRandomChallenge()
                    verificationChallenge = nextChallenge
                    transitionTo(VoiceState.ENROLLING)
                    ttsManager.speak("Got it. $count of 3. Now say: $nextChallenge")
                } else {
                    ttsManager.speak("Enrollment complete. Your voice is now your password.")
                    transitionTo(VoiceState.WAKING)
                }
            } else {
                ttsManager.speak("I didn't hear that correctly. Let's try again. Say: $verificationChallenge")
                // Transitioning to ENROLLING again will trigger the next attempt via TTS listener
                transitionTo(VoiceState.ENROLLING)
            }
        }
    }

    private fun generateRandomChallenge(): String {
        val phrases = listOf(
            "My voice is my secure password",
            "In the digital world my voice is my key",
            "Secure my payments with my unique voice",

        )
        return phrases.random()
    }

    private fun isSensitiveCommand(command: VoiceCommand): Boolean {
        return when (command) {
            is VoiceCommand.SendMoney,
            is VoiceCommand.ConfirmPayment,
            is VoiceCommand.CheckBalance,
            is VoiceCommand.ClearHistory -> true
            else -> false
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
                    if (isSensitiveCommand(command)) {
                        if (!biometricManager.isUserEnrolled()) {
                            transitionTo(VoiceState.ENROLLING)
                            verificationChallenge = generateRandomChallenge()
                            biometricManager.startEnrollment()
                            ttsManager.speak("I need to learn your voice first. Please repeat this phrase: $verificationChallenge")
                        } else {
                            pendingCommand = command
                            verificationChallenge = generateRandomChallenge()
                            transitionTo(VoiceState.AUTHENTICATING)
                            ttsManager.speak("To verify it's you, please say: $verificationChallenge")
                        }
                    } else {
                        transitionTo(VoiceState.RESPONDING)
                        VoiceNavigationHandler.handleCommand(
                            command = command,
                            navController = nav,
                            ttsManager = ttsManager
                        )
                    }
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

    fun resetVoiceData() {
        biometricManager.clearEnrollment()
        ttsManager.speak("Voice data has been reset.")
    }

    fun destroy() {
        Log.d("VOICE_MANAGER", "VoiceManager Destroyed")
        transitionTo(VoiceState.IDLE)
        ttsManager.shutdown()
        speechManager.destroy()
        biometricManager.destroy()
    }
}
