package com.localone.journal.domain.model

import java.util.UUID

data class JournalBook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String = "#44C0FF",
    val isDefault: Boolean = false,
    val entryCount: Int = 0
)
