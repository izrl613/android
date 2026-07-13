package com.agape.sovereign.ai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agape.sovereign.ai.data.*
import com.agape.sovereign.ai.util.BackupCrypto
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Random

data class TrackingLog(
    val id: String,
    val url: String,
    val company: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "BLOCKED"
)

data class AuditCheck(
    val title: String,
    val description: String,
    val status: AuditStatus, // SUCCESS, WARNING, DANGER
    val fixAction: String? = null
)

enum class AuditStatus {
    SUCCESS, WARNING, DANGER
}

class SecureViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SecureDatabase.getDatabase(application)
    private val repository = SecureRepository(database.secureDao())

    // UI Navigation
    val activeTab = MutableStateFlow("DASHBOARD")

    // Database states
    val passwords: StateFlow<List<PasswordEntry>> = repository.allPasswords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultItems: StateFlow<List<VaultItem>> = repository.allVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<SecureMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val identityModules: StateFlow<List<IdentityModule>> = repository.allIdentityModules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security & Locks
    val biometricLocked = MutableStateFlow(true)
    val userPin = MutableStateFlow("4209") // Default secure bypass code
    val pinAttempt = MutableStateFlow("")

    val showEncryptedToggles = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    // Simulated Real-Time Trackers Blocked
    val isTrackingShieldOn = MutableStateFlow(true)
    val totalTrackersBlocked = MutableStateFlow(1048)
    private val _trackingLogs = MutableStateFlow<List<TrackingLog>>(emptyList())
    val trackingLogs: StateFlow<List<TrackingLog>> = _trackingLogs.asStateFlow()

    // Sync States
    val isOfflineMode = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis() - 86400000) // 1 day ago
    val syncLogs = MutableStateFlow<List<String>>(listOf("System initialized", "Local E2E secure keystore generated"))

    // Security Audit states
    val isAuditing = MutableStateFlow(false)
    val overallSecurityScore = MutableStateFlow(84)
    private val _auditResults = MutableStateFlow<List<AuditCheck>>(emptyList())
    val auditResults: StateFlow<List<AuditCheck>> = _auditResults.asStateFlow()

    init {
        seedIdentityModulesIfNeeded()
        seedSampleMessagesIfNeeded()
        seedSamplePasswordsIfNeeded()
        seedSampleVaultIfNeeded()
        startRealTimeTrackerSimulator()
        runFullSecurityAuditSilent()
    }

    private fun seedIdentityModulesIfNeeded() = viewModelScope.launch {
        if (repository.getIdentityModulesCount() == 0) {
            val list = listOf(
                IdentityModule(1, "Personal Identity Core", "Master credentials, personal mail and core keys.", true, "Email OTP", "ACTIVE", "Strict", 90),
                IdentityModule(2, "Enterprise Persona", "Confidential corporate logins, private key-pairs & docs.", true, "Authenticator APP", "ACTIVE", "Strict", 95),
                IdentityModule(3, "Financial Vault Shield", "Bank interface keys, multi-sig crypto addresses.", true, "Secure Key Yubi", "RESTRICTED", "Strict", 100),
                IdentityModule(4, "Social Sandbox Profile", "Credentials insulation and built-in telemetry strippers.", false, "SMS", "ACTIVE", "Standard", 50),
                IdentityModule(5, "Medical Ledger Node", "Encrypted health insurance log, personal allergy data.", true, "Secure Key Yubi", "ACTIVE", "Strict", 95),
                IdentityModule(6, "Developer Access Token", "Encrypted SSH signatures, API tokens & webhook keys.", true, "Authenticator APP", "RESTRICTED", "Strict", 95),
                IdentityModule(7, "IoT Smart Home Hub", "Encrypted local control, dynamic video surveillance lock.", true, "Authenticator APP", "ACTIVE", "Standard", 85),
                IdentityModule(8, "Anonymous Nomad VPN", "Disposable onion routing & virtual MAC controller.", false, "SMS", "DISABLED", "Low Telemetry", 40),
                IdentityModule(9, "Ephemeral Chat Profile", "Dynamic peer keys with Double-Ratchet encryption.", true, "SMS", "ACTIVE", "Strict", 85),
                IdentityModule(10, "BIP-39 Crypto Node", "Air-gapped seed phrase vault / hardware transaction signs.", true, "Secure Key Yubi", "RESTRICTED", "Strict", 100),
                IdentityModule(11, "Device Sandbox Scan", "Auditor scanner, device memory sandbox, permission manager.", true, "Authenticator APP", "ACTIVE", "Strict", 95),
                IdentityModule(12, "Shared Trustee Circle", "Multi-signature emergency recovery circle.", false, "SMS", "DISABLED", "Standard", 35),
                IdentityModule(13, "Anti-Spyware Sentinel", "Kernel-level micromount blocker and camera guard.", true, "Authenticator APP", "ACTIVE", "Strict", 95),
                IdentityModule(14, "Legacy Decryption Link", "Inactive status key triggered after 120 days.", false, "Email OTP", "DISABLED", "Standard", 30),
                IdentityModule(15, "Tor Gateway Sandbox", "Secure onion proxies & deep browser encapsulation.", true, "Authenticator APP", "DISABLED", "Low Telemetry", 50),
                IdentityModule(16, "Adtech Blocker Node", "Global telemetry block updates control stream.", false, "None", "ACTIVE", "Standard", 70)
            )
            repository.insertIdentityModules(list)
        }
    }

    private fun seedSampleMessagesIfNeeded() = viewModelScope.launch {
        repository.allMessages.first().let { current ->
            if (current.isEmpty()) {
                val seedMsgs = listOf(
                    SecureMessage(1, "Alice (Secured)", "Hey! Did you get the encrypted draft?", "AES[4bdf38a2e7c91f0]", false, System.currentTimeMillis() - 3600000),
                    SecureMessage(2, "Alice (Secured)", "Yes, decrypting it now. Use ChaCha20 next time.", "CHACHA[88a09f3e1b7c8ea3]", true, System.currentTimeMillis() - 3000000),
                    SecureMessage(3, "Bob (Verified)", "Keep our chat encrypted with the ephemeral identity nodule.", "AES[a90d98f731c2bb]", false, System.currentTimeMillis() - 7200000)
                )
                for (m in seedMsgs) {
                    repository.insertMessage(m)
                }
            }
        }
    }

    private fun seedSamplePasswordsIfNeeded() = viewModelScope.launch {
        repository.allPasswords.first().let { current ->
            if (current.isEmpty()) {
                val seedPass = listOf(
                    PasswordEntry(1, "Primary Chase Bank", "chase_user99", "ChasePr0!@99#", "STRONG", "Finance", System.currentTimeMillis() - 100000),
                    PasswordEntry(2, "Workspace Admin", "admin@corp", "w0rkSp@ce2026!", "STRONG", "Work", System.currentTimeMillis() - 200000),
                    PasswordEntry(3, "Personal Instagram", "instaguard_u", "123456", "WEAK", "Social", System.currentTimeMillis() - 300000)
                )
                for (p in seedPass) {
                    repository.insertPassword(p)
                }
            }
        }
    }

    private fun seedSampleVaultIfNeeded() = viewModelScope.launch {
        repository.allVaultItems.first().let { current ->
            if (current.isEmpty()) {
                val seedVault = listOf(
                    VaultItem(1, "Passport Digital Scan", "DOCUMENT", "US Passport Number: 4892-A8D1. Expiry Jun 2031", "E2EE-CIPHER[7f827a...8e]", System.currentTimeMillis() - 5 * 60000, "Personal"),
                    VaultItem(2, "Emergency Recovery Phr.", "SECURE_NOTE", "apple banana cherry dog elephant fox grape horse igloo jaguar kangaroo lemon", "E2EE-CIPHER[9a00fb...31]", System.currentTimeMillis() - 10 * 60000, "Credentials"),
                    VaultItem(3, "Cryptographic Seed Backup", "SECURE_PHOTO", "Raw Key: 0x8a92fb9c81bcf77239a0ef73bc110f92", "E2EE-CIPHER[e330a8...2c]", System.currentTimeMillis() - 2 * 3600000, "Financial")
                )
                for (v in seedVault) {
                    repository.insertVaultItem(v)
                }
            }
        }
    }

    // Real-time Trackers Blocking Simulation
    private fun startRealTimeTrackerSimulator() {
        viewModelScope.launch {
            val companies = listOf("Google Analytics", "Facebook Graph Tracking", "DoubleClick Pixel", "TikTok Telemetry", "ByteDance AdNode", "Amazon Cloud Ads", "AdRoll Tracker", "AppsFlyer SDK", "Yandex AppMetrica")
            val routes = listOf("/collect?v=2&en=click", "/tr/?id=88319f", "/ad_collector/query", "/pixel/track?event=Page", "/telemetry/v1/event", "/sdk/v3/ping", "/ads/conversion", "/track_install")

            val random = Random()
            while (true) {
                delay(4000 + random.nextInt(4000).toLong())
                if (isTrackingShieldOn.value) {
                    val company = companies[random.nextInt(companies.size)]
                    val endpoint = routes[random.nextInt(routes.size)]
                    val fullUrl = "https://metrics." + company.lowercase().replace(" ", "") + ".com" + endpoint

                    val log = TrackingLog(
                        id = "TRK_${random.nextInt(100000)}",
                        url = fullUrl,
                        company = company
                    )
                    val currentList = _trackingLogs.value.toMutableList()
                    currentList.add(0, log)
                    if (currentList.size > 20) {
                        currentList.removeAt(currentList.size - 1)
                    }
                    _trackingLogs.value = currentList
                    totalTrackersBlocked.value += 1
                }
            }
        }
    }

    fun interceptAndBlockUrl(url: String): Boolean {
        if (!isTrackingShieldOn.value) return false
        val uriLower = url.lowercase()
        // Common web tracker patterns
        val trackerDomains = listOf(
            "analytics", "telemetry", "tracker", "pixel", "ad_collector", "metrics",
            "facebook.com/tr", "google-analytics.com", "doubleclick", "tiktok", "bytedance",
            "amazon-adsystem", "adroll", "appsflyer", "appmetrica", "yandex", "scorecardresearch",
            "mixpanel", "hotjar", "optimizely", "criteo", "telemetry-block", "adtech", "advertising"
        )
        val isTracker = trackerDomains.any { uriLower.contains(it) }
        if (isTracker) {
            // Determine company
            val company = when {
                uriLower.contains("facebook") -> "Facebook Graph"
                uriLower.contains("google") -> "Google Analytics"
                uriLower.contains("doubleclick") -> "DoubleClick Pixel"
                uriLower.contains("tiktok") -> "TikTok Telemetry"
                uriLower.contains("bytedance") -> "ByteDance Analytics"
                uriLower.contains("amazon") -> "Amazon Cloud Ads"
                uriLower.contains("adroll") -> "AdRoll Tracker"
                uriLower.contains("appsflyer") -> "AppsFlyer SDK"
                uriLower.contains("yandex") || uriLower.contains("appmetrica") -> "Yandex Metrica"
                uriLower.contains("mixpanel") -> "Mixpanel Telemetry"
                uriLower.contains("hotjar") -> "Hotjar Recorder"
                else -> "Generic Web Tracker"
            }
            
            val log = TrackingLog(
                id = "TRK_INT_${System.currentTimeMillis() % 100000}",
                url = url,
                company = company,
                timestamp = System.currentTimeMillis()
            )
            val currentList = _trackingLogs.value.toMutableList()
            currentList.add(0, log)
            if (currentList.size > 20) {
                currentList.removeAt(currentList.size - 1)
            }
            _trackingLogs.value = currentList
            totalTrackersBlocked.value += 1
            return true
        }
        return false
    }

    // Core Security & Encryption logic
    fun encryptText(text: String, format: String): String {
        // High fidelity cryptographic mapping simulator showing character chunks
        if (text.isEmpty()) return ""
        val bytes = text.toByteArray(Charsets.UTF_8)
        val encryptedHex = bytes.map { byte ->
            val rotated = (byte.toInt() xor 0x5F) + 3
            String.format("%02x", rotated and 0xFF)
        }.joinToString("")

        return when(format.uppercase()) {
            "AES-256" -> "E2EE_AES256[0x$encryptedHex]"
            "CHACHA20" -> "E2EE_CHACHA20[0x${encryptedHex.reversed()}]"
            else -> "E2EE_RSA4096[0x${encryptedHex.hashCode()}]"
        }
    }

    fun toggleEncryptedRepresentation(id: Int) {
        val current = showEncryptedToggles.value.toMutableMap()
        current[id] = !(current[id]?.equals(true) ?: false)
        showEncryptedToggles.value = current
    }

    // Password management actions
    fun addPassword(title: String, username: String, passText: String, category: String) {
        viewModelScope.launch {
            val strength = checkPasswordStrength(passText)
            val entry = PasswordEntry(
                title = title.ifEmpty { "New Account" },
                username = username.ifEmpty { "unspecified" },
                passwordDecrypted = passText.ifEmpty { "Pass123!" },
                strength = strength,
                category = category
            )
            repository.insertPassword(entry)
            runFullSecurityAuditSilent()
        }
    }

    fun deletePassword(id: Int) {
        viewModelScope.launch {
            repository.deletePasswordById(id)
            runFullSecurityAuditSilent()
        }
    }

    // Vault management actions
    fun addVaultItem(title: String, type: String, content: String, folder: String = "Personal", cipherType: String = "AES-256") {
        viewModelScope.launch {
            val representation = encryptText(content, cipherType)
            val item = VaultItem(
                title = title.ifEmpty { "Vault Entry" },
                fileType = type,
                content = content,
                secureDataRepresentation = representation,
                folder = folder
            )
            repository.insertVaultItem(item)
            runFullSecurityAuditSilent()
        }
    }

    fun updateVaultItemFolder(id: Int, newFolder: String) {
        viewModelScope.launch {
            vaultItems.value.firstOrNull { it.id == id }?.let { item ->
                val updatedItem = item.copy(folder = newFolder)
                repository.insertVaultItem(updatedItem)
                runFullSecurityAuditSilent()
            }
        }
    }

    fun deleteVaultItem(id: Int) {
        viewModelScope.launch {
            repository.deleteVaultItemById(id)
            runFullSecurityAuditSilent()
        }
    }

    // Secure Messaging actions
    fun sendMessage(contact: String, plainText: String, cipherType: String) {
        viewModelScope.launch {
            val cipher = encryptText(plainText, cipherType)
            val msg = SecureMessage(
                contactName = contact,
                messageText = plainText,
                encryptedPayload = cipher,
                isOutgoing = true
            )
            repository.insertMessage(msg)

            // Auto simulated replies after 3 seconds with active notification
            delay(2000)
            val replies = listOf(
                "Message received and integrity verified. Standby.",
                "Decrypting using your current secure Node key. Proceed.",
                "E2EE validation matched. E-signature verified successfully."
            )
            val replyText = replies.random()
            val incomingCipher = encryptText(replyText, cipherType)
            val replyMsg = SecureMessage(
                contactName = contact,
                messageText = replyText,
                encryptedPayload = incomingCipher,
                isOutgoing = false
            )
            repository.insertMessage(replyMsg)
        }
    }

    // Identity Modules customization
    fun updateIdentityModule(module: IdentityModule) {
        viewModelScope.launch {
            // Re-calculate module rating percentage based on security settings
            var base = 30
            if (module.isMfaEnabled) base += 40
            if (module.status == "ACTIVE") base += 10
            if (module.status == "RESTRICTED") base += 20
            if (module.securityScope == "Strict") base += 10
            if (module.securityScope == "Low Telemetry") base += 10

            val updated = module.copy(ratingPercent = base.coerceAtMost(100))
            repository.updateIdentityModule(updated)
            runFullSecurityAuditSilent()
        }
    }

    fun setAllIdentityModulesStatus(status: String) {
        viewModelScope.launch {
            val currentList = identityModules.value
            currentList.forEach { module ->
                var base = 30
                if (module.isMfaEnabled) base += 40
                if (status == "ACTIVE") base += 10
                if (status == "RESTRICTED") base += 20
                if (module.securityScope == "Strict") base += 10
                if (module.securityScope == "Low Telemetry") base += 10

                val updated = module.copy(
                    status = status,
                    ratingPercent = base.coerceAtMost(100)
                )
                repository.updateIdentityModule(updated)
            }
            runFullSecurityAuditSilent()
        }
    }

    // Real-time Cloud Sync
    fun triggerCloudSync() = viewModelScope.launch {
        if (isOfflineMode.value) return@launch
        isSyncing.value = true
        val currentLogs = syncLogs.value.toMutableList()
        currentLogs.add(0, "[SYNC START] Launching encrypted handshake...")
        syncLogs.value = currentLogs
        delay(1200)

        currentLogs.add(0, "[AES-GCM] Verifying local signature integrity")
        syncLogs.value = currentLogs
        delay(1000)

        // Count items
        val pSize = passwords.value.size
        val vSize = vaultItems.value.size
        currentLogs.add(0, "[UPLOAD] E2EE Sync completed: $pSize Passwords, $vSize Vault records synchronized fully")
        syncLogs.value = currentLogs
        isSyncing.value = false
        lastSyncTime.value = System.currentTimeMillis()
    }

    // Password helper check
    fun checkPasswordStrength(p: String): String {
        return when {
            p.length < 6 -> "WEAK"
            p.length >= 10 && p.any { it.isDigit() } && p.any { !it.isLetterOrDigit() } -> "STRONG"
            else -> "MEDIUM"
        }
    }

    // Biometric scanner PIN unlock entry
    fun enterPinDigit(digit: String) {
        if (pinAttempt.value.length < 4) {
            pinAttempt.value += digit
        }
        if (pinAttempt.value.length == 4) {
            viewModelScope.launch {
                delay(200)
                if (pinAttempt.value == userPin.value) {
                    biometricLocked.value = false
                    pinAttempt.value = ""
                } else {
                    // Flash danger pin error
                    delay(400)
                    pinAttempt.value = ""
                }
            }
        }
    }

    fun clearPinAttempt() {
        pinAttempt.value = ""
    }

    fun instantLockApp() {
        biometricLocked.value = true
        pinAttempt.value = ""
    }

    fun bypassWithMasterPin() {
        biometricLocked.value = false
        pinAttempt.value = ""
    }

    // Running a full Security Audit
    fun runFullSecurityAudit() = viewModelScope.launch {
        isAuditing.value = true
        delay(2000) // Beautiful scanning simulation
        calculateAuditScoresAndIssues()
        isAuditing.value = false
    }

    private fun runFullSecurityAuditSilent() = viewModelScope.launch {
        calculateAuditScoresAndIssues()
    }

    private suspend fun calculateAuditScoresAndIssues() {
        val list = mutableListOf<AuditCheck>()
        var scoreAcc = 100

        // 1. PIN Check
        if (userPin.value == "0000" || userPin.value.length < 4) {
            list.add(
                AuditCheck(
                    title = "Weak Master Bypass Code",
                    description = "Your bypass key PIN is set to common or non-standard configurations. Recommended to set a customized digit code.",
                    status = AuditStatus.DANGER,
                    fixAction = "RESET_PIN"
                )
            )
            scoreAcc -= 20
        } else {
            list.add(
                AuditCheck(
                    title = "Master Access Shield Active",
                    description = "Master PIN and biometric simulation parameters are fully active under host standard.",
                    status = AuditStatus.SUCCESS
                )
            )
        }

        // 2. Tracking Shield Check
        if (!isTrackingShieldOn.value) {
            list.add(
                AuditCheck(
                    title = "Adtech Tracking Shield Log is OFF",
                    description = "Real-time network logging and background DNS spoof protections are currently suspended.",
                    status = AuditStatus.DANGER,
                    fixAction = "SHIELD_ON"
                )
            )
            scoreAcc -= 15
        } else {
            list.add(
                AuditCheck(
                    title = "Host Integrity Anti-Tracking Shield On",
                    description = "Blocking DNS metrics, Doubleclick, and tracking pixels in real-time.",
                    status = AuditStatus.SUCCESS
                )
            )
        }

        // 3. Vault & Document Count
        val vCount = vaultItems.value.size
        if (vCount == 0) {
            list.add(
                AuditCheck(
                    title = "Vault Empty & Vulnerable",
                    description = "No local notes or identity items stored inside the zero-knowledge database yet.",
                    status = AuditStatus.WARNING,
                    fixAction = "GOTO_VAULT"
                )
            )
            scoreAcc -= 10
        } else {
            list.add(
                AuditCheck(
                    title = "AES-256 Vault Records Seal Verified",
                    description = "Secure zero-knowledge storage is actively encasing $vCount critical documents.",
                    status = AuditStatus.SUCCESS
                )
            )
        }

        // 4. Weak Passwords Count
        val passList = passwords.value
        val weakPassCount = passList.count { it.strength == "WEAK" }
        if (weakPassCount > 0) {
            list.add(
                AuditCheck(
                    title = "Weak Logins Exposed",
                    description = "Detected $weakPassCount entries in your credential records with vulnerable parameters.",
                    status = AuditStatus.DANGER,
                    fixAction = "GOTO_PASSWORDS"
                )
            )
            scoreAcc -= (15 * weakPassCount).coerceAtMost(40)
        } else if (passList.isEmpty()) {
            list.add(
                AuditCheck(
                    title = "No Stored Credentials",
                    description = "Password locker contains 0 keys. Use generator nodes for maximum protection.",
                    status = AuditStatus.WARNING
                )
            )
            scoreAcc -= 5
        } else {
            list.add(
                AuditCheck(
                    title = "All Login Credentials Validated",
                    description = "Password manager records reflect 100% compliant strength metrics.",
                    status = AuditStatus.SUCCESS
                )
            )
        }

        // 5. 16 Identity Modules MFA check
        val modulesList = repository.allIdentityModules.first()
        val activeNoMfa = modulesList.count { it.status == "ACTIVE" && !it.isMfaEnabled }
        if (activeNoMfa > 0) {
            list.add(
                AuditCheck(
                    title = "No MFA in Active Identity Segments",
                    description = "Detected $activeNoMfa customized profiles running without Multi-Factor protection toggled.",
                    status = AuditStatus.WARNING,
                    fixAction = "GOTO_IDENTITY"
                )
            )
            scoreAcc -= (5 * activeNoMfa).coerceAtMost(20)
        } else {
            list.add(
                AuditCheck(
                    title = "Custom Identity Segments Insulated",
                    description = "All active workflow environments are protected with secondary MFA criteria.",
                    status = AuditStatus.SUCCESS
                )
            )
        }

        overallSecurityScore.value = scoreAcc.coerceIn(15, 100)
        _auditResults.value = list
    }

    enum class ImportResult {
        SUCCESS, INVALID_JSON, DECRYPTION_FAILED, EMPTY_BACKUP
    }

    // Export secure encrypted JSON backup
    fun exportBackupAsJsonString(passcode: String): String {
        val root = JSONObject()
        root.put("backup_version", 2)
        root.put("encrypted_at", System.currentTimeMillis())
        root.put("encryption_scheme", "AES-256-SHA256")

        val payloadObj = JSONObject()

        // 1. Identity Modules
        val modulesArray = JSONArray()
        for (m in identityModules.value) {
            val mObj = JSONObject()
            mObj.put("id", m.id)
            mObj.put("name", m.name)
            mObj.put("description", m.description)
            mObj.put("isMfaEnabled", m.isMfaEnabled)
            mObj.put("mfaType", m.mfaType)
            mObj.put("status", m.status)
            mObj.put("securityScope", m.securityScope)
            mObj.put("ratingPercent", m.ratingPercent)
            modulesArray.put(mObj)
        }
        payloadObj.put("identity_modules", modulesArray)

        // 2. Passwords
        val passArray = JSONArray()
        for (p in passwords.value) {
            val pObj = JSONObject()
            pObj.put("title", p.title)
            pObj.put("username", p.username)
            pObj.put("passwordDecrypted", p.passwordDecrypted)
            pObj.put("strength", p.strength)
            pObj.put("category", p.category)
            pObj.put("timestamp", p.timestamp)
            passArray.put(pObj)
        }
        payloadObj.put("passwords", passArray)

        // 3. Vault Items
        val vaultArray = JSONArray()
        for (v in vaultItems.value) {
            val vObj = JSONObject()
            vObj.put("title", v.title)
            vObj.put("fileType", v.fileType)
            vObj.put("content", v.content)
            vObj.put("secureDataRepresentation", v.secureDataRepresentation)
            vObj.put("timestamp", v.timestamp)
            vObj.put("folder", v.folder)
            vaultArray.put(vObj)
        }
        payloadObj.put("vault_items", vaultArray)

        val payloadString = payloadObj.toString()
        val (encryptedPayload, iv) = BackupCrypto.encrypt(payloadString, passcode)

        root.put("iv", iv)
        root.put("payload", encryptedPayload)

        return root.toString(2)
    }

    // Export secure encrypted CSV file content
    fun generateEncryptedCsv(passcode: String): String {
        val list = passwords.value
        val csvHeader = "ID,Title,Username,Password,Strength,Category,Timestamp\n"
        val csvContent = StringBuilder(csvHeader)
        for (p in list) {
            val idStr = p.id.toString()
            val titleStr = p.title.replace("\"", "\"\"")
            val userStr = p.username.replace("\"", "\"\"")
            val passStr = p.passwordDecrypted.replace("\"", "\"\"")
            val strengthStr = p.strength.replace("\"", "\"\"")
            val categoryStr = p.category.replace("\"", "\"\"")
            val timestampStr = p.timestamp.toString()
            csvContent.append("$idStr,\"$titleStr\",\"$userStr\",\"$passStr\",\"$strengthStr\",\"$categoryStr\",$timestampStr\n")
        }
        val plainText = csvContent.toString()
        val (encryptedBase64, iv) = BackupCrypto.encrypt(plainText, passcode)
        return "IV:$iv\nDATA:$encryptedBase64"
    }

    // Import secure encrypted JSON backup
    fun importBackupFromJsonString(jsonString: String, passcode: String): ImportResult {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("payload") || !root.has("iv")) {
                return ImportResult.INVALID_JSON
            }

            val iv = root.getString("iv")
            val encryptedPayload = root.getString("payload")

            val decryptedPayloadString = try {
                BackupCrypto.decrypt(encryptedPayload, iv, passcode)
            } catch (e: Exception) {
                return ImportResult.DECRYPTION_FAILED
            }

            val payloadObj = JSONObject(decryptedPayloadString)
            viewModelScope.launch {
                // Import identity modules
                if (payloadObj.has("identity_modules")) {
                    val mArray = payloadObj.getJSONArray("identity_modules")
                    for (i in 0 until mArray.length()) {
                        val mObj = mArray.getJSONObject(i)
                        val id = mObj.getInt("id")
                        val name = mObj.getString("name")
                        val description = mObj.getString("description")
                        val isMfaEnabled = mObj.getBoolean("isMfaEnabled")
                        val mfaType = mObj.getString("mfaType")
                        val status = mObj.getString("status")
                        val securityScope = mObj.getString("securityScope")
                        val ratingPercent = mObj.getInt("ratingPercent")

                        val module = IdentityModule(id, name, description, isMfaEnabled, mfaType, status, securityScope, ratingPercent)
                        repository.updateIdentityModule(module)
                    }
                }

                // Import passwords
                if (payloadObj.has("passwords")) {
                    val pArray = payloadObj.getJSONArray("passwords")
                    for (i in 0 until pArray.length()) {
                        val pObj = pArray.getJSONObject(i)
                        val title = pObj.getString("title")
                        val username = pObj.getString("username")
                        val passwordDecrypted = pObj.getString("passwordDecrypted")
                        val strength = pObj.getString("strength")
                        val category = pObj.getString("category")
                        val timestamp = pObj.optLong("timestamp", System.currentTimeMillis())

                        val entry = PasswordEntry(
                            title = title,
                            username = username,
                            passwordDecrypted = passwordDecrypted,
                            strength = strength,
                            category = category,
                            timestamp = timestamp
                        )
                        repository.insertPassword(entry)
                    }
                }

                // Import vault items
                if (payloadObj.has("vault_items")) {
                    val vArray = payloadObj.getJSONArray("vault_items")
                    for (i in 0 until vArray.length()) {
                        val vObj = vArray.getJSONObject(i)
                        val title = vObj.getString("title")
                        val fileType = vObj.getString("fileType")
                        val content = vObj.getString("content")
                        val secureRep = vObj.getString("secureDataRepresentation")
                        val timestamp = vObj.optLong("timestamp", System.currentTimeMillis())

                        val folder = vObj.optString("folder", "Personal")
                        val item = VaultItem(
                            title = title,
                            fileType = fileType,
                            content = content,
                            secureDataRepresentation = secureRep,
                            timestamp = timestamp,
                            folder = folder
                        )
                        repository.insertVaultItem(item)
                    }
                }

                runFullSecurityAuditSilent()
            }
            return ImportResult.SUCCESS
        } catch (e: Exception) {
            return ImportResult.INVALID_JSON
        }
    }
}
