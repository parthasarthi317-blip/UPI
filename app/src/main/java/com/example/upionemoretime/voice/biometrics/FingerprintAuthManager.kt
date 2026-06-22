package com.example.upionemoretime.voice.biometrics

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class FingerprintAuthManager(private val context: Context) {

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("FINGERPRINT", "Auth Error: $errString ($errorCode)")
                    onFailure(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("FINGERPRINT", "Auth Succeeded")
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("FINGERPRINT", "Auth Failed")
                    // This is called when a fingerprint is read but not recognized.
                    // The system usually handles retries automatically.
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Verification")
            .setSubtitle("Confirm your identity to proceed")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
