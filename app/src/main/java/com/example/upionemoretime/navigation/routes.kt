package com.example.upionemoretime.navigation

object Routes {
    const val HISTORY = "history"

    const val HOME = "home"
    const val RECHARGE_ROUTE =
        "recharge/{mobileNumber}/{amount}"

    const val PAYMENT = "payment/{amount}/{receiver}"
    fun paymentRoute(
        amount: Int,
        receiver: String
    ): String {

        return "payment/$amount/$receiver"
    }
    fun rechargeRoute(
        mobileNumber: String,
        amount: Int
    ): String {
        return "recharge/$mobileNumber/$amount"
    }

    const val BALANCE = "balance"

    const val RECHARGE = "recharge"

    const val SETTINGS = "settings"
    const val STATS = "stats"
    const val SCAN_QR = "scan_qr"
    
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val OTP_VERIFICATION = "otp_verification/{mobileNumber}/{isLogin}"
    
    fun otpRoute(mobileNumber: String, isLogin: Boolean): String {
        return "otp_verification/$mobileNumber/$isLogin"
    }
}