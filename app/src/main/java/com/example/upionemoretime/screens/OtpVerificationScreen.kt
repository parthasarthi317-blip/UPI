package com.example.upionemoretime.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.VoiceManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    navController: NavController,
    voiceManager: VoiceManager,
    mobileNumber: String,
    isLogin: Boolean
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var otpValue by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                otpValue = credential.smsCode ?: ""
                if (otpValue.isNotEmpty()) {
                    signInWithPhoneAuthCredential(credential, auth, navController, voiceManager)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isVerifying = false
                voiceManager.speak("Verification failed. ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                resendToken = token
                isVerifying = false
                voiceManager.speak("OTP sent successfully.")
            }
        }
    }

    fun startVerification(forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null) {
        isVerifying = true
        voiceManager.speak(if (forceResendingToken == null) "Sending OTP to $mobileNumber" else "Resending OTP")
        
        val formattedNumber = if (mobileNumber.startsWith("+")) mobileNumber else "+91$mobileNumber"
        
        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            
        if (forceResendingToken != null) {
            builder.setForceResendingToken(forceResendingToken)
        }
        
        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    LaunchedEffect(Unit) {
        startVerification()
    }

    Scaffold(
        containerColor = Obsidian
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Verify OTP",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Code sent to $mobileNumber",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = otpValue,
                onValueChange = { if (it.length <= 6) otpValue = it },
                label = { Text("6-Digit OTP", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { 
                if (resendToken != null) {
                    startVerification(resendToken)
                }
            }) {
                Text("Didn't receive code? Resend", color = PrimaryIndigo)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (otpValue.length == 6 && verificationId.isNotEmpty()) {
                        isVerifying = true
                        val credential = PhoneAuthProvider.getCredential(verificationId, otpValue)
                        signInWithPhoneAuthCredential(credential, auth, navController, voiceManager)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                enabled = otpValue.length == 6 && !isVerifying && verificationId.isNotEmpty()
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun signInWithPhoneAuthCredential(
    credential: PhoneAuthCredential,
    auth: FirebaseAuth,
    navController: NavController,
    voiceManager: VoiceManager
) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                voiceManager.speak("Verification successful.")
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            } else {
                voiceManager.speak("Verification failed.")
            }
        }
}
