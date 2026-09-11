package com.localone.journal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.localone.journal.ui.editor.EditorScreen
import com.localone.journal.ui.editor.EditorViewModel
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
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "timeline"
                ) {
                    composable("timeline") {
                        val timelineViewModel: TimelineViewModel = viewModel {
                            TimelineViewModel(repository)
                        }

                        TimelineScreen(
                            viewModel = timelineViewModel,
                            onNavigateToEditor = { entryId ->
                                if (entryId != null && entryId > 0) {
                                    navController.navigate("editor?entryId=$entryId")
                                } else {
                                    navController.navigate("editor")
                                }
                            }
                        )
                    }

                    composable(
                        route = "editor?entryId={entryId}",
                        arguments = listOf(
                            navArgument("entryId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val entryIdStr = backStackEntry.arguments?.getString("entryId")
                        val entryId = entryIdStr?.toLongOrNull()

                        val editorViewModel: EditorViewModel = viewModel(
                            key = "editor_${entryId ?: "new"}"
                        ) {
                            EditorViewModel(repository, entryId)
                        }

                        EditorScreen(
                            viewModel = editorViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
