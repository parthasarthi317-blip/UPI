package com.example.upionemoretime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.PremiumCard
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    amount: Int,
    receiver: String,
    navController: NavController
) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val speechManager = remember { SpeechRecognitionManager(context) }
    
    var paymentSuccess by remember { mutableStateOf(false) }
    var paymentMessage by remember { mutableStateOf("") }

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
                title = { Text("Confirm Payment", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (paymentSuccess) {
                PaymentSuccessState(amount, receiver) {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                }
            } else {
                PaymentConfirmationState(
                    amount = amount,
                    receiver = receiver,
                    paymentMessage = paymentMessage,
                    onConfirm = {
                        if (BalanceStore.balance.value >= amount) {
                            BalanceStore.balance.value -= amount
                            TransactionHistoryStore.paymentHistory.add("₹$amount -> $receiver")
                            paymentSuccess = true
                        } else {
                            ttsManager.speak("Insufficient balance")
                            paymentMessage = "Insufficient Balance"
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentSuccessState(amount: Int, receiver: String, onHomeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SecondaryEmerald,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Payment Successful",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "₹$amount sent to $receiver",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            Text("Back To Home", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentConfirmationState(
    amount: Int,
    receiver: String,
    paymentMessage: String,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        PremiumCard {
            Text(
                text = "PAYING TO",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = receiver,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "AMOUNT",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryIndigo,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (paymentMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = paymentMessage,
                color = ErrorRose,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald)
        ) {
            Text("Confirm Payment", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
