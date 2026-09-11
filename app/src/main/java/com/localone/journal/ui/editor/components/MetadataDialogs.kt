package com.localone.journal.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localone.journal.domain.model.EntryLocation
import com.localone.journal.domain.model.EntryWeather
import com.localone.journal.ui.theme.DayOneBlue
import com.localone.journal.ui.theme.DayOneTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    location: EntryLocation?,
    onDismiss: () -> Unit,
    onRefreshLocation: () -> Unit,
    onUpdatePlaceName: (String) -> Unit,
    onInsertIntoContent: (String) -> Unit,
    onClearLocation: () -> Unit
) {
    var editPlaceName by remember(location?.displaySummary) {
        mutableStateOf(location?.placeName ?: location?.displaySummary ?: "")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 日记位置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRefreshLocation) {
                    Icon(Icons.Default.MyLocation, contentDescription = "重新定位", tint = DayOneTeal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 当前详细地址
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = location?.displaySummary ?: "未获取到位置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (location != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${location.country ?: ""} ${location.administrativeArea ?: ""} ${location.localityName ?: ""}".trim(),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "GPS: %.4f, %.4f".format(location.latitude, location.longitude),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 自定义地名输入
            OutlinedTextField(
                value = editPlaceName,
                onValueChange = {
                    editPlaceName = it
                    onUpdatePlaceName(it)
                },
                label = { Text("自定义地名 (例如: 星巴克咖啡、家里)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 核心功能：插入到正文大按钮
            Button(
                onClick = {
                    val textToInsert = editPlaceName.ifBlank { location?.displaySummary ?: "当前位置" }
                    onInsertIntoContent("📍 $textToInsert")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DayOneTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = Color(0xFF1C1C1E))
                Spacer(modifier = Modifier.width(8.dp))
                Text("插入位置到日记正文", fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
            }

            if (location != null) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        onClearLocation()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("移除日记位置", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherBottomSheet(
    weather: EntryWeather?,
    onDismiss: () -> Unit,
    onRefreshWeather: () -> Unit,
    onUpdateWeather: (Double, String) -> Unit,
    onInsertIntoContent: (String) -> Unit
) {
    var tempVal by remember(weather?.temperatureCelsius) {
        mutableStateOf(weather?.temperatureCelsius ?: 22.0)
    }
    var currentCondition by remember(weather?.conditionsDescription) {
        mutableStateOf(weather?.conditionsDescription ?: "晴朗 ☀")
    }

    val presetWeathers = listOf(
        "晴朗" to "☀",
        "多云" to "⛅",
        "阴天" to "☁",
        "小雨" to "🌧",
        "中大雨" to "🌧",
        "雷阵雨" to "⛈",
        "降雪" to "❄",
        "大风" to "🍃"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☀ 日记天气",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRefreshWeather) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新天气", tint = DayOneTeal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 当前天气概览
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "%.1f°C".format(tempVal),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentCondition,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 温度增减控制
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            tempVal -= 1.0
                            onUpdateWeather(tempVal, currentCondition)
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "降温")
                        }
                        IconButton(onClick = {
                            tempVal += 1.0
                            onUpdateWeather(tempVal, currentCondition)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "升温")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "选择天气状况",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 天气快捷点选网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetWeathers) { (desc, emoji) ->
                    val isSelected = currentCondition.contains(desc)
                    Surface(
                        color = if (isSelected) DayOneTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, DayOneTeal) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                currentCondition = "$desc $emoji"
                                onUpdateWeather(tempVal, currentCondition)
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 核心功能：插入到正文大按钮
            Button(
                onClick = {
                    onInsertIntoContent("${currentCondition.split(" ").lastOrNull() ?: "☀"} %.1f°C $currentCondition".trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DayOneTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFF1C1C1E))
                Spacer(modifier = Modifier.width(8.dp))
                Text("插入天气到日记正文", fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
            }
        }
    }
}
