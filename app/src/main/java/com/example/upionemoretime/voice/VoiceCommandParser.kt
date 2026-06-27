package com.example.upionemoretime.voice

private val wordToDigit = mapOf(
    "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
    "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
    "शून्य" to "0", "एक" to "1", "दो" to "2", "तीन" to "3", "चार" to "4",
    "पांच" to "5", "छह" to "6", "सात" to "7", "आठ" to "8", "नौ" to "9"
)

object VoiceCommandParser {

    fun parse(text: String, currentState: VoiceState = VoiceState.IDLE): VoiceCommand {
        val command = text.lowercase().trim()

        // Handle direct Yes/No for flow control
        if (command in listOf("yes", "correct", "yeah", "yep", "हाँ", "हा", "सही है")) return VoiceCommand.Yes
        if (command in listOf("no", "incorrect", "nope", "नहीं", "गलत है")) return VoiceCommand.No

        // If we are in a data-entry state, we might want to return the raw or processed text as DataInput
        if (isDataEntryState(currentState)) {
            return VoiceCommand.DataInput(processDataInput(command, currentState))
        }

        // Global triggers
        if (("login" in command || "लॉगिन" in command) && ("help" in command || "start" in command || "open" in command || "शुरू" in command || "खोलो" in command)) return VoiceCommand.StartLogin
        if (("signup" in command || "साइन अप" in command) && ("help" in command || "start" in command || "open" in command || "शुरू" in command || "खोलो" in command)) return VoiceCommand.StartSignup

        if ("go back" in command || "back" in command || "previous screen" in command || "पीछे" in command || "वापस" in command) return VoiceCommand.GoBack
        if ("go home" in command || "home" in command || "dashboard" in command || "होम" in command || "मुख्य स्क्रीन" in command) return VoiceCommand.GoHome
        
        if ("confirm recharge" in command || "रिचार्ज कंफर्म" in command || "रिचार्ज करें" in command) return VoiceCommand.ConfirmRecharge
        if ("cancel recharge" in command || "रिचार्ज कैंसिल" in command || "रद्द करें" in command) return VoiceCommand.CancelRecharge
        if ("confirm payment" in command || "confirm" in command || "pay now" in command || "पेमेंट करें" in command || "भुगतान करें" in command) return VoiceCommand.ConfirmPayment
        if ("cancel payment" in command || "cancel" in command || "abort" in command || "कैंसिल" in command || "रद्द" in command) return VoiceCommand.CancelPayment

        if ("payment details" in command || "details" in command || "विवरण" in command) return VoiceCommand.PaymentDetails
        if ("repeat" in command || "दोहराएं" in command) return VoiceCommand.RepeatDetails
        if ("clear" in command || "साफ करें" in command) return VoiceCommand.ClearDetails
        if ("balance" in command || "बैलेंस" in command || "पैसे देखें" in command) {
            if ("show" in command || "reveal" in command || "दिखाएं" in command || "देखें" in command) return VoiceCommand.RevealBalance
            return VoiceCommand.CheckBalance
        }
        
        if (("199" in command || "एक सौ निन्यानवे" in command) && ("plan" in command || "प्लान" in command)) return VoiceCommand.SelectRechargePlan(199)
        if (("299" in command || "दो सौ निन्यानवे" in command) && ("plan" in command || "प्लान" in command)) return VoiceCommand.SelectRechargePlan(299)
        if (("719" in command || "सात सौ उन्नीस" in command) && ("plan" in command || "प्लान" in command)) return VoiceCommand.SelectRechargePlan(719)

        if (("recharge" in command || "रिचार्ज" in command) && ("with" in command || "के साथ" in command)) {
            val numbers = Regex("\\d+").findAll(convertSpokenDigits(command)).map { it.value }.toList()
            if (numbers.size >= 2) {
                return VoiceCommand.RechargeMobile(numbers[0], numbers[1].toIntOrNull() ?: 0)
            }
        }

        if ("recharge" in command || "रिचार्ज" in command) return VoiceCommand.OpenRecharge
        if ("statistics" in command || "stats" in command || "आंकड़े" in command) return VoiceCommand.OpenStatistics
        
        if ("scan qr" in command || "scanner" in command || "स्कैन" in command) return VoiceCommand.ScanQR
        if ("reset voice" in command || "आवाज रीसेट" in command) return VoiceCommand.ResetVoice
        if ("clear history" in command || "इतिहास मिटाएं" in command) return VoiceCommand.ClearHistory
        if ("read history" in command || "इतिहास पढ़ें" in command) return VoiceCommand.ReadHistory
        if ("history" in command || "इतिहास" in command || "लेनदेन" in command) return VoiceCommand.OpenHistory
        if ("settings" in command || "सेटिंग" in command) return VoiceCommand.OpenSettings

        if ("pay" in command || "send" in command || "transfer" in command || "भेजें" in command || "पैसे दें" in command || "भुगतान" in command || "बहेजो" in command) {
            val processedCommand = convertSpokenDigits(command)
            val amount = Regex("\\d+").find(processedCommand)?.value?.toIntOrNull()
            
            // Extract receiver from English or Hindi structure
            var receiver = ""
            if ("to" in command) {
                receiver = command.substringAfter("to", "").trim().replace(".", "")
            } else if ("को" in command) {
                receiver = command.substringBefore("को", "").split(" ").last().trim()
            }

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
