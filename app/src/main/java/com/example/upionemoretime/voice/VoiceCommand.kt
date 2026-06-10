package com.example.upionemoretime.voice

sealed class VoiceCommand {

    data object CheckBalance : VoiceCommand()


    data object OpenRecharge : VoiceCommand()
    data object OpenSettings : VoiceCommand()
    data object OpenPayment : VoiceCommand()
    data object OpenHistory : VoiceCommand()
    data object ReadHistory : VoiceCommand()
    data object ClearHistory : VoiceCommand()

    data class SendMoney(
        val amount: Int,
        val receiver: String
    ) : VoiceCommand()

    data object GoBack : VoiceCommand()
    data object GoHome : VoiceCommand()
    data object ConfirmPayment : VoiceCommand()
    data object CancelPayment : VoiceCommand()
    data object ConfirmRecharge : VoiceCommand()
    data object CancelRecharge : VoiceCommand()
    data object PaymentDetails : VoiceCommand()
    data object RepeatDetails : VoiceCommand()
    data object ClearDetails : VoiceCommand()
    data class RechargeMobile(
        val mobileNumber: String,
        val amount: Int
    ) : VoiceCommand()
    data class SelectRechargePlan(
        val amount: Int
    ) : VoiceCommand()
    data object Unknown : VoiceCommand()
    data object OpenStatistics : VoiceCommand()
}