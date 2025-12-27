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
import com.bazzas.polyglotparse.model.KmpLayout3D
import com.bazzas.polyglotparse.model.SolarSystemLayout
import com.bazzas.polyglotparse.model.GraphTheme
import com.bazzas.polyglotparse.model.GraphView
import com.bazzas.polyglotparse.model.SourceSet
import com.bazzas.polyglotparse.ui.CollapsibleDock
import com.bazzas.polyglotparse.ui.FloatingToolbar
import com.bazzas.polyglotparse.ui.GraphLegend
import com.bazzas.polyglotparse.ui.NodeDetailsPanel

@Composable
fun App(fileSystem: FileSystem) {
    MaterialTheme {
        var currentPath by remember { mutableStateOf(fileSystem.startPath) }
        var fileList by remember { mutableStateOf(emptyList<FileItem>()) }
        var selectedNode by remember { mutableStateOf<CodeNode?>(null) }
        var laidOutGraph by remember { mutableStateOf<LaidOutGraph?>(null) }
        var is3DMode by remember { mutableStateOf(true) }
        var isAutoRotate by remember { mutableStateOf(true) }  // Default ON
        var graphTheme by remember { mutableStateOf(GraphTheme.SOLAR_SYSTEM) }  // Default to Solar System
        var isDockExpanded by remember { mutableStateOf(true) }  // Dock visibility

        // Search and filter state
        var searchQuery by remember { mutableStateOf("") }
        var selectedLanguages by remember { mutableStateOf<Set<String>>(emptySet()) }
        var selectedSourceSets by remember { mutableStateOf<Set<SourceSet>>(emptySet()) }

        // per-file analyzer
        val fileAnalyzer = remember { SimpleAnalyzer() }
        // project-level analyzer + layouters
        val projectAnalyzer = remember { ProjectAnalyzer(fileSystem) }
        val kmpLayouter = remember { KmpLayout3D() }
        val solarLayouter = remember { SolarSystemLayout() }

        val scope = rememberCoroutineScope()

        // reload files whenever currentPath changes
        LaunchedEffect(currentPath) {
            fileList = fileSystem.getFiles(currentPath)
        }

        // NEW LAYOUT: Graph-first design with collapsible dock
        Box(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                // LEFT: Collapsible Dock
                CollapsibleDock(
                    isExpanded = isDockExpanded,
                    onToggle = { isDockExpanded = !isDockExpanded },
                    currentPath = currentPath,
                    fileList = fileList,
                    onNavigateUp = {
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
                    },
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            currentPath = file.path
                            selectedNode = null
                            laidOutGraph = null
                        } else {
                            scope.launch {
                                val content = fileSystem.readFile(file.path)
                                selectedNode = fileAnalyzer.analyze(content, file.path, file.name)
                                laidOutGraph = null
                            }
                        }
                    },
                    onAnalyzeProject = {
                        scope.launch {
                            val graph = projectAnalyzer.analyzeProject(currentPath)
                            laidOutGraph = if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
                                solarLayouter.layout(graph)
                            } else {
                                kmpLayouter.layout(graph)
                            }
                            selectedNode = null
                        }
                    },
                    onLoadDemo = null,  // TODO: Implement demo mode
                    nodes = laidOutGraph?.nodes?.map { it.node } ?: emptyList(),
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedLanguages = selectedLanguages,
                    onLanguageToggle = { lang ->
                        selectedLanguages = if (selectedLanguages.contains(lang)) {
                            selectedLanguages - lang
                        } else {
                            selectedLanguages + lang
                        }
                    },
                    selectedSourceSets = selectedSourceSets,
                    onSourceSetToggle = { sourceSet ->
                        selectedSourceSets = if (selectedSourceSets.contains(sourceSet)) {
                            selectedSourceSets - sourceSet
                        } else {
                            selectedSourceSets + sourceSet
                        }
                    },
                    showSearchFilters = laidOutGraph != null
                )

                // CENTER + RIGHT: Graph Canvas and Details Drawer
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    when {
                        laidOutGraph != null -> {
                            // Graph is loaded: show graph with overlays
                            val graph = laidOutGraph!!

                            // Compute filtered node IDs
                            val filteredNodeIds = graph.nodes
                                .map { it.node }
                                .filter { node ->
                                    val matchesSearch = searchQuery.isEmpty() ||
                                        node.name.contains(searchQuery, ignoreCase = true)
                                    val matchesLanguage = selectedLanguages.isEmpty() ||
                                        selectedLanguages.contains(node.language)
                                    val matchesSourceSet = selectedSourceSets.isEmpty() ||
                                        selectedSourceSets.contains(node.sourceSet)
                                    matchesSearch && matchesLanguage && matchesSourceSet
                                }
                                .map { it.id }
                                .toSet()

                            Row(Modifier.fillMaxSize()) {
                                // Graph Canvas
                                Box(Modifier.weight(if (selectedNode != null) 2f else 1f).fillMaxHeight()) {
                                    GraphView(
                                        laidOutGraph = graph,
                                        is3DMode = is3DMode,
                                        isAutoRotate = isAutoRotate,
                                        graphTheme = graphTheme,
                                        selectedNodeId = selectedNode?.id,
                                        filteredNodeIds = if (filteredNodeIds.size < graph.nodes.size) filteredNodeIds else null,
                                        onNodeClick = { node -> selectedNode = node }
                                    )

                                    // Floating toolbar overlay (top-left)
                                    FloatingToolbar(
                                        is3DMode = is3DMode,
                                        isAutoRotate = isAutoRotate,
                                        graphTheme = graphTheme,
                                        onToggle3DMode = { is3DMode = it },
                                        onToggleAutoRotate = { isAutoRotate = it },
                                        onToggleTheme = { theme ->
                                            graphTheme = theme
                                            scope.launch {
                                                val newGraph = projectAnalyzer.analyzeProject(currentPath)
                                                laidOutGraph = if (theme == GraphTheme.SOLAR_SYSTEM) {
                                                    solarLayouter.layout(newGraph)
                                                } else {
                                                    kmpLayouter.layout(newGraph)
                                                }
                                            }
                                        },
                                        onResetCamera = {
                                            val current = is3DMode
                                            is3DMode = !current
                                            is3DMode = current
                                        },
                                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                                    )

                                    // Legend overlay (bottom-right)
                                    GraphLegend(
                                        graphTheme = graphTheme,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                                    )
                                }

                                // Details Drawer (only when node selected)
                                if (selectedNode != null) {
                                    Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
                                    Box(
                                        Modifier.width(320.dp).fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Node Details", style = MaterialTheme.typography.titleMedium)
                                                IconButton(onClick = { selectedNode = null }) {
                                                    Text("✕")
                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            NodeDetailsPanel(
                                                selectedNode = selectedNode,
                                                allNodes = graph.nodes.map { it.node },
                                                edges = graph.edges
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        selectedNode != null -> {
                            // File analysis mode (no graph)
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    val node = selectedNode!!
                                    Text("File Analysis", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(Modifier.height(16.dp))
                                    Text("File: ${node.name}", style = MaterialTheme.typography.titleLarge)
                                    Text("Language: ${node.language}")
                                    Text("Lines of Code: ${node.linesOfCode}")
                                    Spacer(Modifier.height(16.dp))
                                    val color = if (node.complexity > 5) Color.Red else Color(0xFF00AA00)
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
                            }
                        }

                        else -> {
                            // Empty state
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Click 'Analyze' to visualize project structure",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
