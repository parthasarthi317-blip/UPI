package com.example.upionemoretime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.ui.components.PremiumCard
import com.example.upionemoretime.ui.components.SectionHeader
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.TransactionHistoryStore
import com.example.upionemoretime.voice.TextToSpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    
    val allTransactions = remember {
        (TransactionHistoryStore.paymentHistory.map { TransactionItem(it, "Payment") } +
         TransactionHistoryStore.rechargeHistory.map { TransactionItem(it, "Recharge") })
        .sortedByDescending { it.rawText } // Simplified sorting
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (allTransactions.isEmpty()) {
            EmptyHistoryState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionHeader(title = "Recent Activity") }
                items(allTransactions) { transaction ->
                    TransactionRow(transaction)
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
                item {
                    Button(
                        onClick = {
                            TransactionHistoryStore.rechargeHistory.clear()
                            TransactionHistoryStore.paymentHistory.clear()
                            ttsManager.speak("History cleared")
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Clear History", color = ErrorRose)
                    }
                }
            }
        }
    }
}

data class TransactionItem(val rawText: String, val type: String)

@Composable
fun TransactionRow(item: TransactionItem) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Obsidian),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.type == "Payment") Icons.Default.Payment else Icons.Default.Smartphone,
                    null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(item.rawText.split(" -> ").getOrElse(1) { "Unknown" }, style = MaterialTheme.typography.titleLarge)
                Text(item.type, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                item.rawText.split(" -> ").firstOrNull() ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = if (item.type == "Payment") ErrorRose else SecondaryEmerald,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No transactions yet", style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
        Text("Your activity will appear here", style = MaterialTheme.typography.bodyLarge, color = TextDisabled)
    }
}
