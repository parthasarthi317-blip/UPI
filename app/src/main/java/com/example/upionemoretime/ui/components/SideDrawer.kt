package com.example.upionemoretime.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upionemoretime.ui.theme.*

@Composable
fun SideDrawerContent(
    userName: String = "Partha Sarthi",
    upiId: String = "8630511496@ptyes",
    isDarkMode: Boolean = true,
    onDarkModeChange: (Boolean) -> Unit = {},
    isHindi: Boolean = false,
    onLanguageChange: (Boolean) -> Unit = {},
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.85f)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header with Back Arrow and Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "welcome",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF00BAF2),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1.2f))
        }

        // Profile Section
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(2.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00BAF2), modifier = Modifier.size(16.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("UPI ID: $upiId", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00BAF2), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code Card (Simplified Representation)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF00BAF2), Color(0xFF002E6E))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(0.85f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.fillMaxSize(0.8f), tint = Color.Black)
                        }
                    }
                    
                    // Bank Info at bottom of QR card
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF002E6E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("State Bank O... - 4674", fontSize = 10.sp, color = Color.Black)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                
                Text(
                    "Check balance",
                    color = Color(0xFF00BAF2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .clickable { }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add QR to Home", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Section
        Text(
            "Settings",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Appearance with Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Contrast, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isHindi) "दिखावट" else "Appearance", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Text(if (isDarkMode) (if (isHindi) "डार्क मोड" else "Dark Mode") else (if (isHindi) "लाइट मोड" else "Light Mode"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Language with Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isHindi) "भाषा" else "Language", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Text(if (isHindi) "Hindi / हिंदी" else "English", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "EN",
                    modifier = Modifier
                        .clickable { onLanguageChange(false) }
                        .padding(4.dp),
                    color = if (!isHindi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = if (!isHindi) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
                Text("|", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    "हि",
                    modifier = Modifier
                        .clickable { onLanguageChange(true) }
                        .padding(4.dp),
                    color = if (isHindi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = if (isHindi) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }

        DrawerItem(
            icon = Icons.Default.PersonOutline,
            title = if (isHindi) "प्रोफ़ाइल" else "Profile",
            subtitle = if (isHindi) "गोपनीयता, अधिसूचना और भाषा" else "Privacy, Notification & Language"
        )
        DrawerItem(
            icon = Icons.Default.HelpOutline,
            title = if (isHindi) "सहायता और समर्थन" else "Help & Support",
            subtitle = if (isHindi) "ग्राहक सहायता, आपके प्रश्न और अक्सर पूछे जाने वाले प्रश्न" else "Customer Support, Your Queries & FAQs"
        )
        DrawerItem(
            icon = Icons.Default.GroupAdd,
            title = if (isHindi) "रेफर करें और जीतें" else "Refer & Win",
            subtitle = if (isHindi) "₹200 तक जीतें" else "Refer & Win up to ₹200"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Security Shield removed

        Spacer(modifier = Modifier.height(32.dp))

        // Footer Logos
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WELCOME",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF00BAF2),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("TRUSTED BY MILLIONS", fontSize = 10.sp, color = Color.LightGray, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
    }
}
