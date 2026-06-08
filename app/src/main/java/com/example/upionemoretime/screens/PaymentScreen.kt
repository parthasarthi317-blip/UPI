package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.upionemoretime.voice.TransactionHistoryStore
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.VoiceLauncher
import com.example.upionemoretime.voice.VoiceCommand
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.VoiceNavigationHandler
import com.example.upionemoretime.voice.TextToSpeechManager
@Composable
fun PaymentScreen(
    amount: Int,
    receiver: String,
    navController: NavController

) {
    val context = LocalContext.current
    val ttsManager = remember {
        TextToSpeechManager(context)
    }
    val speechManager = remember {
        SpeechRecognitionManager(context)
    }
    var paymentSuccess by remember {
        mutableStateOf(false)
    }
    var paymentMessage by remember {
        mutableStateOf("")
    }


    if (paymentSuccess) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "✅ Payment Successful",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "₹$amount sent to $receiver"
            )
            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    navController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(0)
                    }
                }
            ) {
                Text(
                    text = "Back To Home"
                )
            }

        }


        return
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
                text = "Confirm Payment",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)

                )
            ) {
                if (paymentMessage.isNotBlank()) {

                Text(
                    text = paymentMessage,
                    color = Color.Cyan
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Ready to send ₹$amount to $receiver",
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = receiver,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Amount",
                        color = Color.Gray
                    )

                    Text(
                        text = "₹$amount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {TransactionHistoryStore.paymentHistory.add(
                            "₹$amount -> $receiver"
                        )

                            paymentSuccess = true


                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Confirm Payment"
                        )
                    }

                }

            }


        }
        GlobalVoiceFab(
            onClick = {

                speechManager.startListening(

                    onResult = { result ->

                        val command =
                            VoiceCommandParser.parse(result)

                        when (command) {
                            VoiceCommand.ClearDetails -> {

                                paymentMessage = ""
                            }
                            VoiceCommand.RepeatDetails -> {

                                paymentMessage =
                                    "Sending ₹$amount to $receiver"
                                ttsManager.speak(
                                    "Sending $amount rupees to $receiver"
                                )
                            }
                            VoiceCommand.PaymentDetails -> {

                                paymentMessage =
                                    "Sending ₹$amount to $receiver"
                                ttsManager.speak(
                                    "Sending $amount rupees to $receiver"
                                )
                            }

                            VoiceCommand.ConfirmPayment -> {

                                (context as android.app.Activity).runOnUiThread {

                                    paymentMessage = "CONFIRM PAYMENT DETECTED"

                                    ttsManager.speak(
                                        "Payment successful"
                                    )

                                    TransactionHistoryStore.paymentHistory.add(
                                        "₹$amount -> $receiver"
                                    )

                                    paymentSuccess = true
                                }
                            }

                            VoiceCommand.CancelPayment -> {
                                ttsManager.speak(
                                    "Payment cancelled"
                                )

                                navController.navigate(
                                    Routes.HOME
                                ) {
                                    popUpTo(Routes.HOME)
                                }
                            }

                            else -> {

                                VoiceNavigationHandler.handleCommand(
                                    command = command,
                                    navController = navController
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