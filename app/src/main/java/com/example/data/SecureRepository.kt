package com.example.data

import kotlinx.coroutines.flow.Flow

class SecureRepository(private val secureDao: SecureDao) {
    val allPasswords: Flow<List<PasswordEntry>> = secureDao.getAllPasswords()
    val allVaultItems: Flow<List<VaultItem>> = secureDao.getAllVaultItems()
    val allMessages: Flow<List<SecureMessage>> = secureDao.getAllMessages()
    val allIdentityModules: Flow<List<IdentityModule>> = secureDao.getAllIdentityModules()

    suspend fun insertPassword(password: PasswordEntry) {
        secureDao.insertPassword(password)
    }

    suspend fun deletePasswordById(id: Int) {
        secureDao.deletePasswordById(id)
    }

    suspend fun insertVaultItem(item: VaultItem) {
        secureDao.insertVaultItem(item)
    }

    suspend fun deleteVaultItemById(id: Int) {
        secureDao.deleteVaultItemById(id)
    }

    suspend fun insertMessage(message: SecureMessage) {
        secureDao.insertMessage(message)
    }

    suspend fun deleteMessageById(id: Int) {
        secureDao.deleteMessageById(id)
    }

    suspend fun getIdentityModulesCount(): Int {
        return secureDao.getIdentityModulesCount()
    }

    suspend fun insertIdentityModules(modules: List<IdentityModule>) {
        secureDao.insertIdentityModules(modules)
    }

    suspend fun updateIdentityModule(module: IdentityModule) {
        secureDao.updateIdentityModule(module)
    }
}
