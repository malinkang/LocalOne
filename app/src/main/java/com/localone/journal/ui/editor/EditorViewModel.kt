package com.localone.journal.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localone.journal.domain.model.EntryLocation
import com.localone.journal.domain.model.EntryWeather
import com.localone.journal.domain.model.JournalEntry
import com.localone.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class EditorViewModel(
    private val repository: JournalRepository,
    private val initialEntryId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState(entryId = initialEntryId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        loadEntry(initialEntryId)
    }

    private fun loadEntry(id: Long?) {
        if (id == null || id <= 0) {
            _uiState.update {
                it.copy(
                    creationTime = Instant.now(),
                    location = EntryLocation(
                        latitude = 39.9042,
                        longitude = 116.4074,
                        placeName = "当前位置",
                        localityName = "北京市",
                        country = "中国"
                    ),
                    weather = EntryWeather(
                        temperatureCelsius = 23.5,
                        weatherCode = "800",
                        conditionsDescription = "晴朗",
                        moonPhase = 0.45
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entry = repository.getEntryById(id)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        entryId = entry.id,
                        title = entry.title ?: "",
                        bodyValue = TextFieldValue(text = entry.content, selection = TextRange(entry.content.length)),
                        isStarred = entry.isStarred,
                        creationTime = entry.creationTime,
                        photos = entry.photoUris,
                        location = entry.location,
                        weather = entry.weather,
                        bookId = entry.bookId,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onBodyChange(newValue: TextFieldValue) {
        _uiState.update { it.copy(bodyValue = newValue) }
    }

    fun toggleStar() {
        _uiState.update { it.copy(isStarred = !it.isStarred) }
    }

    fun addPhoto(photoUri: String) {
        _uiState.update { it.copy(photos = it.photos + photoUri) }
    }

    fun removePhoto(photoUri: String) {
        _uiState.update { it.copy(photos = it.photos - photoUri) }
    }

    fun applyWrapFormatting(prefix: String, suffix: String) {
        val current = _uiState.value.bodyValue
        val text = current.text
        val selection = current.selection

        val newText: String
        val newSelection: TextRange

        if (selection.collapsed) {
            val cursor = selection.start
            val before = text.substring(0, cursor)
            val after = text.substring(cursor)
            newText = "$before$prefix$suffix$after"
            val newCursorPos = cursor + prefix.length
            newSelection = TextRange(newCursorPos)
        } else {
            val start = selection.min
            val end = selection.max
            val before = text.substring(0, start)
            val selectedText = text.substring(start, end)
            val after = text.substring(end)
            newText = "$before$prefix$selectedText$suffix$after"
            newSelection = TextRange(start + prefix.length, end + prefix.length)
        }

        _uiState.update { it.copy(bodyValue = TextFieldValue(newText, newSelection)) }
    }

    fun applyLinePrefixFormatting(prefix: String) {
        val current = _uiState.value.bodyValue
        val text = current.text
        val selection = current.selection
        val cursor = selection.start

        val lastNewline = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1

        val before = text.substring(0, lineStart)
        val after = text.substring(lineStart)
        val newText = "$before$prefix$after"
        val newCursor = cursor + prefix.length

        _uiState.update { it.copy(bodyValue = TextFieldValue(newText, TextRange(newCursor))) }
    }

    fun saveEntry() {
        val state = _uiState.value
        val title = state.title.trim()
        val body = state.bodyValue.text.trim()

        if (title.isEmpty() && body.isEmpty() && state.photos.isEmpty()) {
            _uiState.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val entryToSave = JournalEntry(
                id = state.entryId ?: 0L,
                bookId = state.bookId,
                title = title.ifEmpty { null },
                content = body,
                previewSnippet = body.take(120).replace("\n", " "),
                creationTime = state.creationTime,
                modifiedTime = Instant.now(),
                isStarred = state.isStarred,
                photoUris = state.photos,
                location = state.location,
                weather = state.weather
            )
            val id = repository.insertOrUpdateEntry(entryToSave)
            _uiState.update { it.copy(entryId = id, isSaving = false, isSaved = true) }
        }
    }

    fun deleteEntry() {
        val id = _uiState.value.entryId ?: return
        viewModelScope.launch {
            repository.deleteEntry(id)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
