package com.example.upionemoretime.voice

import androidx.compose.runtime.mutableStateListOf

object TransactionHistoryStore {

    val paymentHistory =
        mutableStateListOf<String>()

    val rechargeHistory =
        mutableStateListOf<String>()
}