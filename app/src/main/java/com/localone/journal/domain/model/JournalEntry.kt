package com.localone.journal.domain.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

data class JournalEntry(
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val bookId: String = "default",
    val creationTime: Instant = Instant.now(),
    val modifiedTime: Instant = Instant.now(),
    val timeZone: String = ZoneId.systemDefault().id,
    val title: String? = null,
    val content: String = "",
    val previewSnippet: String = "",
    val photoUris: List<String> = emptyList(),
    val audioUris: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val location: EntryLocation? = null,
    val weather: EntryWeather? = null,
    val activity: EntryActivity? = null,
    val isStarred: Boolean = false,
    val isPinned: Boolean = false,
    val isDraft: Boolean = false,
    val isTrashed: Boolean = false
) {
    val localDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(creationTime, ZoneId.of(timeZone))

    val year: Int get() = localDateTime.year
    val month: Int get() = localDateTime.monthValue
    val dayOfMonth: Int get() = localDateTime.dayOfMonth
}
