package com.localone.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.localone.journal.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE isTrashed = 0 ORDER BY creationEpochMs DESC")
    fun getAllActiveEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isTrashed = 0 AND month = :month AND day = :day ORDER BY year DESC, creationEpochMs DESC")
    fun getOnThisDayEntries(month: Int, day: Int): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: JournalEntryEntity): Long

    @Query("UPDATE journal_entries SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarStatus(id: Long, isStarred: Boolean)

    @Query("UPDATE journal_entries SET isTrashed = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM journal_entries WHERE isTrashed = 0 AND (content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') ORDER BY creationEpochMs DESC")
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>
}
