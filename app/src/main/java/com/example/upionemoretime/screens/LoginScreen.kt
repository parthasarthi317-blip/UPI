package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.VoiceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, voiceManager: VoiceManager) {
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Register UI listeners with VoiceManager
    LaunchedEffect(Unit) {
        voiceManager.setLoginListeners(
            onMobileUpdate = { mobileNumber = it },
            onPasswordUpdate = { password = it },
            onActionTrigger = {
                if (mobileNumber.length == 10) {
                    navController.navigate(Routes.otpRoute(mobileNumber, true))
                }
            }
        )
    }

    val isHindi = voiceManager.isHindi()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
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
                text = if (isHindi) "वापसी पर स्वागत है" else "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (isHindi) "अपने खाते में लॉगिन करें" else "Login to your account",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it },
                label = { Text(if (isHindi) "मोबाइल नंबर" else "Mobile Number", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (isHindi) "पासवर्ड" else "Password", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.navigate(Routes.SIGNUP) }) {
                Text(
                    text = if (isHindi) "खाता नहीं है? साइन अप करें" else "Don't have an account? Sign Up",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (mobileNumber.length == 10) {
                        navController.navigate(Routes.otpRoute(mobileNumber, true))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = mobileNumber.length == 10
            ) {
                Text(if (isHindi) "आगे बढ़ें" else "Next", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
