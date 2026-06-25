package com.example.upionemoretime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    navController: NavController,
    voiceManager: VoiceManager
) {
    var paymentSuccess by remember { mutableStateOf(false) }
    var paymentMessage by remember { mutableStateOf("") }
    var editableAmount by remember { mutableStateOf(if (amount > 0) amount.toString() else "") }

    LaunchedEffect(Unit) {
        if (amount <= 0) {
            voiceManager.speak("How much money do you want to pay?", startListeningAfter = true)
        }
        
        voiceManager.authenticatedCommands.collect { command ->
            when (command) {
                is VoiceCommand.ConfirmPayment -> {
                    val finalAmount = editableAmount.toIntOrNull() ?: 0
                    if (finalAmount > 0) {
                        if (BalanceStore.balance.value >= finalAmount) {
                            BalanceStore.balance.value -= finalAmount
                            TransactionHistoryStore.paymentHistory.add("₹$finalAmount -> $receiver")
                            paymentSuccess = true
                        } else {
                            voiceManager.speak("Insufficient balance")
                            paymentMessage = "Insufficient Balance"
                        }
                    } else {
                        voiceManager.speak("Please specify a valid amount first.")
                    }
                }
                is VoiceCommand.SetAmount -> {
                    editableAmount = command.amount.toString()
                    voiceManager.speak("Setting amount to ${command.amount} rupees. You can now confirm the payment.")
                }
                else -> {}
            }
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
                PaymentSuccessState(editableAmount.toIntOrNull() ?: 0, receiver) {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                }
            } else {
                PaymentConfirmationState(
                    amount = editableAmount,
                    onAmountChange = { editableAmount = it },
                    receiver = receiver,
                    paymentMessage = paymentMessage,
                    onConfirm = {
                        voiceManager.triggerSensitiveCommand(VoiceCommand.ConfirmPayment)
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
    amount: String,
    onAmountChange: (String) -> Unit,
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
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.fillMaxWidth(),
                prefix = { 
                    Text(
                        "₹", 
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = PrimaryIndigo,
                    focusedTextColor = PrimaryIndigo,
                    unfocusedTextColor = PrimaryIndigo
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
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
