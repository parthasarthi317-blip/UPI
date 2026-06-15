package com.example.upionemoretime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.upionemoretime.navigation.AppNavigation
import com.example.upionemoretime.ui.theme.UPIOneMoreTimeTheme
import com.example.upionemoretime.voice.VoiceManager

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(this)
        voiceManager.startWakeWordDetection()

        setContent {
            UPIOneMoreTimeTheme {
                AppNavigation(
                    voiceManager = voiceManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        voiceManager.destroy()
    }
}