package com.bazzas.polyglotparse.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.SourceSet

@Composable
fun CollapsibleDock(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    currentPath: String,
    fileList: List<FileItem>,
    onNavigateUp: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onAnalyzeProject: () -> Unit,
    onLoadDemo: (() -> Unit)?,  // Null if demo not available yet
    // Search and filter state
    nodes: List<CodeNode>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    selectedSourceSets: Set<SourceSet>,
    onSourceSetToggle: (SourceSet) -> Unit,
    showSearchFilters: Boolean,  // Only show when graph is displayed
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Collapsible content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Column(
                Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Project Explorer",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Action buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAnalyzeProject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5CE1E6)
                        )
                    ) {
                        Text("Analyze", color = Color.Black)
                    }

                    onLoadDemo?.let { loadDemo ->
                        Button(
                            onClick = loadDemo,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700)
                            )
                        ) {
                            Text("Demo", color = Color.Black)
                        }
                    }
                }

                Divider()

                // Current path with up button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateUp) {
                        Text("⬆ Up")
                    }
                    Text(
                        text = "📂",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )

                // File list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fileList) { file ->
                        Button(
                            onClick = { onFileClick(file) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (file.isDirectory) "📁 ${file.name}" else "📄 ${file.name}",
                                maxLines = 1
                            )
                        }
                    }
                }

                // Search and filters (only when graph is displayed)
                if (showSearchFilters) {
                    Divider()
                    SearchFilterPanel(
                        nodes = nodes,
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        selectedLanguages = selectedLanguages,
                        onLanguageToggle = onLanguageToggle,
                        selectedSourceSets = selectedSourceSets,
                        onSourceSetToggle = onSourceSetToggle
                    )
                }
            }
        }

        // Toggle button (always visible)
        Box(
            Modifier
                .width(48.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            ) {
                Text(
                    text = if (isExpanded) "◀" else "▶",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
