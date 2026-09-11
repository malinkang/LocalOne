package com.localone.journal.ui.timeline

import com.localone.journal.domain.model.JournalEntry

data class TimelineUiState(
    val isLoading: Boolean = false,
    val entries: List<JournalEntry> = emptyList(),
    val onThisDayEntries: List<JournalEntry> = emptyList(),
    val searchQuery: String = "",
    val activeFilterTag: String? = null
)
