package com.example.upionemoretime.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.upionemoretime.voice.VoiceCommand
import com.example.upionemoretime.voice.VoiceManager
import com.example.upionemoretime.voice.VoiceState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    voiceManager: VoiceManager,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val voiceState by voiceManager.state.collectAsState()
    var isBalanceVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        voiceManager.authenticatedCommands.collect { command ->
            if (command is VoiceCommand.RevealBalance) {
                isBalanceVisible = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceManager.listenAndHandle(navController = navController)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideDrawerContent(
                userName = voiceManager.getUserName(),
                isDarkMode = isDarkMode,
                onDarkModeChange = { 
                    onDarkModeChange(it)
                },
                isHindi = voiceManager.isHindi(),
                onLanguageChange = { voiceManager.setLanguage(it) },
                onClose = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (voiceManager.isHindi()) "यूपीआई सहायक" else "UPI Assistant",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
            VoiceAssistantSection(
                voiceState = voiceState,
                voiceManager = voiceManager,
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
                text = if (voiceManager.isHindi()) "वापस स्वागत है," else "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = voiceManager.getUserName(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Hero Balance Card
            PremiumCard(
                gradient = GradientIndigo
            ) {
                Text(
                    text = if (voiceManager.isHindi()) "कुल बैलेंस" else "Total Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        if (!isBalanceVisible) {
                            voiceManager.requestManualBiometric(
                                "Verify to see balance",
                                onAuthenticated = { isBalanceVisible = true }
                            )
                        } else {
                            isBalanceVisible = false
                        }
                    }
                ) {
                    Text(
                        text = if (isBalanceVisible) "₹${BalanceStore.balance.value}" else "••••••",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
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

            SectionHeader(title = if (voiceManager.isHindi()) "त्वरित क्रियाएं" else "Quick Actions", textColor = MaterialTheme.colorScheme.onBackground)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "स्कैन QR" else "Scan QR",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = { navController.navigate(Routes.SCAN_QR) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "भेजें" else "Send",
                    icon = Icons.Default.Send,
                    onClick = { navController.navigate(Routes.paymentRoute(0, "Receiver")) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "रिचार्ज" else "Recharge",
                    icon = Icons.Default.Smartphone,
                    onClick = { navController.navigate(Routes.RECHARGE) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "इतिहास" else "History",
                    icon = Icons.Default.History,
                    onClick = { navController.navigate(Routes.HISTORY) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "आंकड़े" else "Stats",
                    icon = Icons.Default.BarChart,
                    onClick = { navController.navigate(Routes.STATS) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuickActionChip(
                    title = if (voiceManager.isHindi()) "बैंक लिंक" else "Link Bank",
                    icon = Icons.Default.AddCard,
                    onClick = { navController.navigate(Routes.LINK_BANK) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = if (voiceManager.isHindi()) "वॉयस सुरक्षा" else "Voice Security", textColor = MaterialTheme.colorScheme.onBackground)
            
            PremiumCard(containerColor = MaterialTheme.colorScheme.surface) {
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
                            text = if (voiceManager.isHindi()) "वॉयस पहचान" else "Voice Identity",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (voiceManager.isHindi()) "उच्च-मूल्य लेनदेन के लिए उपयोग किया जाता है।" else "Used for high-value transactions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                            Text(if (voiceManager.isHindi()) "वॉयस प्रिंट रीसेट करें" else "Reset Voice Print", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(title = if (voiceManager.isHindi()) "ऐप अनुभव" else "App Experience", textColor = MaterialTheme.colorScheme.onBackground)
            
            PremiumCard(containerColor = MaterialTheme.colorScheme.surface) {
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
                            text = if (voiceManager.isHindi()) "वॉयस कंट्रोल" else "Voice Controlled",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (voiceManager.isHindi()) "भुगतान या रिचार्ज करने के लिए बस 'हे असिस्टेंट' कहें।" else "Just say 'Hey Assistant' to pay or recharge.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}

@Composable
fun VoiceAssistantSection(
    voiceState: VoiceState,
    voiceManager: VoiceManager,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                )
            )
            .padding(bottom = 24.dp, top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (voiceState) {
                    VoiceState.WAKING -> if (voiceManager.isHindi()) "कहें 'हे असिस्टेंट'" else "Say 'Hey Assistant'"
                    VoiceState.LISTENING -> if (voiceManager.isHindi()) "मैं सुन रहा हूँ..." else "I'm listening..."
                    VoiceState.PROCESSING -> if (voiceManager.isHindi()) "प्रोसेसिंग..." else "Processing..."
                    VoiceState.PROMPTING -> if (voiceManager.isHindi()) "सोच रहा हूँ..." else "Thinking..."
                    VoiceState.RESPONDING -> if (voiceManager.isHindi()) "एक पल..." else "One moment..."
                    VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE -> if (voiceManager.isHindi()) "वॉयस नामांकन..." else "Voice Enrollment..."
                    VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE -> if (voiceManager.isHindi()) "पहचान सत्यापित की जा रही है..." else "Verifying Identity..."
                    VoiceState.UNAUTHORIZED -> if (voiceManager.isHindi()) "पहुंच अस्वीकृत" else "Access Denied"
                    else -> if (voiceManager.isHindi()) "बोलने के लिए टैप करें" else "Tap to speak"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (voiceState) {
                    VoiceState.LISTENING, VoiceState.ENROLLING, VoiceState.ENROLLING_VOICE,
                    VoiceState.AUTHENTICATING, VoiceState.AUTHENTICATING_VOICE -> SecondaryEmerald
                    VoiceState.UNAUTHORIZED -> ErrorRose
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
