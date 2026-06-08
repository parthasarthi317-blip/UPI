package com.example.upionemoretime.voice

import android.content.Context
import androidx.navigation.NavController
import com.example.upionemoretime.voice.TextToSpeechManager
object VoiceLauncher {

    fun startListening(
        context: Context,
        navController: NavController
    ) {

        val voiceManager =
            VoiceManager(context)

        voiceManager.listenAndHandle(
            navController = navController
        )
    }
}