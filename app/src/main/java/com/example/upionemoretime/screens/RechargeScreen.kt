package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.GlobalVoiceFab
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.VoiceLauncher
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.upionemoretime.voice.VoiceCommand
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.TextToSpeechManager
import com.example.upionemoretime.voice.VoiceNavigationHandler
import com.example.upionemoretime.voice.TransactionHistoryStore
@Composable
fun RechargeScreen(
    navController: NavController,
    initialMobileNumber: String = "",
    initialAmount: Int = 0
) {

    val context = LocalContext.current
    val ttsManager = remember {
        TextToSpeechManager(context)
    }

    val speechManager = remember {
        SpeechRecognitionManager(context)
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    var showConfirmation by remember {
        mutableStateOf(false)
    }
    var rechargeSuccess by remember {
        mutableStateOf(false)
    }

    var mobileNumber by remember {
        mutableStateOf(initialMobileNumber)
    }

    var rechargeAmount by remember {
        mutableStateOf(
            if (initialAmount == 0) ""
            else initialAmount.toString()
        )
    }
    if (rechargeSuccess) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "✅ Recharge Successful"
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text =
                    "₹$rechargeAmount recharge completed for $mobileNumber"
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
                Text("Back To Home")
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
                text = "Mobile Recharge"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = {
                    mobileNumber = it
                },
                label = {
                    Text("Mobile Number")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = rechargeAmount,

                onValueChange = {
                    rechargeAmount = it
                },
                label = {
                    Text("Recharge Amount")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text("Popular Plans")

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = {
                        rechargeAmount = "199"
                    }
                ) {
                    Text("₹199")
                }

                Button(
                    onClick = {
                        rechargeAmount = "299"
                    }
                ) {
                    Text("₹299")
                }

                Button(
                    onClick = {
                        rechargeAmount = "719"
                    }
                ) {
                    Text("₹719")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {

                    if (
                        mobileNumber.length == 10 &&
                        rechargeAmount.toIntOrNull() != null &&
                        rechargeAmount.toInt() > 0
                    ) {

                        showConfirmation = true

                    } else {

                        ttsManager.speak(
                            "Please enter a valid mobile number and recharge amount"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Proceed")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
            if (showConfirmation) {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Recharge Number: $mobileNumber"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Amount: ₹$rechargeAmount"
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        TransactionHistoryStore.rechargeHistory.add(
                            "₹$rechargeAmount -> $mobileNumber"
                        )

                        rechargeSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Recharge")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {

                        showConfirmation = false

                        rechargeAmount = ""
                        mobileNumber = ""

                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
            if (
                TransactionHistoryStore.rechargeHistory.isNotEmpty()
            ){

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text("Recent Recharges")

                TransactionHistoryStore.rechargeHistory
                    .takeLast(5).reversed().forEach {

                    Text(it)
                }
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
                        android.util.Log.d(
                            "RECHARGE_VOICE",
                            result
                        )

                        val command =
                            VoiceCommandParser.parse(result)

                        when (command) {
                            is VoiceCommand.SelectRechargePlan -> {

                                rechargeAmount =
                                    command.amount.toString()

                                ttsManager.speak(
                                    "Plan selected for rupees ${command.amount}"
                                )
                            }

                            VoiceCommand.ConfirmRecharge -> {

                                if (showConfirmation) {

                                    ttsManager.speak(
                                        "Recharge of rupees $rechargeAmount for mobile number $mobileNumber completed successfully"
                                    )
                                    TransactionHistoryStore.rechargeHistory.add(
                                        "₹$rechargeAmount -> $mobileNumber"
                                    )

                                    rechargeSuccess = true
                                }
                            }

                            VoiceCommand.CancelRecharge -> {

                                if (showConfirmation) {

                                    ttsManager.speak(
                                        "Recharge cancelled"
                                    )

                                    showConfirmation = false

                                    rechargeAmount = ""
                                    mobileNumber = ""
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