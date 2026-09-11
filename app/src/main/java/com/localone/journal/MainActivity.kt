package com.localone.journal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localone.journal.ui.theme.LocalOneTheme
import com.localone.journal.ui.timeline.TimelineScreen
import com.localone.journal.ui.timeline.TimelineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LocalOneApp
        val repository = app.repository

        setContent {
            LocalOneTheme {
                val timelineViewModel: TimelineViewModel = viewModel {
                    TimelineViewModel(repository)
                }

                TimelineScreen(
                    viewModel = timelineViewModel,
                    onNavigateToEditor = { entryId ->
                        // TODO: 路由跳转至富文本编辑器
                    }
                )
            }
        }
    }
}
