package com.example.upionemoretime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.components.PremiumCard
import com.example.upionemoretime.ui.components.SectionHeader
import com.example.upionemoretime.ui.theme.*
import com.example.upionemoretime.voice.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    navController: NavController,
    voiceManager: VoiceManager,
    initialMobileNumber: String = "",
    initialAmount: Int = 0
) {
    var rechargeSuccess by remember { mutableStateOf(false) }
    var mobileNumber by remember { mutableStateOf(initialMobileNumber) }
    var rechargeAmount by remember { mutableStateOf(if (initialAmount == 0) "" else initialAmount.toString()) }
    var showConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        voiceManager.authenticatedCommands.collect { command ->
            if (command == VoiceCommand.ConfirmRecharge) {
                TransactionHistoryStore.rechargeHistory.add("₹$rechargeAmount -> $mobileNumber")
                rechargeSuccess = true
                showConfirmation = false
            }
        }
    }

    Scaffold(
        containerColor = Obsidian,
        topBar = {
            TopAppBar(
                title = { Text("Mobile Recharge", style = MaterialTheme.typography.titleLarge) },
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
            if (rechargeSuccess) {
                RechargeSuccessState(rechargeAmount, mobileNumber) {
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                }
            } else {
                RechargeInputState(
                    mobileNumber = mobileNumber,
                    onMobileChange = { mobileNumber = it },
                    rechargeAmount = rechargeAmount,
                    onAmountChange = { rechargeAmount = it },
                    onProceed = {
                        if (mobileNumber.length == 10 && rechargeAmount.toIntOrNull() != null) {
                            showConfirmation = true
                        } else {
                            voiceManager.speak("Invalid details")
                        }
                    }
                )

                if (showConfirmation) {
                    ModalBottomSheet(
                        onDismissRequest = { showConfirmation = false },
                        containerColor = DeepSlate
                    ) {
                        ConfirmationContent(
                            mobileNumber = mobileNumber,
                            rechargeAmount = rechargeAmount,
                            onConfirm = {
                                voiceManager.triggerSensitiveCommand(VoiceCommand.ConfirmRecharge)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RechargeInputState(
    mobileNumber: String,
    onMobileChange: (String) -> Unit,
    rechargeAmount: String,
    onAmountChange: (String) -> Unit,
    onProceed: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        PremiumCard {
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = onMobileChange,
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = rechargeAmount,
                onValueChange = onAmountChange,
                label = { Text("Recharge Amount") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                prefix = { Text("₹") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        SectionHeader(title = "Popular Plans")
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("199", "299", "719").forEach { plan ->
                Button(
                    onClick = { onAmountChange(plan) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rechargeAmount == plan) PrimaryIndigo else CardSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("₹$plan")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            Text("Proceed", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ConfirmationContent(mobileNumber: String, rechargeAmount: String, onConfirm: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 24.dp)) {
        Text("Review Recharge", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        PremiumCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("NUMBER", style = MaterialTheme.typography.labelSmall)
                    Text(mobileNumber, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AMOUNT", style = MaterialTheme.typography.labelSmall)
                    Text("₹$rechargeAmount", style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald)
        ) {
            Text("Confirm Recharge", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RechargeSuccessState(amount: String, mobileNumber: String, onHomeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = SecondaryEmerald, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("Recharge Successful", style = MaterialTheme.typography.headlineMedium)
        Text("₹$amount recharge completed for $mobileNumber", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onHomeClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = CircleShape) {
            Text("Back To Home")
        }
    }
}
