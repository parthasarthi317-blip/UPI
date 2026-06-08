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
}