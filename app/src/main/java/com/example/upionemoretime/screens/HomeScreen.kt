package com.example.upionemoretime.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.*
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.BalanceStore
import com.example.upionemoretime.voice.PermissionManager
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.VoiceState

@Composable
fun HomeScreen(navController: NavController, voiceManager: VoiceManager) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val voiceState by voiceManager.state.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceManager.listenAndHandle(navController = navController)
        }
    }

    Scaffold(
        containerColor = Obsidian,
        bottomBar = {
            VoiceAssistantSection(
                voiceState = voiceState,
                onClick = {
                    if (PermissionManager.hasAudioPermission(context)) {
                        voiceManager.listenAndHandle(navController = navController)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) {
paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Greeting Section
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Text(
                text = "Sambhav",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Hero Balance Card
            PremiumCard(
                gradient = GradientIndigo
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${BalanceStore.balance.value}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { navController.navigate(Routes.BALANCE) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = "Quick Actions")
            
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = "Scan QR",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = { navController.navigate(Routes.SCAN_QR) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuickActionChip(
                    title = "Send",
                    icon = Icons.Default.Send,
                    onClick = { navController.navigate(Routes.paymentRoute(0, "Receiver")) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = "Recharge",
                    icon = Icons.Default.Smartphone,
                    onClick = { navController.navigate(Routes.RECHARGE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuickActionChip(
                    title = "History",
                    icon = Icons.Default.History,
                    onClick = { navController.navigate(Routes.HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = "Stats",
                    icon = Icons.Default.BarChart,
                    onClick = { navController.navigate(Routes.STATS) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = "Voice Security")
            
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = SecondaryEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Voice Identity",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Used for high-value transactions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { voiceManager.resetVoiceData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRose.copy(alpha = 0.2f),
                                contentColor = ErrorRose
                            ),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Voice Print", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = "App Experience")
            
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Voice Controlled",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Just say 'Hey Assistant' to pay or recharge.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun VoiceAssistantSection(
    voiceState: VoiceState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Obsidian.copy(alpha = 0.9f))
                )
            )
            .padding(bottom = 24.dp, top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (voiceState) {
                    VoiceState.WAKING -> "Say 'Hey Assistant'"
                    VoiceState.LISTENING -> "I'm listening..."
                    VoiceState.PROCESSING -> "Processing..."
                    VoiceState.PROMPTING -> "Thinking..."
                    VoiceState.RESPONDING -> "One moment..."
                    VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE -> "Voice Enrollment..."
                    VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE -> "Verifying Identity..."
                    VoiceState.UNAUTHORIZED -> "Access Denied"
                    else -> "Tap to speak"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (voiceState) {
                    VoiceState.LISTENING, VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE,
                    VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE -> SecondaryEmerald
                    VoiceState.UNAUTHORIZED -> ErrorRose
                    else -> TextSecondary
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoiceAssistantFab(
                voiceState = voiceState,
                onClick = onClick
            )
        }
    }
}
