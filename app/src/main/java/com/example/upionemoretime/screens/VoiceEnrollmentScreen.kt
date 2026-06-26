package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.VoiceState

@Composable
fun VoiceEnrollmentScreen(
    navController: NavController,
    voiceManager: VoiceManager
) {
    val voiceState by voiceManager.state.collectAsState()
    val progressCount by voiceManager.enrollmentProgress.collectAsState()
    
    LaunchedEffect(Unit) {
        voiceManager.triggerEnrollmentFlow()
    }

    Scaffold(
        containerColor = Obsidian
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Voice Enrollment",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Secure your account with your unique voice print.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = if (voiceState == VoiceState.ENROLLING_VOICE) SecondaryEmerald else PrimaryIndigo.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryIndigo)
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.ENROLLING_VOICE) Icons.Default.SettingsVoice else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).padding(32.dp),
                        tint = if (voiceState == VoiceState.ENROLLING_VOICE) Color.White else PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = when (voiceState) {
                    VoiceState.ENROLLING -> "Listen to the phrase..."
                    VoiceState.ENROLLING_VOICE -> "Repeat the phrase clearly."
                    else -> if (progressCount >= 3) "Enrollment Complete!" else "Setting up your voice profile..."
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (voiceState == VoiceState.ENROLLING_VOICE) SecondaryEmerald else TextPrimary
            )

            Spacer(modifier = Modifier.height(64.dp))

            LinearProgressIndicator(
                progress = { progressCount / 3f },
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = SecondaryEmerald,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Step $progressCount of 3",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
