package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.VoiceLauncher
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text("Voice Language")

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text("Payment Preferences")

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text("Security")

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Back")
            }
        }
        GlobalVoiceFab(
            onClick = {
                VoiceLauncher.startListening(
                    context = context,
                    navController = navController
                )
            }
        )
    }

}