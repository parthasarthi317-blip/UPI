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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && 
                      mobileNumber.length == 10 && 
                      email.contains("@") && 
                      age.isNotBlank() && 
                      country.isNotBlank()

    Scaffold(
        containerColor = Obsidian
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
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your details to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            SignUpTextField(value = name, onValueChange = { name = it }, label = "Full Name")
            Spacer(modifier = Modifier.height(16.dp))
            
            SignUpTextField(
                value = mobileNumber, 
                onValueChange = { if (it.length <= 10) mobileNumber = it }, 
                label = "Mobile Number",
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SignUpTextField(value = email, onValueChange = { email = it }, label = "Email ID", keyboardType = KeyboardType.Email)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    SignUpTextField(value = age, onValueChange = { age = it }, label = "Age", keyboardType = KeyboardType.Number)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(2f)) {
                    SignUpTextField(value = country, onValueChange = { country = it }, label = "Country")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                Text("Already have an account? Login", color = PrimaryIndigo)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    if (isFormValid) {
                        navController.navigate(Routes.otpRoute(mobileNumber, false))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                enabled = isFormValid
            ) {
                Text("Next", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryIndigo,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}
