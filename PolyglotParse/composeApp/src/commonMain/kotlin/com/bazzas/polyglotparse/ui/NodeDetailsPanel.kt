package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.model.CodeEdge
import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.EdgeType

@Composable
fun NodeDetailsPanel(
    selectedNode: CodeNode?,
    allNodes: List<CodeNode>,
    edges: List<CodeEdge>,
    modifier: Modifier = Modifier
) {
    if (selectedNode == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Click a node to see details", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Node Details", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text("Name: ${selectedNode.name}", style = MaterialTheme.typography.bodyMedium)
                Text("Source Set: ${selectedNode.sourceSet.name}", style = MaterialTheme.typography.bodySmall)
                Text("Language: ${selectedNode.language}", style = MaterialTheme.typography.bodySmall)
                Text("Lines of Code: ${selectedNode.linesOfCode}", style = MaterialTheme.typography.bodySmall)
                Text("Complexity: ${selectedNode.complexity}", style = MaterialTheme.typography.bodySmall)

                if (selectedNode.isExpect) {
                    Text("✨ Expect declaration", color = Color(0xFFFFD700), style = MaterialTheme.typography.bodySmall)
                }
                if (selectedNode.isActual) {
                    Text("✨ Actual implementation", color = Color(0xFFFFD700), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Dependencies (nodes this depends on)
            item {
                Spacer(Modifier.height(8.dp))
                Text("Dependencies:", style = MaterialTheme.typography.labelMedium)
            }

            val dependencies = edges.filter {
                it.fromId == selectedNode.id && it.type == EdgeType.DEPENDS_ON
            }
            if (dependencies.isEmpty()) {
                item {
                    Text("None", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(dependencies) { edge ->
                    val targetNode = allNodes.firstOrNull { it.id == edge.toId }
                    if (targetNode != null) {
                        Text("→ ${targetNode.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Depended by (nodes that depend on this)
            item {
                Spacer(Modifier.height(8.dp))
                Text("Depended by:", style = MaterialTheme.typography.labelMedium)
            }

            val dependents = edges.filter {
                it.toId == selectedNode.id && it.type == EdgeType.DEPENDS_ON
            }
            if (dependents.isEmpty()) {
                item {
                    Text("None", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(dependents) { edge ->
                    val sourceNode = allNodes.firstOrNull { it.id == edge.fromId }
                    if (sourceNode != null) {
                        Text("← ${sourceNode.name}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Expect/Actual pairs
            val expectActualEdges = edges.filter {
                (it.fromId == selectedNode.id || it.toId == selectedNode.id) && it.type == EdgeType.EXPECT_ACTUAL
            }

            if (expectActualEdges.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Expect/Actual Pairs:", style = MaterialTheme.typography.labelMedium)
                }

                items(expectActualEdges) { edge ->
                    val pairNode = allNodes.firstOrNull {
                        it.id == if (edge.fromId == selectedNode.id) edge.toId else edge.fromId
                    }
                    if (pairNode != null) {
                        Text(
                            "↔ ${pairNode.name} (${pairNode.sourceSet.name})",
                            color = Color(0xFFFFD700),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
