package com.example.upionemoretime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.upionemoretime.voice.VoiceState

@Composable
fun VoiceAssistantFab(

    voiceState: VoiceState,

    onClick: () -> Unit

) {

    val backgroundColor = when (voiceState) {

        VoiceState.IDLE -> Color(0xFF22C55E)

        VoiceState.LISTENING -> Color(0xFF3B82F6)

        VoiceState.PROCESSING -> Color(0xFFF59E0B)

        VoiceState.WAKE_WORD_LISTENING -> Color(0xFF8B5CF6)

        VoiceState.FOLLOW_UP_LISTENING -> Color(0xFF06B6D4)
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .background(
                backgroundColor,
                CircleShape
            )
    ) {

        Icon(
            imageVector = when (voiceState) {

                VoiceState.IDLE ->
                    Icons.Default.Mic

                VoiceState.LISTENING ->
                    Icons.Default.SettingsVoice

                VoiceState.PROCESSING ->
                    Icons.Default.SettingsVoice

                VoiceState.WAKE_WORD_LISTENING ->
                    Icons.Default.SettingsVoice

                VoiceState.FOLLOW_UP_LISTENING ->
                    Icons.Default.SettingsVoice
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}