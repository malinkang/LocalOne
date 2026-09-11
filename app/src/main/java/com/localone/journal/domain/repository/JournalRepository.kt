package com.localone.journal.domain.repository

import com.localone.journal.domain.model.JournalBook
import com.localone.journal.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>
    fun getOnThisDayEntries(month: Int, day: Int): Flow<List<JournalEntry>>
    suspend fun getEntryById(id: Long): JournalEntry?
    suspend fun insertOrUpdateEntry(entry: JournalEntry): Long
    suspend fun deleteEntry(id: Long)
    suspend fun toggleStar(id: Long, isStarred: Boolean)
    fun searchEntries(query: String): Flow<List<JournalEntry>>
    fun getAllBooks(): Flow<List<JournalBook>>
}
