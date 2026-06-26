package com.example.upionemoretime.voice

private val wordToDigit = mapOf(
    "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
    "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9"
)

object VoiceCommandParser {

    fun parse(text: String, currentState: VoiceState = VoiceState.IDLE): VoiceCommand {
        val command = text.lowercase().trim()

        // Handle direct Yes/No for flow control
        if (command == "yes" || command == "correct" || command == "yeah" || command == "yep") return VoiceCommand.Yes
        if (command == "no" || command == "incorrect" || command == "nope") return VoiceCommand.No

        // If we are in a data-entry state, we might want to return the raw or processed text as DataInput
        if (isDataEntryState(currentState)) {
            return VoiceCommand.DataInput(processDataInput(command, currentState))
        }

        // Global triggers
        if ("login" in command && ("help" in command || "start" in command || "open" in command)) return VoiceCommand.StartLogin
        if ("signup" in command && ("help" in command || "start" in command || "open" in command)) return VoiceCommand.StartSignup

        if ("go back" in command || "back" in command || "previous screen" in command) return VoiceCommand.GoBack
        if ("go home" in command || "home" in command || "dashboard" in command) return VoiceCommand.GoHome
        
        if ("confirm recharge" in command) return VoiceCommand.ConfirmRecharge
        if ("cancel recharge" in command) return VoiceCommand.CancelRecharge
        if ("confirm payment" in command || "confirm" in command || "pay now" in command) return VoiceCommand.ConfirmPayment
        if ("cancel payment" in command || "cancel" in command || "abort" in command) return VoiceCommand.CancelPayment

        if ("payment details" in command || "details" in command) return VoiceCommand.PaymentDetails
        if ("repeat" in command) return VoiceCommand.RepeatDetails
        if ("clear" in command) return VoiceCommand.ClearDetails
        if ("balance" in command) return VoiceCommand.CheckBalance
        
        if ("199" in command && "plan" in command) return VoiceCommand.SelectRechargePlan(199)
        if ("299" in command && "plan" in command) return VoiceCommand.SelectRechargePlan(299)
        if ("719" in command && "plan" in command) return VoiceCommand.SelectRechargePlan(719)

        if ("recharge" in command && "with" in command) {
            val numbers = Regex("\\d+").findAll(command).map { it.value }.toList()
            if (numbers.size >= 2) {
                return VoiceCommand.RechargeMobile(numbers[0], numbers[1].toIntOrNull() ?: 0)
            }
        }

        if ("recharge" in command) return VoiceCommand.OpenRecharge
        if ("statistics" in command || "stats" in command) return VoiceCommand.OpenStatistics
        
        if ("scan qr" in command || "scanner" in command) return VoiceCommand.ScanQR
        if ("reset voice" in command) return VoiceCommand.ResetVoice
        if ("clear history" in command) return VoiceCommand.ClearHistory
        if ("read history" in command) return VoiceCommand.ReadHistory
        if ("history" in command) return VoiceCommand.OpenHistory
        if ("settings" in command) return VoiceCommand.OpenSettings

        if ("pay" in command || "send" in command || "transfer" in command) {
            val amount = Regex("\\d+").find(command)?.value?.toIntOrNull()
            val receiver = command.substringAfter("to", "").trim().replace(".", "")
            if (amount != null && receiver.isNotBlank()) {
                return VoiceCommand.SendMoney(amount, receiver)
            }
            return VoiceCommand.OpenPayment
        }

        return VoiceCommand.Unknown
    }

    private fun isDataEntryState(state: VoiceState): Boolean {
        return state in listOf(
            VoiceState.LOGIN_MOBILE, VoiceState.LOGIN_PASSWORD,
            VoiceState.SIGNUP_NAME, VoiceState.SIGNUP_MOBILE, VoiceState.SIGNUP_EMAIL,
            VoiceState.SIGNUP_PASSWORD, VoiceState.SIGNUP_CONFIRM_PASSWORD,
            VoiceState.OTP_INPUT
        )
    }

    private fun processDataInput(text: String, state: VoiceState): String {
        return when (state) {
            VoiceState.LOGIN_MOBILE, VoiceState.SIGNUP_MOBILE, VoiceState.OTP_INPUT -> convertSpokenDigits(text)
            VoiceState.SIGNUP_EMAIL -> convertSpokenEmail(text)
            VoiceState.LOGIN_PASSWORD, VoiceState.SIGNUP_PASSWORD, VoiceState.SIGNUP_CONFIRM_PASSWORD -> {
                // Remove "my password is" or similar filler if present
                text.replace("my password is", "").trim()
            }
            else -> text
        }
    }

    private fun convertSpokenDigits(text: String): String {
        var result = text
        wordToDigit.forEach { (word, digit) ->
            result = result.replace(word, digit)
        }
        return result.replace(Regex("[^0-9]"), "")
    }

    private fun convertSpokenEmail(text: String): String {
        return text.lowercase()
            .replace(" at ", "@")
            .replace(" dot ", ".")
            .replace(" ", "")
            .trim()
    }
}
