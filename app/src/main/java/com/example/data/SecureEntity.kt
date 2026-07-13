package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val username: String,
    val passwordDecrypted: String,
    val strength: String, // "WEAK", "MEDIUM", "STRONG"
    val category: String, // "Finance", "Social", "Email", "Work", "Other"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fileType: String, // "DOCUMENT", "SECURE_NOTE", "SECURE_PHOTO"
    val content: String,
    val secureDataRepresentation: String, // Simulated ciphertext
    val timestamp: Long = System.currentTimeMillis(),
    val folder: String = "Personal"
)

@Entity(tableName = "secure_messages")
data class SecureMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val messageText: String,
    val encryptedPayload: String, // Simulated cipher blocks representation
    val isOutgoing: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "identity_modules")
data class IdentityModule(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val isMfaEnabled: Boolean,
    val mfaType: String, // "Email", "Authenticator APP", "Secure Key Yubi", "SMS"
    val status: String, // "ACTIVE", "RESTRICTED", "DISABLED"
    val securityScope: String, // "Strict", "Standard", "Low Telemetry"
    val ratingPercent: Int
)
