package com.example.upionemoretime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.example.upionemoretime.navigation.AppNavigation
import com.example.upionemoretime.ui.theme.UPIOneMoreTimeTheme
import com.example.upionemoretime.voice.VoiceManager

class MainActivity : FragmentActivity() {

    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(this)
        voiceManager.startWakeWordDetection()

        setContent {
            var isDarkMode by remember { mutableStateOf(voiceManager.isDarkMode()) }
            UPIOneMoreTimeTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    voiceManager = voiceManager,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { 
                        isDarkMode = it 
                        voiceManager.setDarkMode(it)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        voiceManager.destroy()
    }
}