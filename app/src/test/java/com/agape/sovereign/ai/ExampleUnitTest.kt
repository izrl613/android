package com.agape.sovereign.ai

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.agape.sovereign.ai.util.BackupCrypto
import com.agape.sovereign.ai.ui.generateSecurePassword
import com.agape.sovereign.ai.ui.evaluatePasswordStrength

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPasswordGenerator_lengthAndProfiles() {
    val len = 20
    val password = generateSecurePassword(len, upper = true, lower = true, digits = true, symbols = true)
    
    assertEquals(len, password.length)
    assertTrue(password.any { it.isUpperCase() })
    assertTrue(password.any { it.isLowerCase() })
    assertTrue(password.any { it.isDigit() })
    assertTrue(password.any { !it.isLetterOrDigit() })
  }

  @Test
  fun testPasswordGenerator_emptyRequirements() {
    val password = generateSecurePassword(12, upper = false, lower = false, digits = false, symbols = false)
    assertEquals("", password)
  }

  @Test
  fun testPasswordStrength_evaluation() {
    assertEquals("WEAK", evaluatePasswordStrength("123"))
    assertEquals("MEDIUM", evaluatePasswordStrength("Abc12345"))
    assertEquals("STRONG", evaluatePasswordStrength("AbcdEfgh1234!_"))
    assertEquals("MILITARY-GRADE", evaluatePasswordStrength("Abcdefgh1234567890!@#$%"))
  }

  @Test
  fun backupCrypto_encryptAndDecrypt_isCorrect() {
    val plainText = "{\"identity_modules\":[],\"passwords\":[],\"vault_items\":[]}"
    val passcode = "998877"

    val (encrypted, iv) = BackupCrypto.encrypt(plainText, passcode)
    assertNotEquals(plainText, encrypted)
    assertTrue(encrypted.isNotEmpty())
    assertTrue(iv.isNotEmpty())

    val decrypted = BackupCrypto.decrypt(encrypted, iv, passcode)
    assertEquals(plainText, decrypted)
  }

  @Test
  fun testCsvEncryptionAndDecryption() {
    val mockCsv = "ID,Title,Username,Password,Strength,Category,Timestamp\n1,\"Chase Bank\",\"user123\",\"p@ss123\",\"STRONG\",\"Finance\",1700000000\n"
    val passcode = "secret123"
    val (encrypted, iv) = BackupCrypto.encrypt(mockCsv, passcode)
    assertNotEquals(mockCsv, encrypted)
    
    val decrypted = BackupCrypto.decrypt(encrypted, iv, passcode)
    assertEquals(mockCsv, decrypted)
  }
}
