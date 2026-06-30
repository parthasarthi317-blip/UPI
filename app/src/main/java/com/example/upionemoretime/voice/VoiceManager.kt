package com.example.upionemoretime.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.voice.biometrics.VoiceBiometricManager
import com.example.upionemoretime.voice.biometrics.FingerprintAuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceManager(
    val context: Context
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _authenticatedCommands = MutableSharedFlow<VoiceCommand>(extraBufferCapacity = 1)
    val authenticatedCommands = _authenticatedCommands.asSharedFlow()

    private val speechManager = SpeechRecognitionManager(context)
    private val ttsManager = TextToSpeechManager(context)
    private val wakeWordManager = WakeWordManager(speechManager)
    private val followUpSessionManager = FollowUpSessionManager()
    private val biometricManager = VoiceBiometricManager(context)
    private val fingerprintManager = FingerprintAuthManager(context)

    private var currentNavController: NavController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _enrollmentProgress = MutableStateFlow(0)
    val enrollmentProgress: StateFlow<Int> = _enrollmentProgress.asStateFlow()

    private var currentState: VoiceState
        get() = _state.value
        set(value) {
            _state.value = value
        }

    private var retryCount = 0
    private val MAX_RETRIES = 2

    private var pendingCommand: VoiceCommand? = null
    private var verificationChallenge: String = ""
    private var nextStateAfterSpeech: VoiceState? = null

    private var _userName = "User"
    private var isHindi = false
    private var isDarkMode = true
    private var _mobileNumber = ""
    private val prefs = context.getSharedPreferences("voice_prefs", Context.MODE_PRIVATE)
    private val bankPrefs = context.getSharedPreferences("bank_prefs", Context.MODE_PRIVATE)

    // UI Listeners
    private var onLoginMobileUpdate: ((String) -> Unit)? = null
    private var onLoginPasswordUpdate: ((String) -> Unit)? = null
    private var onLoginAction: (() -> Unit)? = null

    private var onSignupNameUpdate: ((String) -> Unit)? = null
    private var onSignupMobileUpdate: ((String) -> Unit)? = null
    private var onSignupEmailUpdate: ((String) -> Unit)? = null
    private var onSignupPasswordUpdate: ((String) -> Unit)? = null
    private var onSignupConfirmPasswordUpdate: ((String) -> Unit)? = null
    private var onSignupAction: (() -> Unit)? = null

    private var onOtpUpdate: ((String) -> Unit)? = null
    private var onOtpAction: (() -> Unit)? = null

    private var onBankSelected: ((String) -> Unit)? = null
    private var onBankLinkConfirmed: (() -> Unit)? = null
    private var onAddAnotherAccount: (() -> Unit)? = null
    private var onUnlinkAccount: (() -> Unit)? = null

    init {
        Log.d("VOICE_MANAGER", "VoiceManager Created: ${hashCode()}")
        _userName = prefs.getString("user_name", "User") ?: "User"
        _mobileNumber = prefs.getString("user_mobile", "") ?: ""
        val lang = prefs.getString("language", "en") ?: "en"
        isHindi = lang == "hi"
        isDarkMode = prefs.getBoolean("dark_mode", true)
        speechManager.setLanguage(lang)

        ttsManager.setOnSpeechDoneListener {
            mainHandler.post {
                handleSpeechDone()
            }
        }
    }

    private fun handleSpeechDone() {
        Log.d("VOICE_DEBUG", "TTS Finished. State: $currentState, Next: $nextStateAfterSpeech")
        
        val next = nextStateAfterSpeech
        if (next != null) {
            nextStateAfterSpeech = null
            transitionTo(next)
            return
        }

        when (currentState) {
            VoiceState.PROMPTING, VoiceState.RESPONDING, VoiceState.PROMPTING_SILENT -> {
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
            // Dialog Flow States
            VoiceState.LOGIN_MOBILE, VoiceState.LOGIN_PASSWORD, VoiceState.LOGIN_CONFIRM,
            VoiceState.SIGNUP_NAME, VoiceState.SIGNUP_NAME_CONFIRM, VoiceState.SIGNUP_MOBILE,
            VoiceState.SIGNUP_EMAIL, VoiceState.SIGNUP_PASSWORD, VoiceState.SIGNUP_CONFIRM_PASSWORD,
            VoiceState.SIGNUP_CONFIRM_FINAL, VoiceState.OTP_INPUT, VoiceState.OTP_CONFIRM,
            VoiceState.ONBOARDING_CHECK, VoiceState.LINK_BANK_SELECTION, VoiceState.LINK_BANK_CONFIRMATION -> {
                transitionTo(currentState) // Stay in flow
            }
            else -> {
                if (currentState != VoiceState.LISTENING && currentState != VoiceState.WAKING && currentState != VoiceState.IDLE) {
                    transitionTo(VoiceState.WAKING)
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
                speechManager.stopListening()
                retryCount = 0
                wakeWordManager.startListening {
                    Log.d("VOICE_DEBUG", "Wake Word Detected!")
                    mainHandler.post { handleWakeWord() }
                }
            }
            VoiceState.LISTENING, VoiceState.LOGIN_MOBILE, VoiceState.LOGIN_PASSWORD, VoiceState.LOGIN_CONFIRM,
            VoiceState.SIGNUP_NAME, VoiceState.SIGNUP_NAME_CONFIRM, VoiceState.SIGNUP_MOBILE,
            VoiceState.SIGNUP_EMAIL, VoiceState.SIGNUP_PASSWORD, VoiceState.SIGNUP_CONFIRM_PASSWORD,
            VoiceState.SIGNUP_CONFIRM_FINAL, VoiceState.OTP_INPUT, VoiceState.OTP_CONFIRM,
            VoiceState.ONBOARDING_CHECK, VoiceState.LINK_BANK_SELECTION, VoiceState.LINK_BANK_CONFIRMATION -> {
                wakeWordManager.stopListening()
                mainHandler.postDelayed({
                    startListeningForCommand()
                }, 400)

                followUpSessionManager.startTimeout(timeoutMillis = 15_000) {
                    Log.d("VOICE_SESSION", "Inactivity Timeout.")
                    transitionTo(VoiceState.WAKING)
                }
            }
            VoiceState.PROCESSING, VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE,
            VoiceState.AUTHENTICATING_FINGERPRINT,
            VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE,
            VoiceState.PROMPTING, VoiceState.PROMPTING_SILENT, VoiceState.RESPONDING, VoiceState.CLOSING -> {
                followUpSessionManager.stopTimeout()
                wakeWordManager.stopListening()
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

    private fun handleWakeWord() {
        val route = currentNavController?.currentBackStackEntry?.destination?.route
        Log.d("VOICE_DEBUG", "Wake word on route: $route")

        when {
            route == Routes.SIGNUP -> {
                transitionTo(VoiceState.ONBOARDING_CHECK)
                ttsManager.speak("Hello! I can help you with registration. Are you already registered with InclusivePay?")
            }
            route == Routes.LOGIN -> {
                transitionTo(VoiceState.LOGIN_MOBILE)
                ttsManager.speak("Hello, I can help you login. Please tell me your registered mobile number.")
            }
            route?.startsWith("otp_verification") == true -> {
                transitionTo(VoiceState.OTP_INPUT)
                ttsManager.speak("Please tell me the 6 digit O T P you received.")
            }
            route == Routes.LINK_BANK -> {
                val linkedBank = bankPrefs.getString("linked_bank", null)
                if (linkedBank == null) {
                    transitionTo(VoiceState.LINK_BANK_SELECTION)
                    val mobile = getUserMobile()
                    val last4 = if (mobile.length >= 4) mobile.takeLast(4) else "XXXX"
                    ttsManager.speak(
                        if (isHindi) "मुझे आपका पंजीकृत मोबाइल नंबर मिला है जिसके अंत में $last4 है। अब कृपया वह बैंक चुनें जिसे आप लिंक करना चाहते हैं।"
                        else "I found your registered mobile number ending with $last4. Now please choose the bank you want to link."
                    )
                } else {
                    transitionTo(VoiceState.PROMPTING)
                    ttsManager.speak(if (isHindi) "मैं आपकी क्या मदद कर सकता हूँ?" else "How can I help you?")
                }
            }
            else -> {
                transitionTo(VoiceState.PROMPTING)
                ttsManager.speak("How can I help you?")
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
                followUpSessionManager.stopTimeout()
                val command = VoiceCommandParser.parse(result, currentState)
                handleCommand(command, nav)
            },
            onError = { error ->
                Log.d("VOICE_DEBUG", "Speech Error: $error")
                handleUnknownCommand(errorType = error)
            }
        )
    }

    private fun handleCommand(command: VoiceCommand, nav: NavController) {
        when (currentState) {
            VoiceState.ONBOARDING_CHECK -> when (command) {
                VoiceCommand.Yes -> {
                    mainHandler.post {
                        nav.navigate(Routes.LOGIN)
                        transitionTo(VoiceState.LOGIN_MOBILE)
                        ttsManager.speak("I can help you login. Please tell me your registered mobile number.")
                    }
                }
                VoiceCommand.No -> {
                    transitionTo(VoiceState.SIGNUP_NAME)
                    ttsManager.speak("Welcome to InclusivePay Signup. What is your full name?")
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            VoiceState.LOGIN_MOBILE -> if (command is VoiceCommand.DataInput) {
                onLoginMobileUpdate?.invoke(command.text)
                transitionTo(VoiceState.LOGIN_PASSWORD)
                ttsManager.speak("Please tell me your password.")
            }
            VoiceState.LOGIN_PASSWORD -> if (command is VoiceCommand.DataInput) {
                onLoginPasswordUpdate?.invoke(command.text)
                transitionTo(VoiceState.LOGIN_CONFIRM)
                ttsManager.speak("I have filled your login details. Would you like me to login?")
            }
            VoiceState.LOGIN_CONFIRM -> when (command) {
                VoiceCommand.Yes -> {
                    onLoginAction?.invoke()
                    transitionTo(VoiceState.WAKING)
                }
                VoiceCommand.No -> {
                    ttsManager.speak("Okay.")
                    transitionTo(VoiceState.WAKING)
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            VoiceState.SIGNUP_NAME -> if (command is VoiceCommand.DataInput) {
                onSignupNameUpdate?.invoke(command.text)
                transitionTo(VoiceState.SIGNUP_NAME_CONFIRM)
                ttsManager.speak("Your name is ${command.text}. Is this correct?")
            }
            VoiceState.SIGNUP_NAME_CONFIRM -> when (command) {
                VoiceCommand.Yes -> {
                    transitionTo(VoiceState.SIGNUP_MOBILE)
                    ttsManager.speak("Please tell your mobile number.")
                }
                VoiceCommand.No -> {
                    transitionTo(VoiceState.SIGNUP_NAME)
                    ttsManager.speak("Sorry, what is your full name?")
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            VoiceState.SIGNUP_MOBILE -> if (command is VoiceCommand.DataInput) {
                onSignupMobileUpdate?.invoke(command.text)
                transitionTo(VoiceState.SIGNUP_EMAIL)
                ttsManager.speak("What is your email address?")
            }
            VoiceState.SIGNUP_EMAIL -> if (command is VoiceCommand.DataInput) {
                onSignupEmailUpdate?.invoke(command.text)
                transitionTo(VoiceState.SIGNUP_PASSWORD)
                ttsManager.speak("Please create a password.")
            }
            VoiceState.SIGNUP_PASSWORD -> if (command is VoiceCommand.DataInput) {
                onSignupPasswordUpdate?.invoke(command.text)
                transitionTo(VoiceState.SIGNUP_CONFIRM_PASSWORD)
                ttsManager.speak("Please confirm password.")
            }
            VoiceState.SIGNUP_CONFIRM_PASSWORD -> if (command is VoiceCommand.DataInput) {
                onSignupConfirmPasswordUpdate?.invoke(command.text)
                transitionTo(VoiceState.SIGNUP_CONFIRM_FINAL)
                ttsManager.speak("I have completed your signup form. Would you like me to create your account?")
            }
            VoiceState.SIGNUP_CONFIRM_FINAL -> when (command) {
                VoiceCommand.Yes -> {
                    onSignupAction?.invoke()
                    transitionTo(VoiceState.WAKING)
                }
                VoiceCommand.No -> {
                    ttsManager.speak("Okay.")
                    transitionTo(VoiceState.WAKING)
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            VoiceState.OTP_INPUT -> if (command is VoiceCommand.DataInput) {
                onOtpUpdate?.invoke(command.text)
                transitionTo(VoiceState.OTP_CONFIRM)
                ttsManager.speak("I have entered the O T P. Should I verify it?")
            }
            VoiceState.OTP_CONFIRM -> when (command) {
                VoiceCommand.Yes -> {
                    onOtpAction?.invoke()
                    transitionTo(VoiceState.WAKING)
                }
                VoiceCommand.No -> {
                    ttsManager.speak("Okay, staying on this screen.")
                    transitionTo(VoiceState.WAKING)
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            VoiceState.LINK_BANK_SELECTION -> when (command) {
                is VoiceCommand.LinkBank -> {
                    onBankSelected?.invoke(command.bankName)
                    transitionTo(VoiceState.LINK_BANK_CONFIRMATION)
                    ttsManager.speak("You selected ${command.bankName}. Shall I link this account?")
                }
                is VoiceCommand.DataInput -> {
                    onBankSelected?.invoke(command.text)
                    transitionTo(VoiceState.LINK_BANK_CONFIRMATION)
                    ttsManager.speak("You selected ${command.text}. Shall I link this account?")
                }
                else -> ttsManager.speak("Please tell me your bank name.")
            }
            VoiceState.LINK_BANK_CONFIRMATION -> when (command) {
                VoiceCommand.Yes -> {
                    onBankLinkConfirmed?.invoke()
                    transitionTo(VoiceState.WAKING)
                }
                VoiceCommand.No -> {
                    transitionTo(VoiceState.LINK_BANK_SELECTION)
                    ttsManager.speak("Okay, which bank would you like to link?")
                }
                else -> ttsManager.speak("Please say yes or no.")
            }
            else -> handleGlobalCommand(command, nav)
        }
    }

    private fun handleGlobalCommand(command: VoiceCommand, nav: NavController) {
        if (command == VoiceCommand.Unknown) {
            handleUnknownCommand("UNKNOWN")
            return
        }

        if (command == VoiceCommand.OpenLinkBank && nav.currentBackStackEntry?.destination?.route == Routes.LINK_BANK) {
            onAddAnotherAccount?.invoke()
            transitionTo(VoiceState.LINK_BANK_SELECTION)
            val mobile = getUserMobile()
            val last4 = if (mobile.length >= 4) mobile.takeLast(4) else "XXXX"
            ttsManager.speak(
                if (isHindi) "जरूर, मुझे आपका पंजीकृत मोबाइल नंबर $last4 मिला है। कृपया उस बैंक का नाम बताएं जिसे आप जोड़ना चाहते हैं।" 
                else "Sure, I found your registered mobile number ending with $last4. Please tell me the name of the bank you want to add."
            )
            return
        }

        if (command == VoiceCommand.UnlinkBank && nav.currentBackStackEntry?.destination?.route == Routes.LINK_BANK) {
            onUnlinkAccount?.invoke()
            transitionTo(VoiceState.RESPONDING)
            ttsManager.speak(if (isHindi) "आपका बैंक खाता सफलतापूर्वक हटा दिया गया है।" else "Your bank account has been unlinked successfully.")
            return
        }

        if (isSensitiveCommand(command)) {
            handleSensitiveCommand(command)
        } else {
            transitionTo(VoiceState.RESPONDING)
            VoiceNavigationHandler.handleCommand(command, nav, ttsManager)
        }
    }

    private fun handleSensitiveCommand(command: VoiceCommand) {
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
    }

    // Biometric Verification methods
    private fun startBiometricVerification() {
        speechManager.startListening(
            onResult = { result ->
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
            onError = { transitionTo(VoiceState.UNAUTHORIZED) }
        )
    }

    private fun startVoiceVerificationCapture() {
        biometricManager.verifySpeaker { embedding -> finalizeVerificationInternal(true, embedding) }
        mainHandler.postDelayed({ biometricManager.stopCapture() }, 4000)
    }

    private fun finalizeVerificationInternal(textMatch: Boolean, voiceEmbedding: FloatArray?) {
        mainHandler.post {
            val master = biometricManager.getMasterEmbedding()
            if (textMatch && voiceEmbedding != null && master != null) {
                if (biometricManager.getSimilarity(master, voiceEmbedding) > 0.75f) {
                    startFingerprintAuthentication()
                } else {
                    transitionTo(VoiceState.UNAUTHORIZED)
                }
            } else {
                transitionTo(VoiceState.UNAUTHORIZED)
            }
        }
    }

    private fun startFingerprintAuthentication() {
        val activity = context as? FragmentActivity ?: return transitionTo(VoiceState.UNAUTHORIZED)
        if (!fingerprintManager.canAuthenticate()) {
            ttsManager.speak("Fingerprint authentication is not set up.")
            return transitionTo(VoiceState.UNAUTHORIZED)
        }
        transitionTo(VoiceState.AUTHENTICATING_FINGERPRINT)
        fingerprintManager.authenticate(activity, 
            onSuccess = {
                val command = pendingCommand
                pendingCommand = null
                if (command != null) {
                    scope.launch { _authenticatedCommands.emit(command) }
                    transitionTo(VoiceState.RESPONDING)
                    if (command == VoiceCommand.ResetVoice) { resetVoiceData(); transitionTo(VoiceState.WAKING) }
                    else { VoiceNavigationHandler.handleCommand(command, currentNavController!!, ttsManager) }
                } else { transitionTo(VoiceState.WAKING) }
            },
            onFailure = { transitionTo(VoiceState.UNAUTHORIZED) }
        )
    }

    private fun startEnrollmentCapture() {
        speechManager.startListening(
            onResult = { result ->
                if (result.lowercase().replace(Regex("[^a-z0-9]"), "").contains(verificationChallenge.lowercase().replace(Regex("[^a-z0-9]"), ""))) {
                    mainHandler.post { transitionTo(VoiceState.ENROLLING_VOICE); ttsManager.speak("Good. Now say it again for the voice sample.") }
                } else { ttsManager.speak("Please repeat: $verificationChallenge") }
            },
            onError = { ttsManager.speak("Let's try that again. Say: $verificationChallenge") }
        )
    }

    private fun startVoiceEnrollmentCapture() {
        biometricManager.captureEnrollmentSample { embedding -> finalizeEnrollmentStepInternal(true, embedding) }
        mainHandler.postDelayed({ biometricManager.stopCapture() }, 4000)
    }

    private fun finalizeEnrollmentStepInternal(textMatch: Boolean, embedding: FloatArray?) {
        mainHandler.post {
            if (textMatch && embedding != null) {
                val count = biometricManager.addEnrollmentSample(embedding)
                _enrollmentProgress.value = count
                if (count < 3) {
                    verificationChallenge = generateRandomChallenge()
                    transitionTo(VoiceState.ENROLLING)
                    ttsManager.speak("Got it. $count of 3. Now say: $verificationChallenge")
                } else {
                    ttsManager.speak("Enrollment complete. Your voice is now your password.")
                    // Redirect to home after a short delay
                    mainHandler.postDelayed({
                        currentNavController?.navigate(Routes.HOME) {
                            popUpTo(Routes.VOICE_ENROLLMENT) { inclusive = true }
                        }
                        transitionTo(VoiceState.WAKING)
                    }, 2000)
                }
            } else { transitionTo(VoiceState.ENROLLING) }
        }
    }

    private fun generateRandomChallenge() = listOf("My voice is my secure password", "In the digital world my voice is my key", "Secure my payments with my unique voice").random()

    private fun isSensitiveCommand(command: VoiceCommand) = command in listOf(VoiceCommand.CheckBalance, VoiceCommand.RevealBalance, VoiceCommand.ClearHistory, VoiceCommand.ResetVoice) || command is VoiceCommand.SendMoney || command is VoiceCommand.ConfirmPayment || command is VoiceCommand.RechargeMobile || command is VoiceCommand.ConfirmRecharge

    private fun handleUnknownCommand(errorType: String) {
        val isSilence = errorType.contains("TIMEOUT", ignoreCase = true) || errorType.contains("NO_MATCH", ignoreCase = true)
        retryCount++
        if (retryCount >= MAX_RETRIES || (isSilence && retryCount >= 1)) {
            transitionTo(VoiceState.CLOSING)
            ttsManager.speak("I'll stop listening now.")
        } else {
            transitionTo(VoiceState.PROMPTING)
            ttsManager.speak(if (isSilence) "I didn't hear anything. Could you repeat?" else "I didn't catch that. Could you repeat?")
        }
    }

    fun setLoginListeners(onMobileUpdate: (String) -> Unit, onPasswordUpdate: (String) -> Unit, onActionTrigger: () -> Unit) {
        this.onLoginMobileUpdate = onMobileUpdate; this.onLoginPasswordUpdate = onPasswordUpdate; this.onLoginAction = onActionTrigger
    }

    fun setSignupListeners(onNameUpdate: (String) -> Unit, onMobileUpdate: (String) -> Unit, onEmailUpdate: (String) -> Unit, onPasswordUpdate: (String) -> Unit, onConfirmPasswordUpdate: (String) -> Unit, onActionTrigger: () -> Unit) {
        this.onSignupNameUpdate = onNameUpdate; this.onSignupMobileUpdate = onMobileUpdate; this.onSignupEmailUpdate = onEmailUpdate
        this.onSignupPasswordUpdate = onPasswordUpdate; this.onSignupConfirmPasswordUpdate = onConfirmPasswordUpdate; this.onSignupAction = onActionTrigger
    }

    fun setOtpListeners(onOtpUpdate: (String) -> Unit, onActionTrigger: () -> Unit) {
        this.onOtpUpdate = onOtpUpdate
        this.onOtpAction = onActionTrigger
    }

    fun setLinkBankListeners(
        onBankSelected: (String) -> Unit,
        onConfirm: () -> Unit,
        onAddAnother: (() -> Unit)? = null,
        onUnlink: (() -> Unit)? = null
    ) {
        this.onBankSelected = onBankSelected
        this.onBankLinkConfirmed = onConfirm
        this.onAddAnotherAccount = onAddAnother
        this.onUnlinkAccount = onUnlink
    }

    fun updateNavController(navController: NavController) { this.currentNavController = navController }
    fun startWakeWordDetection() { if (currentState == VoiceState.IDLE || currentState == VoiceState.WAKING) transitionTo(VoiceState.WAKING) }
    fun listenAndHandle(navController: NavController) { this.currentNavController = navController; transitionTo(VoiceState.PROMPTING); ttsManager.speak("Listening") }

    fun triggerSensitiveCommand(command: VoiceCommand) {
        pendingCommand = command
        verificationChallenge = generateRandomChallenge()
        if (!biometricManager.isUserEnrolled()) {
            mainHandler.post {
                currentNavController?.navigate(Routes.VOICE_ENROLLMENT)
                ttsManager.speak("You must complete voice enrollment before performing this action.")
            }
        } else {
            transitionTo(VoiceState.AUTHENTICATING)
            ttsManager.speak("To verify it's you, please say: $verificationChallenge")
        }
    }

    fun speak(text: String, startListeningAfter: Boolean = false, nextState: VoiceState? = null) {
        if (startListeningAfter) {
            nextStateAfterSpeech = nextState ?: VoiceState.LISTENING
            // We transition to RESPONDING/PROMPTING while speaking so we don't trigger wake word or double listen
            transitionTo(VoiceState.PROMPTING_SILENT)
        } else {
            nextStateAfterSpeech = null
        }
        ttsManager.speak(text)
    }
    fun resetVoiceData() { biometricManager.clearEnrollment(); ttsManager.speak("Voice data has been reset.") }

    fun setUserName(name: String) {
        _userName = name
        prefs.edit().putString("user_name", name).apply()
    }

    fun setUserMobile(mobile: String) {
        _mobileNumber = mobile
        prefs.edit().putString("user_mobile", mobile).apply()
    }

    fun getUserMobile(): String = _mobileNumber

    fun getUserName(): String = _userName

    fun isHindi(): Boolean = isHindi
    
    fun isDarkMode(): Boolean = isDarkMode

    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun setLanguage(hindi: Boolean) {
        isHindi = hindi
        val langCode = if (hindi) "hi" else "en"
        prefs.edit().putString("language", langCode).apply()
        speechManager.setLanguage(langCode)
    }

    fun isUserEnrolled(): Boolean = biometricManager.isUserEnrolled()

    fun triggerEnrollmentFlow() {
        verificationChallenge = generateRandomChallenge()
        transitionTo(VoiceState.ENROLLING)
        biometricManager.startEnrollment()
        ttsManager.speak("I need to learn your voice print to secure your account. Please repeat this phrase clearly: $verificationChallenge")
    }

    fun requestManualBiometric(prompt: String, onAuthenticated: () -> Unit) {
        val activity = context as? FragmentActivity ?: return
        
        fingerprintManager.authenticate(activity,
            title = if (isHindi) "पहचान सत्यापित करें" else "Verify Identity",
            subtitle = if (isHindi) "बैलेंस देखने के लिए फिंगरप्रिंट का उपयोग करें" else "Use fingerprint to see balance",
            onSuccess = {
                onAuthenticated()
            },
            onFailure = {
            }
        )
    }

    fun destroy() { transitionTo(VoiceState.IDLE); ttsManager.shutdown(); speechManager.destroy(); biometricManager.destroy() }
}
