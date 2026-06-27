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
import com.example.upionemoretime.voice.VoiceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, voiceManager: VoiceManager) {
    val isHindi = voiceManager.isHindi()
    val allTransactions = remember {
        (TransactionHistoryStore.paymentHistory.map { TransactionItem(it, "Payment") } +
         TransactionHistoryStore.rechargeHistory.map { TransactionItem(it, "Recharge") })
        .sortedByDescending { it.rawText } // Simplified sorting
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isHindi) "लेनदेन का इतिहास" else "Transaction History", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (allTransactions.isEmpty()) {
            EmptyHistoryState(isHindi)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionHeader(title = if (isHindi) "हाल की गतिविधि" else "Recent Activity") }
                items(allTransactions) { transaction ->
                    TransactionRow(transaction, isHindi)
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
                item {
                    Button(
                        onClick = {
                            TransactionHistoryStore.rechargeHistory.clear()
                            TransactionHistoryStore.paymentHistory.clear()
                            voiceManager.speak(if (isHindi) "इतिहास मिटा दिया गया" else "History cleared")
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (isHindi) "इतिहास साफ़ करें" else "Clear History", 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

data class TransactionItem(val rawText: String, val type: String)

@Composable
fun TransactionRow(item: TransactionItem, isHindi: Boolean) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.type == "Payment") Icons.Default.Payment else Icons.Default.Smartphone,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    item.rawText.split(" -> ").getOrElse(1) { if (isHindi) "अज्ञात" else "Unknown" }, 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (item.type == "Payment") (if (isHindi) "भुगतान" else "Payment") else (if (isHindi) "रिचार्ज" else "Recharge"), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                item.rawText.split(" -> ").firstOrNull() ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = if (item.type == "Payment") MaterialTheme.colorScheme.error else Color(0xFF2E7D32), // Emerald for success
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyHistoryState(isHindi: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isHindi) "अभी तक कोई लेनदेन नहीं" else "No transactions yet", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (isHindi) "आपकी गतिविधि यहां दिखाई देगी" else "Your activity will appear here", 
            style = MaterialTheme.typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}
