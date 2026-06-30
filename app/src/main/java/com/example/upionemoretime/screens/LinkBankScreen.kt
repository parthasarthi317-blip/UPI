package com.example.upionemoretime.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.upionemoretime.navigation.Routes
import com.example.upionemoretime.ui.theme.SecondaryEmerald
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.VoiceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Bank(val name: String, val icon: ImageVector)

val banks = listOf(
    Bank("State Bank of India", Icons.Default.AccountBalance),
    Bank("HDFC Bank", Icons.Default.AccountBalance),
    Bank("ICICI Bank", Icons.Default.AccountBalance),
    Bank("Punjab National Bank", Icons.Default.AccountBalance),
    Bank("Axis Bank", Icons.Default.AccountBalance),
    Bank("Bank of Baroda", Icons.Default.AccountBalance),
    Bank("Canara Bank", Icons.Default.AccountBalance),
    Bank("Union Bank", Icons.Default.AccountBalance),
    Bank("Kotak Mahindra Bank", Icons.Default.AccountBalance),
    Bank("IndusInd Bank", Icons.Default.AccountBalance)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBankScreen(navController: NavController, voiceManager: VoiceManager) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bank_prefs", Context.MODE_PRIVATE) }
    
    var linkedBank by remember { mutableStateOf(prefs.getString("linked_bank", null)) }
    var selectedBank by remember { mutableStateOf<Bank?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLinking by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isHindi = voiceManager.isHindi()

    val filteredBanks = banks.filter { it.name.contains(searchQuery, ignoreCase = true) }

    LaunchedEffect(Unit) {
        if (linkedBank == null) {
            val mobile = voiceManager.getUserMobile()
            val last4 = if (mobile.length >= 4) mobile.takeLast(4) else "XXXX"
            voiceManager.speak(
                if (isHindi) "मुझे आपका पंजीकृत मोबाइल नंबर मिला है जिसके अंत में $last4 है। अब कृपया वह बैंक चुनें जिसे आप लिंक करना चाहते हैं।"
                else "I found your registered mobile number ending with $last4. Now please choose the bank you want to link.",
                startListeningAfter = true
            )
        }
    }

    LaunchedEffect(Unit) {
        voiceManager.setLinkBankListeners(
            onBankSelected = { bankName ->
                val bank = banks.find { it.name.equals(bankName, ignoreCase = true) }
                if (bank != null) {
                    selectedBank = bank
                    showConfirmation = true
                }
            },
            onConfirm = {
                scope.launch {
                    showConfirmation = false
                    isLinking = true
                    delay(2500)
                    linkedBank = selectedBank?.name
                    prefs.edit().putString("linked_bank", linkedBank).apply()
                    isLinking = false
                    voiceManager.speak(
                        if (isHindi) "बधाई हो! आपका बैंक खाता सफलतापूर्वक लिंक हो गया है।"
                        else "Congratulations! Your bank account has been linked successfully."
                    )
                }
            },
            onAddAnother = {
                linkedBank = null
                // We don't clear prefs here as we are adding another, but the app currently 
                // only tracks one "linked_bank". If we want to support multiple, we'd need a list.
                // For now, clearing state is enough to show the selection UI.
            },
            onUnlink = {
                linkedBank = null
                prefs.edit().remove("linked_bank").apply()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "बैंक खाता लिंक करें" else "Link Bank Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLinking) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (isHindi) "आपका खाता लिंक किया जा रहा है..." else "Linking your account...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (linkedBank != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryEmerald,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        if (isHindi) "सफलतापूर्वक लिंक हुआ" else "Successfully Linked",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            InfoRow(label = if (isHindi) "बैंक का नाम" else "Bank Name", value = linkedBank!!)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            InfoRow(label = if (isHindi) "मोबाइल नंबर" else "Mobile Number", value = voiceManager.getUserMobile())
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            val last4 = if (voiceManager.getUserMobile().length >= 4) voiceManager.getUserMobile().takeLast(4) else "4321"
                            InfoRow(label = if (isHindi) "खाता संख्या" else "Account Number", value = "XXXX$last4")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            InfoRow(label = if (isHindi) "खाता प्रकार" else "Account Type", value = if (isHindi) "बचत" else "Savings")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            InfoRow(label = if (isHindi) "यूपीआई स्थिति" else "UPI Status", value = if (isHindi) "सक्रिय" else "Active", valueColor = SecondaryEmerald)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    OutlinedButton(
                        onClick = {
                            linkedBank = null
                            voiceManager.speak(
                                if (isHindi) "ठीक है, कृपया वह बैंक चुनें जिसे आप लिंक करना चाहते हैं।"
                                else "Sure, please choose the bank you want to link.",
                                startListeningAfter = true,
                                nextState = VoiceState.LINK_BANK_SELECTION
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isHindi) "दूसरा खाता जोड़ें" else "Add Another Account")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = {
                            linkedBank = null
                            prefs.edit().remove("linked_bank").apply()
                        }
                    ) {
                        Text(if (isHindi) "खाता अनलिंक करें" else "Unlink Account", color = Color.Red)
                    }
                    
                    Button(
                        onClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isHindi) "होम पर जाएं" else "Go to Home")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text(if (isHindi) "अपना बैंक खोजें" else "Search your bank") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = if (isHindi) "लोकप्रिय बैंक" else "Popular Banks",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBanks) { bank ->
                            BankItem(bank = bank, onClick = {
                                selectedBank = bank
                                showConfirmation = true
                                voiceManager.speak(
                                    if (isHindi) "आपने ${bank.name} चुना है। क्या मैं इस खाते को लिंक करूँ?"
                                    else "You selected ${bank.name}. Shall I link this account?",
                                    startListeningAfter = true
                                )
                            })
                        }
                    }
                }
            }

            if (showConfirmation && selectedBank != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmation = false },
                    title = { Text(if (isHindi) "खाता लिंक करें?" else "Link Account?") },
                    text = { Text(if (isHindi) "क्या आप ${selectedBank?.name} को अपने यूपीआई आईडी से जोड़ना चाहते हैं?" else "Do you want to link ${selectedBank?.name} to your UPI ID?") },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch {
                                showConfirmation = false
                                isLinking = true
                                delay(2500)
                                linkedBank = selectedBank?.name
                                prefs.edit().putString("linked_bank", linkedBank).apply()
                                isLinking = false
                                voiceManager.speak(
                                    if (isHindi) "बधाई हो! आपका बैंक खाता सफलतापूर्वक लिंक हो गया है।"
                                    else "Congratulations! Your bank account has been linked successfully."
                                )
                            }
                        }) {
                            Text(if (isHindi) "हाँ" else "Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmation = false }) {
                            Text(if (isHindi) "नहीं" else "No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BankItem(bank: Bank, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(bank.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = bank.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
