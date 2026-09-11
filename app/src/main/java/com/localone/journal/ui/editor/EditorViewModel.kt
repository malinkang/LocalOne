package com.localone.journal.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localone.journal.data.sensor.LocationHelper
import com.localone.journal.data.sensor.WeatherHelper
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
                        latitude = 40.0970,
                        longitude = 116.2942,
                        placeName = "当前位置",
                        localityName = "北京市",
                        country = "中国"
                    ),
                    weather = EntryWeather(
                        temperatureCelsius = 20.0,
                        weatherCode = "0",
                        conditionsDescription = "晴朗 ☀",
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

    fun refreshLocationAndWeather(context: Context) {
        viewModelScope.launch {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                _uiState.update { it.copy(location = loc) }
                val weather = WeatherHelper.fetchWeather(loc.latitude, loc.longitude)
                if (weather != null) {
                    _uiState.update { it.copy(weather = weather) }
                }
            }
        }
    }

    fun updateLocation(location: EntryLocation?) {
        _uiState.update { it.copy(location = location) }
    }

    fun updateLocationPlaceName(placeName: String) {
        val current = _uiState.value.location ?: EntryLocation(latitude = 40.097, longitude = 116.294)
        _uiState.update {
            it.copy(location = current.copy(placeName = placeName))
        }
    }

    fun updateWeather(weather: EntryWeather?) {
        _uiState.update { it.copy(weather = weather) }
    }

    fun updateWeatherDetails(temp: Double, desc: String) {
        val current = _uiState.value.weather ?: EntryWeather(temperatureCelsius = temp, conditionsDescription = desc, weatherCode = "0")
        _uiState.update {
            it.copy(weather = current.copy(temperatureCelsius = temp, conditionsDescription = desc))
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

            val entry = JournalEntry(
                id = state.entryId ?: 0,
                title = title.ifBlank { null },
                content = content,
                previewSnippet = content.take(150),
                photoUris = state.photos,
                creationTime = state.creationTime,
                modifiedTime = Instant.now(),
                location = state.location,
                weather = state.weather,
                bookId = state.bookId,
                isStarred = state.isStarred
            )

            repository.insertOrUpdateEntry(entry)

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
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
