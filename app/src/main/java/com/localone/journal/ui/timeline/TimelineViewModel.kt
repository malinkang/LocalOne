package com.localone.journal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localone.journal.domain.model.JournalEntry
import com.localone.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val today = LocalDate.now()

    val uiState: StateFlow<TimelineUiState> = combine(
        repository.getAllEntries(),
        repository.getOnThisDayEntries(today.monthValue, today.dayOfMonth),
        _searchQuery
    ) { allEntries, onThisDay, query ->
        val filtered = if (query.isBlank()) {
            allEntries
        } else {
            allEntries.filter {
                it.content.contains(query, ignoreCase = true) ||
                (it.title?.contains(query, ignoreCase = true) == true) ||
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
        TimelineUiState(
            isLoading = false,
            entries = filtered,
            onThisDayEntries = onThisDay.filter { it.year != today.year },
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleStar(entry: JournalEntry) {
        viewModelScope.launch {
            repository.toggleStar(entry.id, !entry.isStarred)
        }
    }

    fun createSampleEntry() {
        viewModelScope.launch {
            val sample = JournalEntry(
                title = "开始使用 LocalOne 日记",
                content = "# 启程\n\n今天搭建了完全本地的 Day One 风格日记！所有数据加密存储在手机内部，没有私有云上传，彻底掌握个人隐私。\n\n- [x] 搭建本地数据库与架构\n- [x] 1:1 对齐 Day One 经典视觉设计\n- [ ] 体验全套离线 Markdown 编辑与导出",
                tags = listOf("随笔", "里程碑"),
                location = com.localone.journal.domain.model.EntryLocation(
                    latitude = 39.9042,
                    longitude = 116.4074,
                    placeName = "北京",
                    localityName = "东城区"
                ),
                weather = com.localone.journal.domain.model.EntryWeather(
                    temperatureCelsius = 22.5,
                    conditionsDescription = "晴朗",
                    weatherCode = "sunny",
                    moonPhase = 0.5
                ),
                isStarred = true
            )
            repository.insertOrUpdateEntry(sample)
        }
    }
}
