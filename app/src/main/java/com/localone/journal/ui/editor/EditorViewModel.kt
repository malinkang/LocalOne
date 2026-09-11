package com.localone.journal.ui.editor

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
                        contentHtml = entry.content,
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

    fun onContentHtmlChanged(newHtml: String) {
        _uiState.update { it.copy(contentHtml = newHtml) }
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

    fun saveEntry(currentHtml: String? = null) {
        val state = _uiState.value
        val title = state.title.trim()
        val content = (currentHtml ?: state.contentHtml).trim()

        if (title.isEmpty() && content.isEmpty() && state.photos.isEmpty()) {
            _uiState.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // 剥离 HTML 标签生成纯文本摘要
            val plainText = android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            val entryToSave = JournalEntry(
                id = state.entryId ?: 0L,
                bookId = state.bookId,
                title = title.ifEmpty { null },
                content = content,
                previewSnippet = plainText.take(150).replace("\n", " "),
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
