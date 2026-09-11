package com.localone.journal.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localone.journal.domain.model.JournalEntry
import com.localone.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllEntries().collect { entries ->
                val grouped = entries.groupBy { it.localDateTime.toLocalDate() }
                val stats = calculateStats(grouped, entries.size)
                _uiState.update {
                    it.copy(
                        entriesByDate = grouped,
                        stats = stats,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateStats(grouped: Map<LocalDate, List<JournalEntry>>, totalEntries: Int): CalendarStats {
        val dates = grouped.keys.sorted()
        val totalActiveDays = dates.size

        if (dates.isEmpty()) {
            return CalendarStats(0, 0, 0, 0)
        }

        val dateSet = grouped.keys
        val today = LocalDate.now()

        // 计算当前连续天数 (Current Streak)
        var currentStreak = 0
        var checkDate = if (dateSet.contains(today)) today else today.minusDays(1)
        while (dateSet.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // 计算历史最长连续天数 (Longest Streak)
        var longestStreak = 0
        var runningStreak = 0
        var prevDate: LocalDate? = null

        for (date in dates) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > longestStreak) {
                longestStreak = runningStreak
            }
            prevDate = date
        }

        return CalendarStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            totalActiveDays = totalActiveDays,
            totalEntries = totalEntries
        )
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentMonth = YearMonth.from(date)
            )
        }
    }

    fun previousMonth() {
        _uiState.update {
            val prev = it.currentMonth.minusMonths(1)
            it.copy(currentMonth = prev)
        }
    }

    fun nextMonth() {
        _uiState.update {
            val next = it.currentMonth.plusMonths(1)
            it.copy(currentMonth = next)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDate = today,
                currentMonth = YearMonth.from(today)
            )
        }
    }

    fun toggleStar(entry: JournalEntry) {
        viewModelScope.launch {
            repository.toggleStar(entry.id, !entry.isStarred)
        }
    }
}
