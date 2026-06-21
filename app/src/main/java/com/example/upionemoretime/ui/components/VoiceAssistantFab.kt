package com.example.upionemoretime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.VoiceState

@Composable
fun VoiceAssistantFab(
    voiceState: VoiceState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val isWorking = voiceState == VoiceState.LISTENING || 
                     voiceState == VoiceState.ENROLLING || voiceState == VoiceState.ENROLLING_VOICE ||
                     voiceState == VoiceState.AUTHENTICATING || voiceState == VoiceState.AUTHENTICATING_VOICE

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWorking) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowColor = when (voiceState) {
        VoiceState.LISTENING, VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE,
        VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE -> SecondaryEmerald
        VoiceState.PROCESSING -> WarningAmber
        VoiceState.RESPONDING -> PrimaryIndigo
        VoiceState.UNAUTHORIZED -> ErrorRose
        else -> PrimaryIndigo.copy(alpha = 0.5f)
    }

    Box(contentAlignment = Alignment.Center) {
        // Outer Glow
        if (isWorking) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.2f))
            )
        }

        // Main Button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientIndigo))
                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = if (isWorking) Icons.Default.SettingsVoice else Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
