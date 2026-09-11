package com.localone.journal.ui.calendar

import com.localone.journal.domain.model.JournalEntry
import java.time.LocalDate
import java.time.YearMonth

data class CalendarStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalActiveDays: Int = 0,
    val totalEntries: Int = 0
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val entriesByDate: Map<LocalDate, List<JournalEntry>> = emptyMap(),
    val stats: CalendarStats = CalendarStats(),
    val isLoading: Boolean = false
) {
    val selectedDateEntries: List<JournalEntry>
        get() = entriesByDate[selectedDate] ?: emptyList()
}
