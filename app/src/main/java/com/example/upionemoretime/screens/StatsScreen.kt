package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.TextToSpeechManager
import com.example.upionemoretime.voice.TransactionHistoryStore
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.VoiceNavigationHandler

@Composable
fun StatsScreen(
    navController: NavController
) {

    val paymentCount =
        TransactionHistoryStore.paymentHistory.size

    val rechargeCount =
        TransactionHistoryStore.rechargeHistory.size
    val context = LocalContext.current

    val speechManager = remember {
        SpeechRecognitionManager(context)
    }

    val ttsManager = remember {
        TextToSpeechManager(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    )

    {

        Text("Transaction Statistics")

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text("Payments: $paymentCount")

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text("Recharges: $rechargeCount")

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
            speechManager.startListening(
                onResult = { result ->

                    val command =
                        VoiceCommandParser.parse(result)

                    VoiceNavigationHandler.handleCommand(
                        command = command,
                        navController = navController,
                        ttsManager = ttsManager
                    )
                },
                onError = {}
            )
        }
    )
}