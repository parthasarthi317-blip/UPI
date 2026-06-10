package com.example.upionemoretime.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.voice.TransactionHistoryStore
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.TextToSpeechManager
import androidx.compose.runtime.remember
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.VoiceCommand
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.VoiceNavigationHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay

@Composable
fun HistoryScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val ttsManager = remember {
        TextToSpeechManager(context)
    }
    val speechManager = remember {
        SpeechRecognitionManager(context)
    }
    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
            ttsManager.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Text("Transaction History")

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text("Recent Recharges")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TransactionHistoryStore.rechargeHistory
                .takeLast(10)
                .reversed()
                .forEach {

                    Text(it)
                }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text("Recent Payments")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TransactionHistoryStore.paymentHistory
                .takeLast(10)
                .reversed()
                .forEach {

                    Text(it)
                }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
            Button(
                onClick = {
                    val rechargeCount =
                        TransactionHistoryStore.rechargeHistory.size

                    val paymentCount =
                        TransactionHistoryStore.paymentHistory.size

                    ttsManager.speak(
                        "You have $paymentCount payments and $rechargeCount recharges in your transaction history"
                    )
                }
            ) {
                Text("Read History")
            }
            Button(
                onClick = {

                    TransactionHistoryStore.rechargeHistory.clear()

                    TransactionHistoryStore.paymentHistory.clear()

                    ttsManager.speak(
                        "Transaction history cleared"
                    )
                }
            ) {
                Text("Clear History")
            }
            Button(
                onClick = {
                    navController.navigate(
                        Routes.STATS
                    )
                }
            ) {
                Text("View Statistics")
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

                speechManager.startListening(

                    onResult = { result ->

                        val command =
                            VoiceCommandParser.parse(result)

                        when (command) {

                            VoiceCommand.ReadHistory -> {

                                Log.d("HISTORY_TTS", "Read History button clicked")
                                val rechargeCount =
                                    TransactionHistoryStore.rechargeHistory.size

                                val paymentCount =
                                    TransactionHistoryStore.paymentHistory.size
                                Log.d("HISTORY_TTS", "Speaking summary")
                                ttsManager.speak(
                                    "You have $paymentCount payments and $rechargeCount recharges in your transaction history"
                                )
                            }
                            VoiceCommand.ClearHistory -> {

                                TransactionHistoryStore.rechargeHistory.clear()

                                TransactionHistoryStore.paymentHistory.clear()

                                ttsManager.speak(
                                    "Transaction history cleared"
                                )
                            }

                            else -> {

                                VoiceNavigationHandler.handleCommand(
                                    command = command,
                                    navController = navController,
                                    ttsManager = ttsManager
                                )
                            }
                        }
                    },

                    onError = {

                    }
                )
            }
        )
    }
}