package com.localone.journal.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localone.journal.domain.model.JournalEntry
import com.localone.journal.ui.theme.DayOneBlue
import com.localone.journal.ui.theme.DayOneLightGray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarMonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    entriesByDate: Map<LocalDate, List<JournalEntry>>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE))
    val today = LocalDate.now()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 月份导航条
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = monthTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 回到今天快捷按钮
                    TextButton(
                        onClick = onGoToToday,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "今天",
                            tint = DayOneBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "今天",
                            fontSize = 12.sp,
                            color = DayOneBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上月",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下月",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日期网格数据构建
            val firstOfMonth = currentMonth.atDay(1)
            val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // 周日为 0
            val daysInMonth = currentMonth.lengthOfMonth()

            val calendarDays = mutableListOf<CalendarDayInfo>()

            // 上月余天
            val prevMonth = currentMonth.minusMonths(1)
            val daysInPrevMonth = prevMonth.lengthOfMonth()
            for (i in (firstDayOfWeek - 1) downTo 0) {
                val date = prevMonth.atDay(daysInPrevMonth - i)
                calendarDays.add(CalendarDayInfo(date = date, isCurrentMonth = false))
            }

            // 当月天数
            for (day in 1..daysInMonth) {
                val date = currentMonth.atDay(day)
                calendarDays.add(CalendarDayInfo(date = date, isCurrentMonth = true))
            }

            // 下月填充至 7 的倍数
            val remaining = (7 - (calendarDays.size % 7)) % 7
            for (day in 1..remaining) {
                val date = currentMonth.plusMonths(1).atDay(day)
                calendarDays.add(CalendarDayInfo(date = date, isCurrentMonth = false))
            }

            // 绘制网格行
            calendarDays.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { dayInfo ->
                        val date = dayInfo.date
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val entries = entriesByDate[date] ?: emptyList()
                        val hasEntries = entries.isNotEmpty()

                        DayCell(
                            dayNumber = date.dayOfMonth,
                            isCurrentMonth = dayInfo.isCurrentMonth,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEntries = hasEntries,
                            entriesCount = entries.size,
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

private data class CalendarDayInfo(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Composable
private fun DayCell(
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    hasEntries: Boolean,
    entriesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        isSelected -> Color.White
        !isCurrentMonth -> DayOneLightGray.copy(alpha = 0.4f)
        isToday -> DayOneBlue
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(DayOneBlue)
                } else if (isToday) {
                    Modifier.border(1.dp, DayOneBlue, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            // Day One 标志性打卡小圆点
            if (hasEntries) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else DayOneBlue)
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
