package com.localone.journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val bookId: String,
    val creationEpochMs: Long,
    val modifiedEpochMs: Long,
    val timeZone: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val title: String?,
    val content: String,
    val previewSnippet: String,
    val photoUrisJson: String,
    val audioUrisJson: String,
    val tagsJson: String,
    val locationJson: String?,
    val weatherJson: String?,
    val activityJson: String?,
    val isStarred: Boolean,
    val isPinned: Boolean,
    val isDraft: Boolean,
    val isTrashed: Boolean
)
