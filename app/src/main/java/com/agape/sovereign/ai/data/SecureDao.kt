package com.agape.sovereign.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SecureDao {
    // Passwords
    @Query("SELECT * FROM passwords ORDER BY timestamp DESC")
    fun getAllPasswords(): Flow<List<PasswordEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntry)

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Int)

    // Vault
    @Query("SELECT * FROM vault_items ORDER BY timestamp DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemById(id: Int)

    // Messages
    @Query("SELECT * FROM secure_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SecureMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SecureMessage)

    @Query("DELETE FROM secure_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    // Identity Modules
    @Query("SELECT * FROM identity_modules ORDER BY id ASC")
    fun getAllIdentityModules(): Flow<List<IdentityModule>>

    @Query("SELECT COUNT(*) FROM identity_modules")
    suspend fun getIdentityModulesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentityModule(module: IdentityModule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentityModules(modules: List<IdentityModule>)

    @Update
    suspend fun updateIdentityModule(module: IdentityModule)
}
