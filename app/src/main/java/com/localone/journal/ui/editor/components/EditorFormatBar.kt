package com.localone.journal.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.localone.journal.R
import com.localone.journal.ui.theme.DayOneBlue

@Composable
fun EditorFormatBar(
    onAddPhotoClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onHeadingClick: () -> Unit,
    onTaskListClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onListClick: () -> Unit,
    onCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 插入照片
            IconButton(onClick = onAddPhotoClick) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "添加照片",
                    tint = DayOneBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // 粗体
            IconButton(onClick = onBoldClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_bold),
                    contentDescription = "粗体",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 斜体
            IconButton(onClick = onItalicClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_italic),
                    contentDescription = "斜体",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 标题
            IconButton(onClick = onHeadingClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_heading),
                    contentDescription = "大标题",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 待办清单
            IconButton(onClick = onTaskListClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_tasklist),
                    contentDescription = "待办列表",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 引用
            IconButton(onClick = onQuoteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_quote),
                    contentDescription = "引用",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 无序列表
            IconButton(onClick = onListClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_list),
                    contentDescription = "无序列表",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 代码块
            IconButton(onClick = onCodeClick) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "代码块",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
