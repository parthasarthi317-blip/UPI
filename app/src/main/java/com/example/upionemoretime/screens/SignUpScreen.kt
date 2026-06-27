package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, voiceManager: VoiceManager) {
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && 
                      mobileNumber.length == 10 && 
                      email.contains("@") &&
                      password.isNotBlank() &&
                      password == confirmPassword

    // Register UI listeners with VoiceManager
    LaunchedEffect(Unit) {
        voiceManager.setSignupListeners(
            onNameUpdate = { name = it },
            onMobileUpdate = { mobileNumber = it },
            onEmailUpdate = { email = it },
            onPasswordUpdate = { password = it },
            onConfirmPasswordUpdate = { confirmPassword = it },
            onActionTrigger = {
                if (isFormValid) {
                    voiceManager.setUserName(name)
                    navController.navigate(Routes.otpRoute(mobileNumber, false))
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = if (isHindi) "खाता बनाएं" else "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (isHindi) "शुरू करने के लिए अपना विवरण दर्ज करें" else "Enter your details to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            SignUpTextField(value = name, onValueChange = { name = it }, label = if (isHindi) "पूरा नाम" else "Full Name", voiceManager = voiceManager)
            Spacer(modifier = Modifier.height(16.dp))
            
            SignUpTextField(
                value = mobileNumber, 
                onValueChange = { if (it.length <= 10) mobileNumber = it }, 
                label = if (isHindi) "मोबाइल नंबर" else "Mobile Number",
                keyboardType = KeyboardType.Phone,
                voiceManager = voiceManager
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SignUpTextField(value = email, onValueChange = { email = it }, label = if (isHindi) "ईमेल आईडी" else "Email ID", keyboardType = KeyboardType.Email, voiceManager = voiceManager)
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                value = password, 
                onValueChange = { password = it }, 
                label = if (isHindi) "पासवर्ड" else "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                voiceManager = voiceManager
            )
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                value = confirmPassword, 
                onValueChange = { confirmPassword = it }, 
                label = if (isHindi) "पासवर्ड की पुष्टि करें" else "Confirm Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                voiceManager = voiceManager
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                Text(if (isHindi) "पहले से ही एक खाता है? लॉगिन करें" else "Already have an account? Login", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    if (isFormValid) {
                        voiceManager.setUserName(name)
                        navController.navigate(Routes.otpRoute(mobileNumber, false))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = isFormValid
            ) {
                Text(if (isHindi) "अगला" else "Next", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    voiceManager: VoiceManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
