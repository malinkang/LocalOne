package com.localone.journal

import android.app.Application
import com.localone.journal.data.local.LocalOneDatabase
import com.localone.journal.data.repository.JournalRepositoryImpl
import com.localone.journal.domain.repository.JournalRepository

class LocalOneApp : Application() {

    lateinit var database: LocalOneDatabase
        private set

    lateinit var repository: JournalRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = LocalOneDatabase.getInstance(this)
        repository = JournalRepositoryImpl(database.journalDao())
    }
}
