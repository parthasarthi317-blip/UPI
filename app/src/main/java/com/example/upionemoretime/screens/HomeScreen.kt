package com.example.upionemoretime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import com.example.upionemoretime.voice.VoiceState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upionemoretime.ui.components.VoiceAssistantFab
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.upionemoretime.voice.PermissionManager
import com.example.upionemoretime.voice.SpeechRecognitionManager
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.upionemoretime.voice.BalanceStore

import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.WakeWordManager

@Composable
fun HomeScreen(navController: NavController, voiceManager: VoiceManager) {
    var detectedCommand by remember {
        mutableStateOf("No command detected")
    }
    val context = LocalContext.current

    var recognizedText by remember {
        mutableStateOf(
            "No voice command yet"
        )
    }
    var voiceState by remember {
        mutableStateOf(
            VoiceState.WAKE_WORD_LISTENING
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                voiceState = VoiceState.LISTENING

            }
        }


    Box(
        modifier = Modifier
            .fillMaxSize()

            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = 120.dp
                )
        ){

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Good Evening 👋",
                color = Color(0xFF94A3B8),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sambhav",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Last Voice Command",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = detectedCommand,
                        color = Color.Cyan,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recognizedText,
                        color = Color(0xFF22C55E),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Your Voice. Your Payments.",
                color = Color.LightGray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF22C55E)
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${BalanceStore.balance.value}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quick Actions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

//            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(32.dp))

            FeatureCard(
                title = "💸 Send Money",
                onClick = {
                    navController.navigate(
                        Routes.paymentRoute(
                            amount = 0,
                            receiver = "Receiver"
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                title = "📱 Mobile Recharge",
                onClick = {
                    navController.navigate(
                        Routes.RECHARGE
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                title = "🏦 Check Balance",
                onClick = {
                    navController.navigate(
                        Routes.BALANCE
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                title = "⚙️ Settings",
                onClick = {
                    navController.navigate(
                        Routes.SETTINGS
                    )
                }
            )
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            FeatureCard (
                title ="Transaction History",
                onClick = {
                    navController.navigate(
                        Routes.HISTORY
                    )
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = when (voiceState) {

                        VoiceState.WAKE_WORD_LISTENING ->
                            "Waiting for 'Hey Assistant'"

                        VoiceState.FOLLOW_UP_LISTENING ->
                            "Listening for follow-up command..."

                        VoiceState.IDLE ->
                            "Tap to Speak"

                        VoiceState.LISTENING ->
                            "Listening..."

                        VoiceState.PROCESSING ->
                            "Processing..."
                    },
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                VoiceAssistantFab(
                    voiceState = voiceState,
                    onClick = {

                        if (
                            PermissionManager.hasAudioPermission(
                                context
                            )
                        ) {

                            voiceState = VoiceState.LISTENING

                            voiceManager.listenAndHandle(
                                navController = navController
                            )

                        } else {

                            permissionLauncher.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }

                    }

                )

            }
        }

    }
}

@Composable
fun FeatureCard(title: String,
                onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF334155)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "→",
                color = Color.White,
                fontSize = 24.sp
            )
        }
    }
}