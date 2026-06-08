package com.example.upionemoretime.voice

private val wordToNumber = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3,
    "four" to 4,
    "five" to 5,
    "six" to 6,
    "seven" to 7,
    "eight" to 8,
    "nine" to 9,
    "ten" to 10,
    "hundred" to 100,
    "thousand" to 1000
)
object VoiceCommandParser {

    fun parse(
        text: String
    ): VoiceCommand {

        val command = text.lowercase()
        if (
            "go back" in command ||
            "back" in command||"previous screen" in command ||
                "go to previous screen" in command ||
                "return" in command
        ) {
            return VoiceCommand.GoBack
        }
        if (
            "go home" in command ||
            "home" in command||"open home" in command ||
                "main screen" in command ||
                "dashboard" in command
        ) {
            return VoiceCommand.GoHome
        }
        if (
            "confirm recharge" in command
        ) {
            return VoiceCommand.ConfirmRecharge
        }

        if (
            "cancel recharge" in command
        ) {
            return VoiceCommand.CancelRecharge
        }
        if (
            "confirm payment" in command ||
            "confirm" in command ||
            "pay now" in command
        ) {
            return VoiceCommand.ConfirmPayment
        }
        if (
            "cancel payment" in command ||
            "cancel" in command ||
            "abort payment" in command||"abort" in command
        ) {
            return VoiceCommand.CancelPayment
        }

        if (
            "payment details" in command ||
            "show payment details" in command ||
            "details" in command
        ) {
            return VoiceCommand.PaymentDetails
        }
        if (
            command == "repeat" ||
            "repeat details" in command ||
            "repeat payment details" in command
        ) {
            return VoiceCommand.RepeatDetails
        }
        if (
            command == "clear" ||
            "clear details" in command ||
            "hide details" in command
        ) {
            return VoiceCommand.ClearDetails
        }
        if ("balance" in command||
            "check balance" in command ||
            "show balance" in command ||
            "my balance" in command ||
            "account balance" in command) {
            return VoiceCommand.CheckBalance
        }
        if (
            "199" in command &&
            "plan" in command
        ) {
            return VoiceCommand.SelectRechargePlan(199)
        }

        if (
            "299" in command &&
            "plan" in command
        ) {
            return VoiceCommand.SelectRechargePlan(299)
        }

        if (
            "719" in command &&
            "plan" in command
        ) {
            return VoiceCommand.SelectRechargePlan(719)
        }
        if (
            "recharge" in command &&
            "with" in command
        ) {

            val numbers =
                Regex("\\d+")
                    .findAll(command)
                    .map { it.value }
                    .toList()

            if (numbers.size >= 2) {

                val mobileNumber = numbers[0]

                val amount =
                    numbers[1].toIntOrNull()

                if (amount != null) {

                    return VoiceCommand.RechargeMobile(
                        mobileNumber = mobileNumber,
                        amount = amount
                    )
                }
            }
        }
        if (
            "confirm recharge" in command
        ) {
            return VoiceCommand.ConfirmRecharge
        }

        if (
            "cancel recharge" in command
        ) {
            return VoiceCommand.CancelRecharge
        }

        if ("recharge" in command||"mobile recharge" in command ||
                "phone recharge" in command ||
                "recharge my phone" in command) {
            return VoiceCommand.OpenRecharge
        }
        if (
            "clear history" in command ||
            "delete history" in command ||
            "remove history" in command
        ) {
            return VoiceCommand.ClearHistory
        }
        if (
            "history" in command ||
            "transaction history" in command ||
            "show history" in command ||
            "recent transactions" in command
        ) {
            return VoiceCommand.OpenHistory
        }
        if (
            "read history" in command ||
            "speak history" in command ||
            "tell my transactions" in command
        ) {
            return VoiceCommand.ReadHistory
        }

        if ("setting" in command||
            "settings" in command ||
            "open settings" in command||
            "app settings" in command ||
            "open app settings" in command ||
            "show settings" in command
        ) {
            return VoiceCommand.OpenSettings
        }

        if ("pay" in command ||
            "send" in command ||
            "transfer" in command||"send money" in command ||
            "transfer money" in command ||
            "make payment" in command) {

            var amount =
                Regex("\\d+")
                    .find(command)
                    ?.value
                    ?.toIntOrNull()
            if (amount == null) {

                if (
                    "five hundred" in command
                ) {
                    amount = 500
                }

                if (
                    "one thousand" in command
                ) {
                    amount = 1000
                }
            }

            val receiver =
                command.substringAfter(
                    "to",
                    ""
                ).trim()
                    .replace(".", "")
                    .replace(",", "")


            if (
                amount != null &&
                receiver.isNotBlank()
            ) {

                return VoiceCommand.SendMoney(
                    amount = amount,
                    receiver = receiver
                )
            }

            return VoiceCommand.OpenPayment
        }

        return VoiceCommand.Unknown

    }
}