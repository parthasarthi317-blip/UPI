package com.example.upionemoretime.voice

import android.os.Handler
import android.os.Looper

class FollowUpSessionManager {

    private val handler = Handler(Looper.getMainLooper())

    private var timeoutRunnable: Runnable? = null

    fun startTimeout(
        timeoutMillis: Long = 60_000,
        onTimeout: () -> Unit
    ) {
        stopTimeout()

        timeoutRunnable = Runnable {
            onTimeout()
        }

        handler.postDelayed(
            timeoutRunnable!!,
            timeoutMillis
        )
    }

    fun stopTimeout() {
        timeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        timeoutRunnable = null
    }
}