package com.example.upionemoretime.voice

import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.voice.TextToSpeechManager
object VoiceNavigationHandler {

    fun handleCommand(
        command: VoiceCommand,
        navController: NavController,
        ttsManager: TextToSpeechManager? = null
    ) {

        when (command) {

            VoiceCommand.CheckBalance -> {
                ttsManager?.speak(
                    "Opening balance screen"
                )
                navController.navigate(
                    Routes.BALANCE
                ){
                    launchSingleTop = true
                }
            }

            VoiceCommand.OpenRecharge -> {
                ttsManager?.speak(
                    "Opening recharge screen"
                )
                navController.navigate(
                    Routes.RECHARGE
                ){
                    launchSingleTop = true
                }
            }

            VoiceCommand.OpenSettings -> {
                ttsManager?.speak(
                    "Opening settings"
                )
                navController.navigate(
                    Routes.SETTINGS
                ){
                    launchSingleTop = true
                }
            }

            VoiceCommand.GoBack -> {
                ttsManager?.speak(
                    "Going back"
                )
                navController.popBackStack()
            }

            VoiceCommand.GoHome -> {
                ttsManager?.speak(
                    "Going home"
                )
                navController.navigate(
                    Routes.HOME
                ) {
                    popUpTo(Routes.HOME)
                    launchSingleTop = true
                }
            }

            is VoiceCommand.SendMoney -> {
                navController.navigate(
                    Routes.paymentRoute(
                        amount = command.amount,
                        receiver = command.receiver
                    )
                )
            }
            is VoiceCommand.RechargeMobile -> {

                navController.navigate(
                    Routes.rechargeRoute(
                        mobileNumber = command.mobileNumber,
                        amount = command.amount
                    )
                )
            }

            VoiceCommand.OpenPayment -> {
                navController.navigate(
                    Routes.paymentRoute(
                        amount = 0,
                        receiver = "Receiver"
                    )
                )
            }
            VoiceCommand.OpenHistory -> {

                ttsManager?.speak(
                    "Opening transaction history"
                )

                navController.navigate(
                    Routes.HISTORY
                ) {
                    launchSingleTop = true
                }
            }
            VoiceCommand.ConfirmPayment -> {
                // Will be handled inside PaymentScreen
            }
            VoiceCommand.CancelPayment -> {
                // Will be handled inside PaymentScreen
            }

            VoiceCommand.Unknown -> {}
            else -> {}
        }
    }
}