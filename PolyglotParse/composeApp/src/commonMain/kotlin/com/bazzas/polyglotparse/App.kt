package com.bazzas.polyglotparse

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
import kotlinx.coroutines.launch
import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.core.FileSystem
import com.bazzas.polyglotparse.engine.SimpleAnalyzer
import com.bazzas.polyglotparse.engine.ProjectAnalyzer
import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.LaidOutGraph
import com.bazzas.polyglotparse.model.CircularLayouter
import com.bazzas.polyglotparse.model.GraphView

@Composable
fun App(fileSystem: FileSystem) {
    MaterialTheme {
        var currentPath by remember { mutableStateOf(fileSystem.startPath) }
        var fileList by remember { mutableStateOf(emptyList<FileItem>()) }
        var selectedNode by remember { mutableStateOf<CodeNode?>(null) }
        var laidOutGraph by remember { mutableStateOf<LaidOutGraph?>(null) }

        // per-file analyzer
        val fileAnalyzer = remember { SimpleAnalyzer() }
        // project-level analyzer + layouter
        val projectAnalyzer = remember { ProjectAnalyzer(fileSystem) }
        val layouter = remember { CircularLayouter() }

        val scope = rememberCoroutineScope()

        // reload files whenever currentPath changes
        LaunchedEffect(currentPath) {
            fileList = fileSystem.getFiles(currentPath)
        }

        Row(Modifier.fillMaxSize()) {

            // LEFT PANEL
            Column(Modifier.weight(1f).padding(16.dp)) {

                // path + "Analyze project" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Only show "Up" if we're not at the starting path
                        if (currentPath != fileSystem.startPath) {
                            TextButton(
                                onClick = {
                                    val trimmed = currentPath.trimEnd('/', '\\')
                                    val index = trimmed.lastIndexOfAny(charArrayOf('/', '\\'))

                                    val parent = if (index > 0) {
                                        trimmed.substring(0, index)
                                    } else {
                                        trimmed
                                    }

                                    if (parent.isNotBlank() && parent != currentPath) {
                                        currentPath = parent
                                        selectedNode = null
                                        laidOutGraph = null
                                    }
                                }
                            ) {
                                Text("⬆ Up")
                            }
                        }


                        Text(
                            text = "📂 $currentPath",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val graph = projectAnalyzer.analyzeProject(currentPath)
                                laidOutGraph = layouter.layout(graph)
                                selectedNode = null   // graph mode
                            }
                        }
                    ) {
                        Text("Analyze project")
                    }
                }


                Spacer(Modifier.height(8.dp))

                // file list
                LazyColumn {
                    items(fileList) { file ->
                        Button(
                            onClick = {
                                if (file.isDirectory) {
                                    currentPath = file.path
                                    selectedNode = null
                                    laidOutGraph = null
                                } else {
                                    scope.launch {
                                        val content = fileSystem.readFile(file.path)
                                        selectedNode = fileAnalyzer.analyze(
                                            content,
                                            file.path,
                                            file.name
                                        )
                                        laidOutGraph = null   // file mode
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Text(if (file.isDirectory) "📁 ${file.name}" else "📄 ${file.name}")
                        }
                    }
                }
            }

            // RIGHT PANEL
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    laidOutGraph != null -> {
                        Column(
                            Modifier.fillMaxSize()
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Project graph", style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = { laidOutGraph = null }) {
                                    Text("Close")
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Box(Modifier.weight(1f).fillMaxWidth()) {
                                GraphView(
                                    laidOutGraph = laidOutGraph!!,
                                    onNodeClick = { node ->
                                        selectedNode = node
                                    }
                                )
                            }
                        }
                    }


                    // otherwise show file analysis
                    selectedNode != null -> {
                        val node = selectedNode!!
                        Text("Analysis Result", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))

                        Text("File: ${node.name}", style = MaterialTheme.typography.titleLarge)
                        Text("Language: ${node.language}")
                        Text("Lines of Code: ${node.linesOfCode}")

                        Spacer(Modifier.height(16.dp))

                        val color =
                            if (node.complexity > 5) Color.Red else Color(0xFF00AA00)
                        Text(
                            "Complexity: ${node.complexity}",
                            color = color,
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(Modifier.height(8.dp))
                        if (node.complexity > 5) {
                            Text("⚠️ High Complexity detected!", color = Color.Red)
                        } else {
                            Text("✅ Simple and clean.", color = Color(0xFF00AA00))
                        }
                    }

                    else -> {
                        Text("Click a file or run Analyze project")
                    }
                }
            }
        }
    }
}
