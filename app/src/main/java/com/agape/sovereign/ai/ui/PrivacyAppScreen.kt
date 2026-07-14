package com.agape.sovereign.ai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import com.agape.sovereign.ai.data.*
import com.agape.sovereign.ai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyAppScreen(
    viewModel: SecureViewModel,
    modifier: Modifier = Modifier
) {
    val biometricLocked by viewModel.biometricLocked.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Crossfade(
            targetState = biometricLocked,
            label = "BiometricTransition"
        ) { locked ->
            if (locked) {
                BiometricLockScreen(viewModel)
            } else {
                MainAppLayout(viewModel, activeTab)
            }
        }
    }
}

@Composable
fun BiometricLockScreen(viewModel: SecureViewModel) {
    val pinAttempt by viewModel.pinAttempt.collectAsStateWithLifecycle()
    val userPin by viewModel.userPin.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Headers
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentMagenta.copy(alpha = 0.15f))
                    .border(2.dp, AccentMagenta, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security Vault Lock",
                    tint = AccentMagenta,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AEGIS PRIVACY",
                color = LightText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text = "Zero-Knowledge Biometric Vault",
                color = AccentBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        // Pad Status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ENTER SEC_CODE UNLOCK",
                color = LightTextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val active = pinAttempt.length > i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (active) AccentMagenta else Color.White.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                if (active) AccentMagenta else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (pinAttempt.isNotEmpty()) {
                Text(
                    text = "Attempting encryption decipher key...",
                    color = AccentOrange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "Bypass simulation PIN is ${userPin}",
                    color = AccentBlue.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Numeric Keypad Styled with orange/blue highlights
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "🔓")
            )

            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (key) {
                                        "C" -> AccentOrange.copy(alpha = 0.15f)
                                        "🔓" -> AccentBlue.copy(alpha = 0.15f)
                                        else -> PanelBg
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (key) {
                                        "C" -> AccentOrange.copy(alpha = 0.4f)
                                        "🔓" -> AccentBlue.copy(alpha = 0.4f)
                                        else -> Color.White.copy(alpha = 0.08f)
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    when (key) {
                                        "C" -> viewModel.clearPinAttempt()
                                        "🔓" -> viewModel.bypassWithMasterPin()
                                        else -> viewModel.enterPinDigit(key)
                                    }
                                }
                                .testTag("digit_button_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = when (key) {
                                    "C" -> AccentOrange
                                    "🔓" -> AccentBlue
                                    else -> LightText
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: SecureViewModel, activeTab: String) {
    val overallSecurityScore by viewModel.overallSecurityScore.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNavigationSuite(
                activeTab = activeTab,
                onTabSelected = { viewModel.activeTab.value = it }
            )
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High Security Top Sync Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOfflineMode) AccentOrange else AccentBlue)
                    )
                    Text(
                        text = if (isOfflineMode) "OFFLINE SECURITY MODE" else "CLOUD HARDENED SYNC",
                        color = LightText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sync icon button
                    IconButton(
                        onClick = { viewModel.triggerCloudSync() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("sync_action_button")
                    ) {
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.Refresh else Icons.Default.Cloud,
                            contentDescription = "Sync Cloud E2EE",
                            tint = if (isSyncing) AccentMagenta else AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Score Circle HUD
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    overallSecurityScore >= 80 -> AccentBlue.copy(alpha = 0.15f)
                                    overallSecurityScore >= 50 -> AccentOrange.copy(alpha = 0.15f)
                                    else -> AccentMagenta.copy(alpha = 0.15f)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    overallSecurityScore >= 80 -> AccentBlue
                                    overallSecurityScore >= 50 -> AccentOrange
                                    else -> AccentMagenta
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${overallSecurityScore}% SECURE",
                            color = LightText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Lock Button
                    IconButton(
                        onClick = { viewModel.instantLockApp() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentMagenta.copy(alpha = 0.15f))
                            .testTag("lock_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Immediate Application Seal",
                            tint = AccentMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Core Navigation Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "DASHBOARD" -> DashboardScreen(viewModel)
                    "PASSWORDS" -> PasswordScreen(viewModel)
                    "VAULT" -> VaultScreen(viewModel)
                    "MESSAGES" -> MessagesScreen(viewModel)
                    "IDENTITY" -> IdentityScreen(viewModel)
                    "TRACKERS" -> TrackerScreen(viewModel)
                    "AUDIT" -> AuditScreen(viewModel)
                    "ARCHITECT" -> ArchitectMcpScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationSuite(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        TabItem("DASHBOARD", Icons.Default.Shield, "Defend"),
        TabItem("PASSWORDS", Icons.Default.VpnKey, "Logins"),
        TabItem("VAULT", Icons.Default.Lock, "Vault"),
        TabItem("MESSAGES", Icons.AutoMirrored.Filled.Send, "Chat"),
        TabItem("IDENTITY", Icons.Default.Face, "Identity"),
        TabItem("TRACKERS", Icons.Default.Warning, "Blocks"),
        TabItem("AUDIT", Icons.Default.Check, "Audit"),
        TabItem("ARCHITECT", Icons.Default.Psychology, "AI")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)))
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { tab ->
            val isSelected = activeTab == tab.id
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab.id) }
                    .padding(vertical = 12.dp)
                    .testTag("nav_tab_${tab.id.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (isSelected) {
                        when (tab.id) {
                            "DASHBOARD", "AUDIT" -> AccentBlue
                            "PASSWORDS", "IDENTITY" -> AccentMagenta
                            "ARCHITECT" -> AccentMagenta
                            else -> AccentOrange
                        }
                    } else LightTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.label,
                    color = if (isSelected) LightText else LightTextMuted,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class TabItem(val id: String, val icon: ImageVector, val label: String)

fun getDashboardModuleIcon(id: Int): ImageVector {
    return when (id) {
        1 -> Icons.Default.Person
        2 -> Icons.Default.Business
        3 -> Icons.Default.AccountBalance
        4 -> Icons.Default.Share
        5 -> Icons.Default.Favorite
        6 -> Icons.Default.Build
        7 -> Icons.Default.Home
        8 -> Icons.Default.VpnLock
        9 -> Icons.AutoMirrored.Filled.Chat
        10 -> Icons.Default.Key
        11 -> Icons.Default.Search
        12 -> Icons.Default.Group
        13 -> Icons.Default.Security
        14 -> Icons.Default.Update
        15 -> Icons.Default.Language
        else -> Icons.Default.Block
    }
}

@Composable
fun DashboardIdentityGridItem(
    module: IdentityModule,
    viewModel: SecureViewModel,
    modifier: Modifier = Modifier
) {
    val isActive = module.status == "ACTIVE"
    
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PanelBg)
            .border(
                BorderStroke(
                    1.dp,
                    if (isActive) AccentBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable {
                val nextStatus = if (isActive) "DISABLED" else "ACTIVE"
                viewModel.updateIdentityModule(module.copy(status = nextStatus))
            }
            .padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AccentBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getDashboardModuleIcon(module.id),
                        contentDescription = null,
                        tint = if (isActive) AccentBlue else LightTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NODE ${if (module.id < 10) "0${module.id}" else "${module.id}"}",
                        color = if (isActive) AccentOrange else LightTextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = module.name.replace(" Identity", "").replace(" Profile", "").replace(" Persona", ""),
                        color = LightText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Switch(
                checked = isActive,
                onCheckedChange = { isChecked ->
                    val nextStatus = if (isChecked) "ACTIVE" else "DISABLED"
                    viewModel.updateIdentityModule(module.copy(status = nextStatus))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentBlue,
                    uncheckedThumbColor = LightTextMuted.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.05f),
                    uncheckedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .scale(0.7f)
                    .testTag("dashboard_module_toggle_${module.id}")
            )
        }
    }
}

fun generateSecurePassword(
    length: Int,
    upper: Boolean,
    lower: Boolean,
    digits: Boolean,
    symbols: Boolean
): String {
    val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    val digitChars = "0123456789"
    val symbolChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
    
    val allowedChars = java.lang.StringBuilder()
    if (upper) allowedChars.append(upperChars)
    if (lower) allowedChars.append(lowerChars)
    if (digits) allowedChars.append(digitChars)
    if (symbols) allowedChars.append(symbolChars)
    
    if (allowedChars.isEmpty()) return ""
    
    val random = java.security.SecureRandom()
    val password = java.lang.StringBuilder()
    
    val guaranteed = mutableListOf<Char>()
    if (upper) guaranteed.add(upperChars[random.nextInt(upperChars.length)])
    if (lower) guaranteed.add(lowerChars[random.nextInt(lowerChars.length)])
    if (digits) guaranteed.add(digitChars[random.nextInt(digitChars.length)])
    if (symbols) guaranteed.add(symbolChars[random.nextInt(symbolChars.length)])
    
    val fillLength = (length - guaranteed.size).coerceAtLeast(0)
    for (i in 0 until fillLength) {
        val nextIdx = random.nextInt(allowedChars.length)
        password.append(allowedChars[nextIdx])
    }
    
    guaranteed.forEach { char ->
        val insertPos = if (password.length > 0) random.nextInt(password.length + 1) else 0
        password.insert(insertPos, char)
    }
    
    return password.toString().take(length)
}

fun evaluatePasswordStrength(password: String): String {
    val len = password.length
    if (len == 0) return "EMPTY"
    
    var score = 0
    if (len >= 8) score++
    if (len >= 14) score++
    if (len >= 20) score++
    
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    if (hasUpper) score++
    if (hasLower) score++
    if (hasDigit) score++
    if (hasSpecial) score++
    
    return when {
        score <= 3 -> "WEAK"
        score <= 5 -> "MEDIUM"
        score <= 6 -> "STRONG"
        else -> "MILITARY-GRADE"
    }
}

@Composable
fun CredentialToggleChip(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) AccentOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = if (checked) AccentOrange else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (checked) LightText else LightTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SecureCredentialGeneratorCard(viewModel: SecureViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var passwordLength by remember { mutableStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeDigits by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var clipboardFeedback by remember { mutableStateOf(false) }

    val anyChecked = includeUpper || includeLower || includeDigits || includeSymbols

    // Initial password generation & response to requirements
    LaunchedEffect(passwordLength, includeUpper, includeLower, includeDigits, includeSymbols) {
        generatedPassword = if (anyChecked) {
            generateSecurePassword(
                length = passwordLength.toInt(),
                upper = includeUpper,
                lower = includeLower,
                digits = includeDigits,
                symbols = includeSymbols
            )
        } else {
            ""
        }
        clipboardFeedback = false
    }

    val strength = evaluatePasswordStrength(generatedPassword)
    val strengthColor = when (strength) {
        "WEAK" -> AccentMagenta
        "MEDIUM" -> AccentOrange
        "STRONG" -> AccentBlue
        else -> Color(0xFF00FFC2) // Premium cyan-green for military-grade
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .testTag("secure_credential_generator_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Key Icon",
                    tint = AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "SECURE CREDENTIAL GENERATOR",
                    color = LightText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Strength Badge
            if (generatedPassword.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(strengthColor.copy(alpha = 0.15f))
                        .border(0.5.dp, strengthColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = strength,
                        color = strengthColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Display Generated Password
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = generatedPassword.ifEmpty { "Select at least one requirement" },
                    color = if (generatedPassword.isEmpty()) LightTextMuted else LightText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generator_display_text"),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regenerate Button
                    IconButton(
                        onClick = {
                            if (anyChecked) {
                                generatedPassword = generateSecurePassword(
                                    length = passwordLength.toInt(),
                                    upper = includeUpper,
                                    lower = includeLower,
                                    digits = includeDigits,
                                    symbols = includeSymbols
                                )
                                clipboardFeedback = false
                            }
                        },
                        enabled = anyChecked,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("generator_regenerate_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = if (anyChecked) LightText else LightTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            if (generatedPassword.isNotEmpty()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Secure Password", generatedPassword)
                                clipboard.setPrimaryClip(clip)
                                clipboardFeedback = true
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = generatedPassword.isNotEmpty(),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("generator_copy_btn")
                    ) {
                        Icon(
                            imageVector = if (clipboardFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = if (clipboardFeedback) Color(0xFF00FFC2) else LightText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Length Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Length Requirement",
                    color = LightTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (passwordLength > 8f) {
                                passwordLength--
                            }
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("generator_len_minus")
                    ) {
                        Text(
                            text = "-",
                            color = LightText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${passwordLength.toInt()} Chars",
                        color = LightText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = {
                            if (passwordLength < 64f) {
                                passwordLength++
                            }
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("generator_len_plus")
                    ) {
                        Text(
                            text = "+",
                            color = LightText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Slider(
                value = passwordLength,
                onValueChange = {
                    passwordLength = it
                },
                valueRange = 8f..64f,
                colors = SliderDefaults.colors(
                    thumbColor = AccentOrange,
                    activeTrackColor = AccentOrange,
                    inactiveTrackColor = Color.White.copy(alpha = 0.06f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generator_length_slider")
            )
        }

        // Character Requirements Toggles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Character Profiles Allowed",
                color = LightTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CredentialToggleChip(
                    label = "A-Z",
                    checked = includeUpper,
                    onCheckedChange = { includeUpper = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generator_checkbox_upper")
                )
                CredentialToggleChip(
                    label = "a-z",
                    checked = includeLower,
                    onCheckedChange = { includeLower = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generator_checkbox_lower")
                )
                CredentialToggleChip(
                    label = "0-9",
                    checked = includeDigits,
                    onCheckedChange = { includeDigits = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generator_checkbox_digits")
                )
                CredentialToggleChip(
                    label = "!@#$",
                    checked = includeSymbols,
                    onCheckedChange = { includeSymbols = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generator_checkbox_symbols")
                )
            }
        }

        // Save Button
        Button(
            onClick = { showSaveDialog = true },
            enabled = generatedPassword.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generator_add_vault_btn"),
            contentPadding = PaddingValues(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = if (generatedPassword.isNotEmpty()) Color.White else LightTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "HARDEN LOCKER: SAVE CREDENTIAL",
                    color = if (generatedPassword.isNotEmpty()) Color.White else LightTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // Save Password Dialog
    if (showSaveDialog) {
        var titleInput by remember { mutableStateOf("") }
        var userInput by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Work") }
        val categories = listOf("Finance", "Social", "Email", "Work", "Other")

        Dialog(onDismissRequest = { showSaveDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .testTag("generator_save_dialog_container"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PanelBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SECURE VAULT INTEGRATION",
                        color = AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "Securely persist the generated passcode in your local database locker.",
                        color = LightTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    // Title Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Account Label / Title", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            placeholder = { Text("e.g. ProtonMail Secondary", color = LightTextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generator_save_title_input")
                        )
                    }

                    // Username Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Username / Email", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = { Text("e.g. security_guard@proton.me", color = LightTextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generator_save_user_input")
                        )
                    }

                    // Password Preview
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Generated Passcode", color = LightTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = generatedPassword,
                                color = AccentBlue,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Category Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Security Category", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val active = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) AccentOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                        .border(
                                            0.5.dp,
                                            if (active) AccentOrange else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (active) LightText else LightTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showSaveDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", color = LightText)
                        }

                        Button(
                            onClick = {
                                viewModel.addPassword(
                                    title = titleInput.ifEmpty { "Generated Login" },
                                    username = userInput.ifEmpty { "anonymous" },
                                    passText = generatedPassword,
                                    category = selectedCategory
                                )
                                showSaveDialog = false
                                android.widget.Toast.makeText(context, "Password entry secured in database!", android.widget.Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("generator_save_confirm_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Secure Record", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: SecureViewModel) {
    val isTrackingShieldOn by viewModel.isTrackingShieldOn.collectAsStateWithLifecycle()
    val totalTrackersBlocked by viewModel.totalTrackersBlocked.collectAsStateWithLifecycle()
    val trackingLogs by viewModel.trackingLogs.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    val passwordCount = passwords.size
    val vaultCount = vaultItems.size
    val identityModules by viewModel.identityModules.collectAsStateWithLifecycle()
    val activeIdentityCount = identityModules.count { it.status == "ACTIVE" }
    var moduleFilter by remember { mutableStateOf("ALL") }

    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var backupPasscode by remember { mutableStateOf("4209") }
    var generatedBackupPayload by remember { mutableStateOf("") }

    var importPasscode by remember { mutableStateOf("4209") }
    var importedJsonTextInput by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }
    var importResultType by remember { mutableStateOf<SecureViewModel.ImportResult?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonText = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (jsonText != null) {
                    importedJsonTextInput = jsonText
                    importStatusMessage = "Backup payload successfully loaded from device storage."
                    importResultType = null
                }
            } catch (e: Exception) {
                importStatusMessage = "Failed to parse loaded file content."
                importResultType = SecureViewModel.ImportResult.INVALID_JSON
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Dashboard Header Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AccentMagenta.copy(alpha = 0.25f),
                                AccentBlue.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURE PROTOCOL ACTIVE",
                            color = AccentMagenta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        
                        // Real-time blocked badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentMagenta.copy(alpha = 0.25f))
                                .border(1.dp, AccentMagenta.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isTrackingShieldOn) AccentBlue else LightTextMuted)
                                )
                                Text(
                                    text = "$totalTrackersBlocked BLOCKED",
                                    color = LightText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("header_blocked_count")
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aegis Zero-Trust Shield",
                        color = LightText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your digital footprint is encrypted locally using hardware backed keys. Synchronizations occur in point-to-point zero-knowledge streams.",
                        color = LightTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            // Quick Action Row (Tracking Shield Toggle and Offline Mode Toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tracking Shield Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isTrackingShieldOn) AccentBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                            ), RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.isTrackingShieldOn.value = !isTrackingShieldOn }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Tracking Shield Toggle",
                            tint = if (isTrackingShieldOn) AccentBlue else LightTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Switch(
                            checked = isTrackingShieldOn,
                            onCheckedChange = { viewModel.isTrackingShieldOn.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = LightTextMuted,
                                uncheckedTrackColor = ObsidianBg
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Anti-Tracking Shield",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTrackingShieldOn) "REAL-TIME PROTECTION ON" else "SHIELD DISARMED",
                        color = if (isTrackingShieldOn) AccentBlue else AccentOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Privacy Offline Mode Block
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isOfflineMode) AccentOrange.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                            ), RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.isOfflineMode.value = !isOfflineMode }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode Toggle",
                            tint = if (isOfflineMode) AccentOrange else LightTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Switch(
                            checked = isOfflineMode,
                            onCheckedChange = { viewModel.isOfflineMode.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentOrange,
                                uncheckedThumbColor = LightTextMuted,
                                uncheckedTrackColor = ObsidianBg
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Airgapped Offline Mode",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOfflineMode) "LOCAL ISOLATION ACTIVE" else "CLOUD HANDSHAKES LIVE",
                        color = if (isOfflineMode) AccentOrange else AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        item {
            // Stats Grid Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Passwords Info Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "Vault Entries", color = LightTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "$vaultCount Secure Notes", color = AccentMagenta, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Passwords locker stats
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "Hardened Keys", color = LightTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "$passwordCount Logins Secured", color = AccentBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            // Modular Shield Command Center Title and Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title and details button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MODULAR SHIELD COMMAND CENTER",
                            color = AccentOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$activeIdentityCount / 16 Nodes Armed",
                            color = LightText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Detail navigation button
                    Button(
                        onClick = { viewModel.activeTab.value = "IDENTITY" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("go_to_identity_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Full Settings", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = LightText, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                
                // Quick actions and filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ALL, ACTIVE, INACTIVE chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL", "ACTIVE", "INACTIVE").forEach { filter ->
                            val isSelected = moduleFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                    .border(
                                        0.5.dp,
                                        if (isSelected) AccentOrange else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { moduleFilter = filter }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) LightText else LightTextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Master toggles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Arm All
                        TextButton(
                            onClick = { viewModel.setAllIdentityModulesStatus("ACTIVE") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ARM ALL", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        
                        // Disarm All
                        TextButton(
                            onClick = { viewModel.setAllIdentityModulesStatus("DISABLED") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("DISARM ALL", color = AccentMagenta, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                
                // Grid render of 16 modules (chunked in rows of 2)
                val filteredModules = when (moduleFilter) {
                    "ACTIVE" -> identityModules.filter { it.status == "ACTIVE" }
                    "INACTIVE" -> identityModules.filter { it.status != "ACTIVE" }
                    else -> identityModules
                }
                
                if (filteredModules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No modules match the filter",
                            color = LightTextMuted,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                } else {
                    val rows = filteredModules.chunked(2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { mod ->
                                    DashboardIdentityGridItem(
                                        module = mod,
                                        viewModel = viewModel,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SecureCredentialGeneratorCard(viewModel)
        }

        item {
            // Cryptographic Backup & Recovery Vault Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("backup_recovery_card_container")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Cryptographic Backups Icon",
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CRYPTOGRAPHIC BACKUPS",
                            color = LightText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("AES-256", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Encrypted Persona Settings & Locker Backup",
                    color = LightText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Export or restore comprehensive zero-knowledge packages containing all 16 identity modules, stored passwords, and vault items safely. Every export is strictly encrypted client-side using symmetric SHA-256 key generation.",
                    color = LightTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export Button
                    Button(
                        onClick = {
                            generatedBackupPayload = ""
                            showExportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_backup_btn_trigger"),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Export backup",
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backup", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Import Button
                    Button(
                        onClick = {
                            importedJsonTextInput = ""
                            importStatusMessage = ""
                            importResultType = null
                            showImportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_backup_btn_trigger"),
                        border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Import backup",
                            tint = AccentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Backup", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // Dynamic Anti-Tracking Feed Sneakpeek Custom Widget
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBg)
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REAL-TIME TRACKERS BLOCKED",
                            color = LightTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "$totalTrackersBlocked ",
                                color = AccentMagenta,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "blocked attempts today",
                                color = LightTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.activeTab.value = "TRACKERS" },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Live Monitor", color = Color.White, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(12.dp))

                if (trackingLogs.isEmpty()) {
                    Text(
                        text = "Awaiting analytical tracker traffic triggers...",
                        color = LightTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val logsToShow = trackingLogs.take(2)
                    for (log in logsToShow) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(
                                    text = log.company,
                                    color = LightText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.url,
                                    color = LightTextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentOrange.copy(alpha = 0.15f))
                                    .border(0.5.dp, AccentOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("BLOCKED", color = AccentOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog Overlay
    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp)),
                color = PanelBg
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "EXPORT ENCRYPTED VAULT",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Setup Encryption Passkey",
                        color = LightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your secret parameters are completely encrypted using AES-256. Provide a secure passcode or use your standard security bypass PIN (defaulting to 4209).",
                        color = LightTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    // Text Field for passcode
                    OutlinedTextField(
                        value = backupPasscode,
                        onValueChange = { backupPasscode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_passcode_field"),
                        label = { Text("Passcode / Master PIN", color = LightTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        singleLine = true
                    )

                    if (generatedBackupPayload.isEmpty()) {
                        Button(
                            onClick = {
                                generatedBackupPayload = viewModel.exportBackupAsJsonString(backupPasscode.ifEmpty { "4209" })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_backup_execute_btn")
                        ) {
                            Text("Generate Cryptographic Payload", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Display generated JSON backup encrypted block
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Secure Payload Created", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${generatedBackupPayload.length} char block", color = LightTextMuted, fontSize = 10.sp)
                            }

                            // Read-only block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBg)
                                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = generatedBackupPayload,
                                    color = LightText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Copy Button
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Aegis Secure Backup", generatedBackupPayload)
                                        clipboard.setPrimaryClip(clip)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("copy_backup_btn")
                                ) {
                                    Text("Copy JSON", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Share Button
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, "Aegis Privacy Secure Backup")
                                            putExtra(Intent.EXTRA_TEXT, generatedBackupPayload)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Save or Send Backup"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_backup_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share File", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showExportDialog = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Close Portal", color = LightTextMuted)
                    }
                }
            }
        }
    }

    // Import Dialog Overlay
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp)),
                color = PanelBg
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "RESTORE VAULT PORTAL",
                        color = AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Decrypt Backup Configuration",
                        color = LightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Pick Backup File option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg)
                            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                            .clickable { filePickerLauncher.launch("application/json") }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Select backup file",
                                tint = AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("Select JSON Backup File", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Browse downloaded files", color = LightTextMuted, fontSize = 10.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "browse files",
                            tint = LightTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Or paste encrypted payload below:",
                        color = LightTextMuted,
                        fontSize = 11.sp
                    )

                    // Text Field for payload input
                    OutlinedTextField(
                        value = importedJsonTextInput,
                        onValueChange = { importedJsonTextInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("import_payload_field"),
                        placeholder = { Text("Paste JSON container starting with backup_version...", color = LightTextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )

                    // Passcode input of decryption key
                    OutlinedTextField(
                        value = importPasscode,
                        onValueChange = { importPasscode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_passcode_field"),
                        label = { Text("Decryption Passcode / PIN", color = LightTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        singleLine = true
                    )

                    if (importStatusMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (importResultType == SecureViewModel.ImportResult.SUCCESS) AccentBlue.copy(alpha = 0.15f)
                                    else AccentMagenta.copy(alpha = 0.15f)
                                )
                                .border(
                                    0.5.dp,
                                    if (importResultType == SecureViewModel.ImportResult.SUCCESS) AccentBlue
                                    else AccentMagenta,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = importStatusMessage,
                                color = LightText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Execute Button
                    Button(
                        onClick = {
                            if (importedJsonTextInput.trim().isEmpty()) {
                                importStatusMessage = "Please paste a backup JSON payload or load a file from device."
                                importResultType = SecureViewModel.ImportResult.EMPTY_BACKUP
                                return@Button
                            }

                            val result = viewModel.importBackupFromJsonString(importedJsonTextInput, importPasscode.ifEmpty { "4209" })
                            importResultType = result
                            when (result) {
                                SecureViewModel.ImportResult.SUCCESS -> {
                                    importStatusMessage = "RESTORE SECURED FULLY! Authenticated successfully, all user configurations decrypted and restored to local Room container."
                                }
                                SecureViewModel.ImportResult.DECRYPTION_FAILED -> {
                                    importStatusMessage = "DECRYPTION FAILED. Verification passcode incorrect, please check key signature or try again."
                                }
                                SecureViewModel.ImportResult.INVALID_JSON -> {
                                    importStatusMessage = "INVALID CONTAINER FORMAT. The loaded payload does not match secure Aegis backup syntax."
                                }
                                else -> {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execute_restore_backup_btn")
                    ) {
                        Text("Verify & Run Point-to-Point Restore", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showImportDialog = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Discard Portal", color = LightTextMuted)
                    }
                }
            }
        }
    }
}

// ---------------- PASSWORDS TAB -----------------
@Composable
fun PasswordScreen(viewModel: SecureViewModel) {
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var rawPassword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }

    val categories = listOf("Work", "Social", "Finance", "Email", "Other")

    val context = LocalContext.current
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var csvExportPasscode by remember { mutableStateOf("") }
    var generatedCsvPayload by remember { mutableStateOf("") }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(generatedCsvPayload.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                android.widget.Toast.makeText(context, "Encrypted CSV saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                showCsvExportDialog = false
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error saving file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("password_screen_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PASSWORD VAULT GENERATOR",
                    color = AccentMagenta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Secured Digital Credentials",
                    color = LightText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export CSV Button
                Button(
                    onClick = {
                        generatedCsvPayload = ""
                        showCsvExportDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_csv_btn_trigger"),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMagenta),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_password_btn_trigger"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Login", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lock Cred", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (passwords.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Keys Empty",
                        tint = LightTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Encrypted Vault is Empty",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate and secure high entropy master keys now.",
                        color = LightTextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(passwords, key = { it.id }) { entry ->
                    PasswordRow(entry = entry, onDelete = { viewModel.deletePassword(entry.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = PanelBg,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SECURE CREDENTIAL LOCK",
                        color = AccentMagenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Service Title (e.g. Chase Bank)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_title_input")
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Identity handle") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_username_input")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = rawPassword,
                            onValueChange = { rawPassword = it },
                            label = { Text("Bespoke Password Node") },
                            colors = OutlinedTextFieldDefaults.colors(
                                  focusedTextColor = LightText,
                                  unfocusedTextColor = LightText,
                                  focusedBorderColor = AccentBlue,
                                  unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_password_input")
                        )

                        // Password generator button colored with our Orange/Blue action style
                        Button(
                            onClick = {
                                val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
                                val rand = java.util.Random()
                                val sb = StringBuilder()
                                for (i in 0 until 14) {
                                    sb.append(alphabet[rand.nextInt(alphabet.length)])
                                }
                                rawPassword = sb.toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, AccentOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Generate High Entropy Key", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Category Selections Flow
                    Text("Secure Category", color = LightTextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val selected = category == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) AccentBlue else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (selected) AccentBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat, color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = LightTextMuted)
                        }
                        Button(
                            onClick = {
                                viewModel.addPassword(title, username, rawPassword, category)
                                title = ""
                                username = ""
                                rawPassword = ""
                                category = "Work"
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f).testTag("save_password_btn")
                        ) {
                            Text("Seal Key", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showCsvExportDialog) {
        Dialog(onDismissRequest = { showCsvExportDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp)),
                color = PanelBg
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "EXPORT ENCRYPTED PASSWORDS",
                        color = AccentMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Setup Encryption Passkey",
                        color = LightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your password entries will be compiled into a standard CSV, then fully encrypted using AES-256. Provide a secure passcode or use your standard bypass PIN (defaulting to 4209).",
                        color = LightTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    // Text Field for passcode
                    OutlinedTextField(
                        value = csvExportPasscode,
                        onValueChange = { csvExportPasscode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("csv_export_passcode_field"),
                        label = { Text("Passcode / Master PIN", color = LightTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentMagenta,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        singleLine = true
                    )

                    if (generatedCsvPayload.isEmpty()) {
                        Button(
                            onClick = {
                                generatedCsvPayload = viewModel.generateEncryptedCsv(csvExportPasscode.ifEmpty { "4209" })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_csv_execute_btn")
                        ) {
                            Text("Generate Encrypted CSV", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Display generated CSV encrypted block
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Secure Payload Created", color = AccentMagenta, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${generatedCsvPayload.length} characters", color = LightTextMuted, fontSize = 10.sp)
                            }

                            // Read-only block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBg)
                                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = generatedCsvPayload,
                                    color = LightText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Download File Button
                                Button(
                                    onClick = {
                                        createCsvLauncher.launch("aegis_passwords_encrypted.csv")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("download_csv_file_btn")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download CSV", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Share Button
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Aegis Encrypted CSV Passwords")
                                            putExtra(Intent.EXTRA_TEXT, generatedCsvPayload)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Save or Send Encrypted CSV"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_csv_file_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = LightText, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showCsvExportDialog = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Close Portal", color = LightTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordRow(entry: PasswordEntry, onDelete: () -> Unit) {
    var rawVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(0.7f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = entry.title,
                    color = LightText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (entry.strength) {
                                "STRONG" -> AccentBlue.copy(alpha = 0.15f)
                                "MEDIUM" -> AccentOrange.copy(alpha = 0.15f)
                                else -> AccentMagenta.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = entry.strength,
                        color = when (entry.strength) {
                            "STRONG" -> AccentBlue
                            "MEDIUM" -> AccentOrange
                            else -> AccentMagenta
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${entry.username}",
                color = LightTextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (rawVisible) entry.passwordDecrypted else "• • • • • • • •",
                color = if (rawVisible) AccentBlue else LightTextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { rawVisible = !rawVisible },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (rawVisible) Icons.Default.Close else Icons.Default.Face,
                    contentDescription = "Toggle Visibility",
                    tint = LightTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Purge Password",
                    tint = AccentMagenta,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---------------- VAULT TAB -----------------
data class UnencryptedBackupFile(
    val id: String,
    val name: String,
    val type: String, // "DOCUMENT", "SECURE_NOTE", "SECURE_PHOTO"
    val content: String,
    val rawSize: String
)

@Composable
fun VaultScreen(viewModel: SecureViewModel) {
    val vaultItems by viewModel.vaultItems.collectAsStateWithLifecycle()
    val showEncryptedToggles by viewModel.showEncryptedToggles.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    var docTitle by remember { mutableStateOf("") }
    var docContent by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("SECURE_NOTE") }
    var selectedCipher by remember { mutableStateOf("AES-256") }

    // Folder states
    val folders = listOf("All", "Personal", "Credentials", "Financial", "Archived")
    var selectedFolder by remember { mutableStateOf("All") }

    // High fidelity temp buffer of unencrypted files that can be dragged & dropped to encrypt
    var stagedFiles by remember {
        mutableStateOf(
            listOf(
                UnencryptedBackupFile("temp_1", "API_Keys_Export.txt", "SECURE_NOTE", "PROD_SECRET=sk_live_9a8f6d2b4c81a2eef980bb, REGION=us-east-1", "124 B"),
                UnencryptedBackupFile("temp_2", "Tax_Receipt_2025.pdf", "DOCUMENT", "Client Name: IDIN. Gross Income: $148,200. Paid: $32,100", "42 KB"),
                UnencryptedBackupFile("temp_3", "HW_Wallet_Backup.phrase", "SECURE_PHOTO", "ocean scale normal simple logic pattern general high rapid dynamic clock track", "11 words")
            )
        )
    }

    // Drag-and-drop tracking states
    var isDraggingActive by remember { mutableStateOf(false) }
    var draggingTitle by remember { mutableStateOf("") }
    var draggingType by remember { mutableStateOf("") }
    var draggingCurrentTouch by remember { mutableStateOf(Offset.Zero) }

    var draggingTempItem by remember { mutableStateOf<UnencryptedBackupFile?>(null) }
    var draggingVaultItem by remember { mutableStateOf<VaultItem?>(null) }
    var activeHoveredFolder by remember { mutableStateOf<String?>(null) }
    var activeHoveredDropzone by remember { mutableStateOf(false) }

    // Coordinate maps for physics-accurate overlapping calculations
    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val dropzoneBounds = remember { mutableStateOf<Rect?>(null) }
    val tempItemBounds = remember { mutableStateMapOf<String, Rect>() }
    val vaultItemBounds = remember { mutableStateMapOf<Int, Rect>() }

    // Interactive Toast/Success State Feedback
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackColor by remember { mutableStateOf(AccentBlue) }

    // Clear feedback message automatically after delay
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage.isNotEmpty()) {
            kotlinx.coroutines.delay(4000)
            feedbackMessage = ""
        }
    }

    // Filtered items based on folder selection
    val filteredItems = remember(vaultItems, selectedFolder) {
        if (selectedFolder == "All") {
            vaultItems
        } else {
            vaultItems.filter { it.folder.equals(selectedFolder, ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vault_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentBlue)
                        )
                        Text(
                            text = "LOCAL CRYPTOGRAPHIC VAULT",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Encrypted Document Shield",
                        color = LightText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_vault_btn_trigger")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Safe Log", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lock Doc", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback Status Toast overlay
            AnimatedVisibility(
                visible = feedbackMessage.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(feedbackColor.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, feedbackColor.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Active",
                        tint = feedbackColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = feedbackMessage,
                        color = LightText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Folder organization Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "VAULT FOLDERS (DRAG FILES HERE TO ORGANIZE)",
                    color = LightTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    folders.forEach { fName ->
                        val isSelected = selectedFolder == fName
                        val matchingCount = if (fName == "All") vaultItems.size else vaultItems.count { it.folder.equals(fName, ignoreCase = true) }
                        val isHovered = activeHoveredFolder == fName

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isHovered) AccentOrange.copy(alpha = 0.25f)
                                    else if (isSelected) AccentBlue.copy(alpha = 0.15f)
                                    else PanelBg
                                )
                                .border(
                                    BorderStroke(
                                        width = if (isHovered) 2.dp else 1.dp,
                                        color = if (isHovered) AccentOrange
                                        else if (isSelected) AccentBlue
                                        else Color.White.copy(alpha = 0.05f)
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFolder = fName }
                                .onGloballyPositioned { layoutCoordinates ->
                                    if (layoutCoordinates.isAttached) {
                                        folderBounds[fName] = layoutCoordinates.boundsInRoot()
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (fName == "All") Icons.Default.AllInclusive else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isHovered) AccentOrange else if (isSelected) AccentBlue else LightTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = fName.uppercase(),
                                    color = if (isHovered) AccentOrange else if (isSelected) AccentBlue else LightText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "($matchingCount items)",
                                    color = LightTextMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Drag-and-Drop Area Upload Dropzone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (activeHoveredDropzone) AccentBlue.copy(alpha = 0.25f)
                        else PanelBg
                    )
                    .border(
                        BorderStroke(
                            width = if (activeHoveredDropzone) 2.dp else 1.dp,
                            color = if (activeHoveredDropzone) AccentBlue else Color.White.copy(alpha = 0.08f)
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .onGloballyPositioned { layoutCoordinates ->
                        if (layoutCoordinates.isAttached) {
                            dropzoneBounds.value = layoutCoordinates.boundsInRoot()
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle background dash effect simulation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Drop files here",
                        tint = if (activeHoveredDropzone) AccentBlue else AccentOrange,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DRAG STAMPED FILE HERE TO AUTO-ENCRYPT",
                        color = LightText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Drop unencrypted local items below onto this zone to compile as a 256-bit secure record inside: ${selectedFolder.uppercase()}",
                        color = LightTextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unencrypted Local Buffer Staging list (Draggable items drawer)
            if (stagedFiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "📂 UNENCRYPTED SOURCE FILES (DRAG HANDLES TO ENCRYPT)",
                        color = LightTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stagedFiles.forEach { tempFile ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianBg)
                                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(10.dp))
                                    .onGloballyPositioned { layoutCoordinates ->
                                        if (layoutCoordinates.isAttached) {
                                            tempItemBounds[tempFile.id] = layoutCoordinates.boundsInRoot()
                                        }
                                    }
                                    .pointerInput(tempFile) {
                                        detectDragGestures(
                                            onDragStart = { startOffset ->
                                                val rect = tempItemBounds[tempFile.id] ?: Rect.Zero
                                                isDraggingActive = true
                                                draggingTitle = tempFile.name
                                                draggingType = tempFile.type
                                                draggingTempItem = tempFile
                                                draggingVaultItem = null
                                                draggingCurrentTouch = rect.topLeft + startOffset
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                draggingCurrentTouch += dragAmount

                                                val currentTouchPoint = draggingCurrentTouch

                                                // Update folder hovering bounds
                                                activeHoveredFolder = null
                                                for ((fName, fRect) in folderBounds) {
                                                    if (fRect.contains(currentTouchPoint)) {
                                                        activeHoveredFolder = fName
                                                        break
                                                    }
                                                }

                                                // Update dropzone bounds
                                                activeHoveredDropzone = dropzoneBounds.value?.contains(currentTouchPoint) ?: false
                                            },
                                            onDragEnd = {
                                                if (activeHoveredFolder != null) {
                                                    val destFolder = activeHoveredFolder!!
                                                    val folderTarget = if (destFolder == "All") "Personal" else destFolder
                                                    viewModel.addVaultItem(tempFile.name, tempFile.type, tempFile.content, folderTarget)
                                                    feedbackMessage = "Locked and encrypted '${tempFile.name}' directly into '$folderTarget'!"
                                                    feedbackColor = AccentOrange
                                                    stagedFiles = stagedFiles.filterNot { it.id == tempFile.id }
                                                } else if (activeHoveredDropzone) {
                                                    val folderTarget = if (selectedFolder == "All") "Personal" else selectedFolder
                                                    viewModel.addVaultItem(tempFile.name, tempFile.type, tempFile.content, folderTarget)
                                                    feedbackMessage = "Encrypted '${tempFile.name}' inside zero-knowledge shield!"
                                                    feedbackColor = AccentBlue
                                                    stagedFiles = stagedFiles.filterNot { it.id == tempFile.id }
                                                }
                                                isDraggingActive = false
                                                activeHoveredFolder = null
                                                activeHoveredDropzone = false
                                                draggingTempItem = null
                                            },
                                            onDragCancel = {
                                                isDraggingActive = false
                                                activeHoveredFolder = null
                                                activeHoveredDropzone = false
                                                draggingTempItem = null
                                            }
                                        )
                                    }
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AccentMagenta.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "RAW",
                                                color = AccentMagenta,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag Handle",
                                            tint = LightTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = tempFile.name,
                                        color = LightText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tempFile.type.replace("_", " "),
                                            color = LightTextMuted,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = tempFile.rawSize,
                                            color = LightTextMuted,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Existing encrypted elements list inside the active folder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VAULT SECURED SHIELDS (${filteredItems.size})",
                    color = LightTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "FOLDER: ${selectedFolder.uppercase()}",
                    color = AccentBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault Empty",
                            tint = LightTextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Encrypted Items in $selectedFolder",
                            color = LightText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add manually or drag-and-drop raw local files onto folders to populate.",
                            color = LightTextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val showEncrypted = showEncryptedToggles[item.id] ?: false

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PanelBg)
                                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                                .onGloballyPositioned { layoutCoordinates ->
                                    if (layoutCoordinates.isAttached) {
                                        vaultItemBounds[item.id] = layoutCoordinates.boundsInRoot()
                                    }
                                }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Make drag interaction handles to reorganize records between folders!
                                    Icon(
                                        imageVector = Icons.Default.DragIndicator,
                                        contentDescription = "drag descriptor",
                                        tint = LightTextMuted,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .pointerInput(item) {
                                                detectDragGestures(
                                                    onDragStart = { startOffset ->
                                                        val rect = vaultItemBounds[item.id] ?: Rect.Zero
                                                        isDraggingActive = true
                                                        draggingTitle = item.title
                                                        draggingType = item.fileType
                                                        draggingVaultItem = item
                                                        draggingTempItem = null
                                                        draggingCurrentTouch = rect.topLeft + startOffset
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        draggingCurrentTouch += dragAmount

                                                        val currentTouchPoint = draggingCurrentTouch

                                                        // Check folder hover overlays
                                                        activeHoveredFolder = null
                                                        for ((fName, fRect) in folderBounds) {
                                                            if (fRect.contains(currentTouchPoint)) {
                                                                activeHoveredFolder = fName
                                                                break
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        if (activeHoveredFolder != null) {
                                                            val destFolder = activeHoveredFolder!!
                                                            val folderTarget = if (destFolder == "All") "Personal" else destFolder
                                                            viewModel.updateVaultItemFolder(item.id, folderTarget)
                                                            feedbackMessage = "Moved document '${item.title}' directly to folder '$folderTarget'!"
                                                            feedbackColor = AccentBlue
                                                        }
                                                        isDraggingActive = false
                                                        activeHoveredFolder = null
                                                        draggingVaultItem = null
                                                    },
                                                    onDragCancel = {
                                                        isDraggingActive = false
                                                        activeHoveredFolder = null
                                                        draggingVaultItem = null
                                                    }
                                                )
                                            }
                                    )

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.title,
                                                color = LightText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentBlue.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.fileType,
                                                    color = AccentBlue,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.folder.uppercase(),
                                                    color = AccentOrange,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Text(
                                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                                color = LightTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { viewModel.toggleEncryptedRepresentation(item.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (showEncrypted) AccentOrange else Color.White.copy(alpha = 0.05f)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (showEncrypted) "Decipher" else "Ciphertext",
                                            color = if (showEncrypted) Color.White else LightText,
                                            fontSize = 10.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteVaultItem(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Purge Vault Node", tint = AccentMagenta, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Content representation showing cipher representation perfectly as requested
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBg)
                                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.04f)))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (showEncrypted) "ENCRYPTED CIPHER BLOCKSTREAM (AES-256)" else "DECRYPTED CLEARTEXT NODE",
                                        color = if (showEncrypted) AccentOrange else AccentBlue,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (showEncrypted) item.secureDataRepresentation else item.content,
                                        color = if (showEncrypted) AccentOrange else LightText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING DRAGGED REPLICA CARD (Unclipped layout floating over scroll margins)
        if (isDraggingActive) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (draggingCurrentTouch.x).toInt() - 100, // align with cursor
                            (draggingCurrentTouch.y).toInt() - 40
                        )
                    }
                    .zIndex(200f)
                    .width(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelBg.copy(alpha = 0.9f))
                    .border(BorderStroke(1.5.dp, AccentOrange), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (draggingType == "SECURE_PHOTO") Icons.Default.Image
                                     else if (draggingType == "SECURE_NOTE") Icons.Default.Description
                                     else Icons.AutoMirrored.Filled.Article,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = draggingTitle,
                            color = LightText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeHoveredFolder != null) "Drop on folder: ${activeHoveredFolder!!.uppercase()}"
                                   else if (activeHoveredDropzone) "Drop on Seal Zone"
                                   else "Drag to Dropzone / Folders",
                            color = if (activeHoveredFolder != null || activeHoveredDropzone) AccentBlue else LightTextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = PanelBg,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "NEW HYPER SECURE LOG",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Log Title (e.g. Scanned Identity Secret)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_vault_title")
                    )

                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { docContent = it },
                        label = { Text("Confidential Content Area") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("add_vault_content"),
                        maxLines = 5
                    )

                    Text("Resource Key-Type", color = LightTextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf("SECURE_NOTE", "DOCUMENT", "SECURE_PHOTO")
                        types.forEach { t ->
                            val selected = fileType == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) AccentBlue else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (selected) AccentBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .clickable { fileType = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t.replace("_", " "), color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Encryption Algorithm (Local Cipher)", color = LightTextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ciphers = listOf("AES-256", "CHACHA20")
                        ciphers.forEach { c ->
                            val selected = selectedCipher == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) AccentOrange.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (selected) AccentOrange else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .clickable { selectedCipher = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (selected) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (selected) AccentOrange else LightTextMuted,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(c, color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = LightTextMuted)
                        }
                        Button(
                            onClick = {
                                val destFolder = if (selectedFolder == "All") "Personal" else selectedFolder
                                viewModel.addVaultItem(docTitle, fileType, docContent, destFolder, selectedCipher)
                                docTitle = ""
                                docContent = ""
                                fileType = "SECURE_NOTE"
                                selectedCipher = "AES-256"
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f).testTag("save_vault_item")
                        ) {
                            Text("Seal Log", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SECURE MESSAGES TAB -----------------
@Composable
fun MessagesScreen(viewModel: SecureViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    var selectedContact by remember { mutableStateOf("Alice (Secured)") }
    var typings by remember { mutableStateOf("") }
    var selectedAlgorithm by remember { mutableStateOf("AES-256") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("messages_screen_container")
    ) {
        // Chat Header with Magenta border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EPHEMERAL MESSAGING NODE",
                    color = AccentMagenta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = selectedContact,
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Simple Contact Swapper using orange actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Alice", "Bob").forEach { contactName ->
                    val fullName = "$contactName (Secured)"
                    val isCurr = selectedContact == fullName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurr) AccentMagenta else Color.White.copy(alpha = 0.04f))
                            .clickable { selectedContact = fullName }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(contactName, color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chats Thread
        val channelMessages = messages.filter { it.contactName == selectedContact }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(channelMessages) { msg ->
                var decryptedToggled by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isOutgoing) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (msg.isOutgoing) 12.dp else 0.dp,
                                    bottomEnd = if (msg.isOutgoing) 0.dp else 12.dp
                                )
                            )
                            .background(if (msg.isOutgoing) PanelBg else ObsidianBg)
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    if (msg.isOutgoing) AccentMagenta.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f)
                                ),
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (msg.isOutgoing) 12.dp else 0.dp,
                                    bottomEnd = if (msg.isOutgoing) 0.dp else 12.dp
                                )
                            )
                            .clickable { decryptedToggled = !decryptedToggled }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (msg.isOutgoing) "OUTGOING ENCIPHERED STREAM" else "INCOMING ENCIPHERED STREAM",
                                color = if (msg.isOutgoing) AccentMagenta else AccentBlue,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (decryptedToggled) "CLEARTEXT ON" else "CIPHER ON",
                                color = AccentOrange,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = if (decryptedToggled) msg.messageText else msg.encryptedPayload,
                            color = if (decryptedToggled) LightText else AccentOrange,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Encrypt and Send form with selections of Cipher Algorithm
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cipher selections
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Safe Protocol:", color = LightTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("AES-256", "ChaCha20", "RSA-4096").forEach { alg ->
                        val isSel = selectedAlgorithm == alg
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) AccentBlue else Color.White.copy(alpha = 0.03f))
                                .clickable { selectedAlgorithm = alg }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(alg, color = LightText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Input & Send actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = typings,
                    onValueChange = { typings = it },
                    placeholder = { Text("Write cleartext node message...", color = LightTextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText,
                        focusedBorderColor = AccentMagenta,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.weight(1f).testTag("message_input_control")
                )

                Button(
                    onClick = {
                        if (typings.isNotBlank()) {
                            viewModel.sendMessage(selectedContact, typings, selectedAlgorithm)
                            typings = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMagenta),
                    modifier = Modifier.height(48.dp).testTag("message_send_action"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send secure", tint = Color.White)
                }
            }
        }
    }
}

// ---------------- 16 IDENTITY ENVIRONMENT NODULES TAB -----------------
@Composable
fun IdentityScreen(viewModel: SecureViewModel) {
    val modules by viewModel.identityModules.collectAsStateWithLifecycle()
    var selectedModuleForEdit by remember { mutableStateOf<IdentityModule?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("identity_screen_container")
    ) {
        Column {
            Text(
                text = "16 ENVIRONMENT NODE PORTAL",
                color = AccentOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Customizable Identity Modules",
                color = LightText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure custom authentication channels and scopes for each node profile independently to shield workflows.",
                color = LightTextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(modules, key = { it.id }) { mod ->
                IdentityGridCard(
                    module = mod,
                    onClick = { selectedModuleForEdit = mod }
                )
            }
        }
    }

    if (selectedModuleForEdit != null) {
        val editing = selectedModuleForEdit!!
        var mfaEnabled by remember { mutableStateOf(editing.isMfaEnabled) }
        var mfaType by remember { mutableStateOf(editing.mfaType) }
        var status by remember { mutableStateOf(editing.status) }
        var securityScope by remember { mutableStateOf(editing.securityScope) }

        Dialog(onDismissRequest = { selectedModuleForEdit = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = PanelBg,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CUSTOMIZE NODE: ${editing.id}",
                        color = AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = editing.name,
                        color = LightText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    // MFA Option Toggles as requested
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Secondary Multi-Factor Auth", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Requires MFA to verify entry", color = LightTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = mfaEnabled,
                            onCheckedChange = { mfaEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = AccentOrange),
                            modifier = Modifier.testTag("mfa_toggle")
                        )
                    }

                    if (mfaEnabled) {
                        Text("Select MFA Verification Nodule:", color = LightTextMuted, fontSize = 11.sp)
                        val types = listOf("Email OTP", "Authenticator APP", "Secure Key Yubi", "SMS")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            types.forEach { t ->
                                val selected = mfaType == t
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) AccentOrange else Color.White.copy(alpha = 0.02f))
                                        .border(0.5.dp, if (selected) AccentOrange else Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .clickable { mfaType = t }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(t.replace(" OTP", "").replace(" APP", ""), color = LightText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    Text("Integrity Status", color = LightTextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ACTIVE", "RESTRICTED", "DISABLED").forEach { st ->
                            val isSel = status == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSel) {
                                            when (st) {
                                                "ACTIVE" -> AccentBlue.copy(alpha = 0.2f)
                                                "RESTRICTED" -> AccentOrange.copy(alpha = 0.2f)
                                                else -> AccentMagenta.copy(alpha = 0.2f)
                                            }
                                        } else Color.White.copy(alpha = 0.02f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) {
                                            when (st) {
                                                "ACTIVE" -> AccentBlue
                                                "RESTRICTED" -> AccentOrange
                                                else -> AccentMagenta
                                            }
                                        } else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { status = st }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(st, color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Network Sandbox Profile", color = LightTextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Strict", "Standard", "Low Telemetry").forEach { sc ->
                            val isSel = securityScope == sc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) AccentBlue else Color.White.copy(alpha = 0.02f))
                                    .border(1.dp, if (isSel) AccentBlue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                    .clickable { securityScope = sc }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sc, color = LightText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { selectedModuleForEdit = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", color = LightTextMuted)
                        }
                        Button(
                            onClick = {
                                viewModel.updateIdentityModule(
                                    editing.copy(
                                        isMfaEnabled = mfaEnabled,
                                        mfaType = mfaType,
                                        status = status,
                                        securityScope = securityScope
                                    )
                                )
                                selectedModuleForEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f).testTag("save_identity_changes_btn")
                        ) {
                            Text("Commit Settings", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityGridCard(module: IdentityModule, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBg)
            .border(
                BorderStroke(
                    0.5.dp,
                    when (module.status) {
                        "ACTIVE" -> AccentBlue.copy(alpha = 0.2f)
                        "RESTRICTED" -> AccentOrange.copy(alpha = 0.2f)
                        else -> AccentMagenta.copy(alpha = 0.2f)
                    }
                ), RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("identity_card_${module.id}"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODULE ${module.id}",
                    color = AccentOrange,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (module.status) {
                                "ACTIVE" -> AccentBlue
                                "RESTRICTED" -> AccentOrange
                                else -> AccentMagenta
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = module.name,
                color = LightText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = module.description,
                color = LightTextMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (module.isMfaEnabled) AccentOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (module.isMfaEnabled) "MFA: ${module.mfaType.replace("Secure Key ", "")}" else "MFA OFF",
                    color = if (module.isMfaEnabled) AccentOrange else LightTextMuted,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${module.ratingPercent}% Shield",
                color = AccentBlue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ---------------- ADTECH LIVE TRACKERS BLOCKED DETAILS -----------------
@Composable
fun TrackerScreen(viewModel: SecureViewModel) {
    val totalTrackersBlocked by viewModel.totalTrackersBlocked.collectAsStateWithLifecycle()
    val isTrackingShieldOn by viewModel.isTrackingShieldOn.collectAsStateWithLifecycle()
    val trackingLogs by viewModel.trackingLogs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("trackers_screen_container")
    ) {
        Column {
            Text(
                text = "REAL-TIME TELEMETRY SHIELD",
                color = AccentBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "DNS Tracker Firewall",
                color = LightText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shield Switch Info Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(
                    BorderStroke(
                        1.dp,
                        if (isTrackingShieldOn) AccentBlue.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f)
                    ), RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = if (isTrackingShieldOn) "Local Spoofing Protection is ON" else "Local DNS Shield suspended",
                    color = LightText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isTrackingShieldOn) "We are dropping and spoofing analytic SDK nodes locally before connections leaves the sandbox." else "Your external analytics telemetry is exposed under current diagnostic profiles.",
                    color = LightTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Switch(
                checked = isTrackingShieldOn,
                onCheckedChange = { viewModel.isTrackingShieldOn.value = it },
                colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue),
                modifier = Modifier.testTag("tracker_shield_toggle")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sandbox Telemetry Interceptor Card
        var testUrlInput by remember { mutableStateOf("") }
        var interceptionResult by remember { mutableStateOf<String?>(null) }
        var interceptionSuccess by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SANDBOX TELEMETRY INTERCEPTOR",
                color = AccentBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Input a URL to simulate and intercept background DNS analytics calls:",
                color = LightTextMuted,
                fontSize = 11.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = testUrlInput,
                    onValueChange = { 
                        testUrlInput = it 
                        interceptionResult = null
                    },
                    placeholder = { Text("e.g. https://google-analytics.com/collect", color = LightTextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sandbox_url_input")
                )

                Button(
                    onClick = {
                        if (testUrlInput.isNotBlank()) {
                            val blocked = viewModel.interceptAndBlockUrl(testUrlInput)
                            interceptionSuccess = blocked
                            if (blocked) {
                                interceptionResult = "INTERCEPTED: Dropped analytics stream safely!"
                                testUrlInput = ""
                            } else {
                                interceptionResult = if (!isTrackingShieldOn) {
                                    "FAILED: Anti-Tracking Shield is turned OFF!"
                                } else {
                                    "PASSED: URL did not trigger DNS telemetry rules."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sandbox_test_btn")
                ) {
                    Text("Intercept", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            interceptionResult?.let { res ->
                Text(
                    text = res,
                    color = if (interceptionSuccess) AccentBlue else AccentOrange,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LIVE BLOCKED ATTACKS MONITOR",
            color = AccentMagenta,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trackingLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PanelBg)
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.75f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = log.company,
                                color = LightText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentMagenta.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = log.action,
                                    color = AccentMagenta,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.url,
                            color = LightTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                        color = LightTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ---------------- COMPLEX REGULAR SECURITY RISK AUDITS TAB -----------------
@Composable
fun AuditScreen(viewModel: SecureViewModel) {
    val isAuditing by viewModel.isAuditing.collectAsStateWithLifecycle()
    val auditResults by viewModel.auditResults.collectAsStateWithLifecycle()
    val score by viewModel.overallSecurityScore.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("audit_screen_container")
    ) {
        Column {
            Text(
                text = "REGULAR SECURITY INTEGRITY AUDITS",
                color = AccentMagenta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Device Security Sweeper",
                color = LightText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Perform offline security audits. Sweep all local password nodes, tracking shield parameters, cloud handshake logs, and active identities to locate exposures.",
                color = LightTextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        // Circular sweep progress dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(BorderStroke(1.dp, AccentMagenta.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.6f)) {
                Text(text = "Overall Audit Score", color = LightTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (score >= 80) "SECURE PROFILE SECURED" else "VULNERABILITIES DETECTED",
                    color = if (score >= 80) AccentBlue else AccentOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Aegis rating is based on passwords entropy, dynamic network locks, and MFA module validations.",
                    color = LightTextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        color = when {
                            score >= 80 -> AccentBlue
                            score >= 50 -> AccentOrange
                            else -> AccentMagenta
                        },
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "$score%",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.runFullSecurityAudit() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMagenta),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("trigger_audit_btn")
                ) {
                    Text(
                        text = if (isAuditing) "Sweeping..." else "Audit Sweep",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isAuditing) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentMagenta)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Sweeping local vaults & cipher handshakes...", color = LightTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(auditResults) { check ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PanelBg)
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    when (check.status) {
                                        AuditStatus.SUCCESS -> AccentBlue.copy(alpha = 0.15f)
                                        AuditStatus.WARNING -> AccentOrange.copy(alpha = 0.15f)
                                        AuditStatus.DANGER -> AccentMagenta.copy(alpha = 0.15f)
                                    }
                                ), RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (check.status) {
                                                AuditStatus.SUCCESS -> AccentBlue
                                                AuditStatus.WARNING -> AccentOrange
                                                AuditStatus.DANGER -> AccentMagenta
                                            }
                                        )
                                )
                                Text(
                                    text = check.title,
                                    color = LightText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = check.description,
                                color = LightTextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        // Short-cuts to help users act immediately
                        if (check.fixAction != null) {
                            Button(
                                onClick = {
                                    val dst = when (check.fixAction) {
                                        "SHIELD_ON" -> {
                                            viewModel.isTrackingShieldOn.value = true
                                            "TRACKERS"
                                        }
                                        "RESET_PIN" -> {
                                            viewModel.userPin.value = "4801"
                                            "DASHBOARD"
                                        }
                                        "GOTO_VAULT" -> "VAULT"
                                        "GOTO_PASSWORDS" -> "PASSWORDS"
                                        "GOTO_IDENTITY" -> "IDENTITY"
                                        else -> "DASHBOARD"
                                    }
                                    viewModel.activeTab.value = dst
                                    viewModel.runFullSecurityAudit()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.15f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .border(0.5.dp, AccentOrange, RoundedCornerShape(4.dp))
                                    .testTag("fix_action_${check.fixAction.lowercase()}")
                            ) {
                                Text("Fix Now", color = AccentOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Passed",
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Architect AI MCP Screen ───────────────────────────────────────────────────
//
// Connects to the local Architect AI MCP server (Node/Express on :3001) via
// the REST bridge endpoints.  Works on emulator (10.0.2.2) and physical
// devices on the same LAN.
//
// Required: `npm run dev` inside agape-sovereign/architect-mcp-server/ AND
//           `ollama serve` with gemma4:e2b pulled.

@Composable
fun ArchitectMcpScreen(viewModel: SecureViewModel) {
    val isMcpLoading by viewModel.isMcpLoading.collectAsStateWithLifecycle()
    val mcpReply by viewModel.mcpReply.collectAsStateWithLifecycle()
    val mcpServerHealthy by viewModel.mcpServerHealthy.collectAsStateWithLifecycle()
    val securityScore by viewModel.overallSecurityScore.collectAsStateWithLifecycle()
    val auditResults by viewModel.auditResults.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("ASK") } // ASK | VECTOR | AUDIT

    LaunchedEffect(Unit) {
        viewModel.checkMcpHealth()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ARCHITECT AI",
                    color = AccentMagenta,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "gemma4:e2b  •  MCP Server  •  Offline",
                    color = LightTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Health indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (mcpServerHealthy) {
                            true -> AccentBlue.copy(alpha = 0.12f)
                            false -> AccentMagenta.copy(alpha = 0.12f)
                            null -> Color.White.copy(alpha = 0.05f)
                        }
                    )
                    .clickable { viewModel.checkMcpHealth() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .border(
                        0.5.dp,
                        when (mcpServerHealthy) {
                            true -> AccentBlue
                            false -> AccentMagenta
                            null -> LightTextMuted
                        },
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            when (mcpServerHealthy) {
                                true -> AccentBlue
                                false -> AccentMagenta
                                null -> LightTextMuted
                            }
                        )
                )
                Text(
                    text = when (mcpServerHealthy) {
                        true -> "SERVER OK"
                        false -> "OFFLINE"
                        null -> "CHECKING"
                    },
                    color = LightText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Mode selector chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ASK" to "Ask AI", "VECTOR" to "Vector Analysis", "AUDIT" to "Audit Fix").forEach { (mode, label) ->
                val selected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) AccentMagenta.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) AccentMagenta else LightTextMuted.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedMode = mode }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) AccentMagenta else LightTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Quick-action buttons for AUDIT mode
        if (selectedMode == "AUDIT" && auditResults.isNotEmpty()) {
            Text(
                text = "TAP A FINDING TO GENERATE AN AI FIX",
                color = LightTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(0.35f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(auditResults.filter { it.status != AuditStatus.SUCCESS }) { check ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PanelBg)
                            .border(
                                0.5.dp,
                                if (check.status == AuditStatus.DANGER) AccentMagenta else AccentOrange,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.mcpAuditRecommendation(check) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (check.status == AuditStatus.DANGER) AccentMagenta else AccentOrange)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(check.title, color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(check.description, color = LightTextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Ask Architect",
                            tint = AccentMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Input area (ASK + VECTOR modes)
        if (selectedMode != "AUDIT") {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = when (selectedMode) {
                            "ASK" -> "Ask about privacy, security, mobile threats..."
                            "VECTOR" -> "Describe exposure data or paste scan results..."
                            else -> ""
                        },
                        color = LightTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentMagenta,
                    unfocusedBorderColor = LightTextMuted.copy(alpha = 0.3f),
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText,
                    cursorColor = AccentMagenta,
                    focusedContainerColor = PanelBg,
                    unfocusedContainerColor = PanelBg
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                minLines = 3,
                maxLines = 6
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        when (selectedMode) {
                            "ASK" -> viewModel.mcpAsk(inputText)
                            "VECTOR" -> viewModel.mcpAnalyzeVector(
                                vectorId = "V-scan",
                                vectorName = "Manual Scan Input",
                                rawData = inputText
                            )
                        }
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isMcpLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentMagenta,
                    disabledContainerColor = AccentMagenta.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isMcpLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("ARCHITECT AI PROCESSING...", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (selectedMode) {
                            "ASK" -> "ASK ARCHITECT AI"
                            "VECTOR" -> "ANALYZE VECTOR"
                            else -> "SUBMIT"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        // Reply display
        if (mcpReply.isNotBlank() || isMcpLoading) {
            Text(
                text = "ARCHITECT AI RESPONSE",
                color = LightTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelBg)
                    .border(
                        1.dp,
                        if (mcpReply.startsWith("ERROR:")) AccentMagenta else AccentBlue.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(14.dp)
            ) {
                if (isMcpLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AccentMagenta,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Running gemma4:e2b locally...",
                            color = LightTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    val isError = mcpReply.startsWith("ERROR:")
                    LazyColumn {
                        item {
                            Text(
                                text = mcpReply,
                                color = if (isError) AccentMagenta else LightText,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                            if (isError) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Start the server:  cd agape-sovereign/architect-mcp-server && npm run dev",
                                    color = AccentOrange,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Android emulator binds to:  http://10.0.2.2:3001",
                                    color = LightTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelBg)
                    .border(0.5.dp, LightTextMuted.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AccentMagenta.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Architect AI",
                        color = LightTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "gemma4:e2b  •  MCP Server  •  Offline",
                        color = LightTextMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Score: $securityScore/100",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
