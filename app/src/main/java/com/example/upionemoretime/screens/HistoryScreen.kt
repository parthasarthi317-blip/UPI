package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.voice.TransactionHistoryStore
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.TextToSpeechManager
import androidx.compose.runtime.remember
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.VoiceCommand
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.VoiceNavigationHandler

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

                    val latestRecharge =
                        TransactionHistoryStore.rechargeHistory
                            .lastOrNull()

                    val latestPayment =
                        TransactionHistoryStore.paymentHistory
                            .lastOrNull()

                    when {

                        latestRecharge != null -> {
                            ttsManager.speak(
                                "Latest recharge is $latestRecharge"
                            )
                        }

                        latestPayment != null -> {
                            ttsManager.speak(
                                "Latest payment is $latestPayment"
                            )
                        }

                        else -> {
                            ttsManager.speak(
                                "No transaction history found"
                            )
                        }
                    }
                }
            ) {
                Text("Read History")
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

                                val latestRecharge =
                                    TransactionHistoryStore.rechargeHistory
                                        .lastOrNull()

                                val latestPayment =
                                    TransactionHistoryStore.paymentHistory
                                        .lastOrNull()

                                when {

                                    latestRecharge != null -> {
                                        ttsManager.speak(
                                            "Latest recharge is $latestRecharge"
                                        )
                                    }

                                    latestPayment != null -> {
                                        ttsManager.speak(
                                            "Latest payment is $latestPayment"
                                        )
                                    }

                                    else -> {
                                        ttsManager.speak(
                                            "No transaction history found"
                                        )
                                    }
                                }
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