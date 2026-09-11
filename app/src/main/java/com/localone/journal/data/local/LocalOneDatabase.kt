package com.localone.journal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.localone.journal.data.local.dao.JournalDao
import com.localone.journal.data.local.entity.JournalEntryEntity

@Database(
    entities = [JournalEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocalOneDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: LocalOneDatabase? = null

        fun getInstance(context: Context): LocalOneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalOneDatabase::class.java,
                    "local_one_encrypted.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
