package com.localone.journal.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.localone.journal.ui.editor.components.AztecRichEditor
import com.localone.journal.ui.editor.components.EditorMetadataHeader
import com.localone.journal.ui.editor.components.EditorTopBar
import com.localone.journal.ui.editor.components.LocationBottomSheet
import com.localone.journal.ui.editor.components.WeatherBottomSheet
import com.localone.journal.ui.theme.DayOneLightGray
import org.wordpress.aztec.AztecText

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var aztecTextRef by remember { mutableStateOf<AztecText?>(null) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showWeatherSheet by remember { mutableStateOf(false) }

    // 自动尝试获取真实物理位置和实时天气
    LaunchedEffect(Unit) {
        if (uiState.entryId == null || uiState.entryId!! <= 0) {
            viewModel.refreshLocationAndWeather(context)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it.toString())
        }
    }

    // 在 Aztec 当前光标处插入内容
    val insertContentAtCursor = { textToInsert: String ->
        aztecTextRef?.let { aztec ->
            val start = aztec.selectionStart.coerceAtLeast(0)
            val end = aztec.selectionEnd.coerceAtLeast(start)
            val prefix = if (start > 0 && aztec.text?.getOrNull(start - 1) != '\n') "\n" else ""
            val formatted = "$prefix$textToInsert\n"
            aztec.editableText.replace(start, end, formatted)
            aztec.setSelection((start + formatted.length).coerceAtMost(aztec.text?.length ?: 0))
            viewModel.onContentHtmlChanged(aztec.toHtml(false))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            EditorTopBar(
                isStarred = uiState.isStarred,
                isExistingEntry = uiState.entryId != null && uiState.entryId!! > 0,
                isSaving = uiState.isSaving,
                onSaveClick = {
                    val html = aztecTextRef?.toHtml(false)
                    viewModel.saveEntry(html)
                },
                onUndoClick = {
                    aztecTextRef?.undo()
                },
                onRedoClick = {
                    aztecTextRef?.redo()
                },
                onToggleStar = { viewModel.toggleStar() },
                onDeleteClick = { viewModel.deleteEntry() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .imePadding()
        ) {
            // 交互式元数据信息栏（可点击弹出位置、天气设置与一键插入）
            EditorMetadataHeader(
                creationTime = uiState.creationTime,
                location = uiState.location,
                weather = uiState.weather,
                onLocationClick = { showLocationSheet = true },
                onWeatherClick = { showWeatherSheet = true },
                onQuickInsertClick = {
                    val locSummary = uiState.location?.displaySummary
                    val weatherSummary = uiState.weather?.let { "%.0f°C ${it.conditionsDescription}".trim() }
                    val combined = listOfNotNull(
                        locSummary?.let { "📍 $it" },
                        weatherSummary?.let { "☀ $it" }
                    ).joinToString(" · ")
                    if (combined.isNotEmpty()) {
                        insertContentAtCursor(combined)
                    }
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // 照片多媒体排版区
            if (uiState.photos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.photos) { photoUri ->
                        Box(
                            modifier = Modifier
                                .size(width = 140.dp, height = 100.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "日记图片",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clickable { viewModel.removePhoto(photoUri) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "删除",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 大标题输入区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                if (uiState.title.isEmpty()) {
                    Text(
                        text = "标题（可选）",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayOneLightGray
                    )
                }
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Aztec 原生富文本编辑器与原装工具栏（1:1 对标 Day One）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AztecRichEditor(
                    initialHtml = uiState.contentHtml,
                    onContentChanged = { html ->
                        viewModel.onContentHtmlChanged(html)
                    },
                    onAddPhotoClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onEditorReady = { editor ->
                        aztecTextRef = editor
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // 位置管理 BottomSheet
    if (showLocationSheet) {
        LocationBottomSheet(
            location = uiState.location,
            onDismiss = { showLocationSheet = false },
            onRefreshLocation = { viewModel.refreshLocationAndWeather(context) },
            onUpdatePlaceName = { newName -> viewModel.updateLocationPlaceName(newName) },
            onInsertIntoContent = { textToInsert ->
                insertContentAtCursor(textToInsert)
            },
            onClearLocation = { viewModel.updateLocation(null) }
        )
    }

    // 天气管理 BottomSheet
    if (showWeatherSheet) {
        WeatherBottomSheet(
            weather = uiState.weather,
            onDismiss = { showWeatherSheet = false },
            onRefreshWeather = { viewModel.refreshLocationAndWeather(context) },
            onUpdateWeather = { temp, desc -> viewModel.updateWeatherDetails(temp, desc) },
            onInsertIntoContent = { textToInsert ->
                insertContentAtCursor(textToInsert)
            }
        )
    }
}
