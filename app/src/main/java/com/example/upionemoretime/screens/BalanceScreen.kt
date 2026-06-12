package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.voice.BalanceStore
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.TextToSpeechManager
import com.example.upionemoretime.voice.VoiceLauncher

@Composable
fun BalanceScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val ttsManager = remember {
        TextToSpeechManager(context)
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Account Balance"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "₹${BalanceStore.balance.value}"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Button(
                onClick = {

                    ttsManager.speak(
                        "Your balance is rupees ${BalanceStore.balance.value}"
                    )
                }
            ) {
                Text("Speak Balance")
            }
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
        DisposableEffect(Unit) {
            onDispose {
                ttsManager.shutdown()
            }
        }
    }

}