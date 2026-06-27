package com.example.upionemoretime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.ui.components.PremiumCard
import com.example.upionemoretime.ui.components.SectionHeader
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.TransactionHistoryStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, voiceManager: VoiceManager) {
    val paymentCount = TransactionHistoryStore.paymentHistory.size
    val rechargeCount = TransactionHistoryStore.rechargeHistory.size
    val isHindi = voiceManager.isHindi()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isHindi) "एनालिटिक्स" else "Analytics", 
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.BarChart, 
                null, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            PremiumCard {
                StatRow(
                    if (isHindi) "कुल भुगतान" else "Total Payments", 
                    paymentCount.toString(), 
                    MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                StatRow(
                    if (isHindi) "कुल रिचार्ज" else "Total Recharges", 
                    rechargeCount.toString(), 
                    MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(title = if (isHindi) "इनसाइट्स" else "Insights")
            
            PremiumCard {
                Text(
                    if (isHindi) "आपने इस महीने भुगतान से अधिक रिचार्ज किए हैं। अधिक सुविधाओं को खोजने के लिए आवाज का उपयोग करें!"
                    else "You've made more recharges than payments this month. Try using voice to explore more features!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    if (isHindi) "बंद करें" else "Close", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
    }
}
