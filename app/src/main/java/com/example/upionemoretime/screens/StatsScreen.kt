package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.ui.components.PremiumCard
import com.example.upionemoretime.ui.components.SectionHeader
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.SpeechRecognitionManager
import com.example.upionemoretime.voice.TextToSpeechManager
import com.example.upionemoretime.voice.TransactionHistoryStore
import com.example.upionemoretime.voice.VoiceCommandParser
import com.example.upionemoretime.voice.VoiceNavigationHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val context = LocalContext.current
    val speechManager = remember { SpeechRecognitionManager(context) }
    val ttsManager = remember { TextToSpeechManager(context) }

    val paymentCount = TransactionHistoryStore.paymentHistory.size
    val rechargeCount = TransactionHistoryStore.rechargeHistory.size

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
            ttsManager.shutdown()
        }
    }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            TopAppBar(
                title = { Text("Analytics", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.BarChart, null, tint = PrimaryIndigo, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(32.dp))
            
            PremiumCard {
                StatRow("Total Payments", paymentCount.toString(), PrimaryIndigo)
                Spacer(modifier = Modifier.height(16.dp))
                StatRow("Total Recharges", rechargeCount.toString(), SecondaryEmerald)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(title = "Insights")
            
            PremiumCard {
                Text(
                    "You've made more recharges than payments this month. Try using voice to explore more features!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = CardSurface)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
