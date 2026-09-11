package com.localone.journal.ui.editor

import com.localone.journal.domain.model.EntryLocation
import com.localone.journal.domain.model.EntryWeather
import java.time.Instant

data class EditorUiState(
    val entryId: Long? = null,
    val title: String = "",
    val contentHtml: String = "",
    val isStarred: Boolean = false,
    val creationTime: Instant = Instant.now(),
    val timeZone: String = java.time.ZoneId.systemDefault().id,
    val photos: List<String> = emptyList(),
    val location: EntryLocation? = null,
    val weather: EntryWeather? = null,
    val bookId: String = "default",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)
