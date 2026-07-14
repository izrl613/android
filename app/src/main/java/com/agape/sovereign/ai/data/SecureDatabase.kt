package com.agape.sovereign.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PasswordEntry::class, VaultItem::class, SecureMessage::class, IdentityModule::class],
    version = 2,
    exportSchema = false
)
abstract class SecureDatabase : RoomDatabase() {
    abstract fun secureDao(): SecureDao

    companion object {
        @Volatile
        private var INSTANCE: SecureDatabase? = null

        fun getDatabase(context: Context): SecureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecureDatabase::class.java,
                    "secure_consumer_privacy_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
