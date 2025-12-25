package com.bazzas.polyglotparse.model

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt

@Composable
fun GraphView(
    laidOutGraph: LaidOutGraph,
    is3DMode: Boolean = true,
    selectedNodeId: String? = null,
    filteredNodeIds: Set<String>? = null,
    onNodeClick: (CodeNode) -> Unit = {}
) {
    var camera by remember { mutableStateOf(Camera3D()) }
    var clickPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF050810))
            .pointerInput(Unit) {
                detectDragGestures { change: PointerInputChange, dragAmount ->
                    change.consume()
                    // Convert drag to camera rotation
                    val sensitivity = 0.005f
                    val deltaYaw = dragAmount.x * sensitivity
                    val deltaPitch = -dragAmount.y * sensitivity  // Invert Y for natural rotation
                    camera = camera.rotateBy(deltaPitch, deltaYaw)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    clickPosition = offset
                }
            }
    ) {
        if (laidOutGraph.nodes.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No files found", color = Color.White)
            }
        } else {
            // Render the 3D graph
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Project nodes to 2D with perspective
                val projectedNodes = if (is3DMode) {
                    perspectiveProject(laidOutGraph.nodes, camera)
                } else {
                    // 2D mode: flatten to z=0
                    laidOutGraph.nodes.map { pn ->
                        ProjectedNode(
                            node = pn.node,
                            screenX = pn.x,
                            screenY = pn.y,
                            depth = 500f,
                            apparentSize = 1.0f
                        )
                    }
                }

                // Handle click detection
                clickPosition?.let { click ->
                    val clickThreshold = 30f
                    val clickedNode = projectedNodes.firstOrNull { pn ->
                        val nodeX = centerX + pn.screenX
                        val nodeY = centerY + pn.screenY
                        val dx = click.x - nodeX
                        val dy = click.y - nodeY
                        val distance = sqrt(dx * dx + dy * dy)
                        distance < clickThreshold * pn.apparentSize
                    }
                    if (clickedNode != null) {
                        onNodeClick(clickedNode.node)
                    }
                    clickPosition = null  // Reset after processing
                }

                // Find neighbors of selected node
                val neighborIds = if (selectedNodeId != null) {
                    val connectedEdges = laidOutGraph.edges.filter {
                        it.fromId == selectedNodeId || it.toId == selectedNodeId
                    }
                    connectedEdges.map { edge ->
                        if (edge.fromId == selectedNodeId) edge.toId else edge.fromId
                    }.toSet()
                } else {
                    emptySet()
                }

                // Find min/max depth for normalization
                val minDepth = projectedNodes.minOfOrNull { it.depth } ?: 1f
                val maxDepth = projectedNodes.maxOfOrNull { it.depth } ?: 1000f
                val depthRange = (maxDepth - minDepth).coerceAtLeast(1f)

                // Draw edges first (back to front)
                laidOutGraph.edges.forEach { edge ->
                    val fromNode = projectedNodes.firstOrNull { it.node.id == edge.fromId }
                    val toNode = projectedNodes.firstOrNull { it.node.id == edge.toId }

                    if (fromNode != null && toNode != null) {
                        // Check if this edge is connected to selected node
                        val isConnectedToSelected = selectedNodeId != null &&
                            (edge.fromId == selectedNodeId || edge.toId == selectedNodeId)

                        // Apply filtering
                        val isFiltered = filteredNodeIds != null &&
                            (!filteredNodeIds.contains(edge.fromId) || !filteredNodeIds.contains(edge.toId))

                        // Average depth for edge alpha
                        val avgDepth = (fromNode.depth + toNode.depth) / 2f
                        val normalizedDepth = (avgDepth - minDepth) / depthRange
                        var alpha = 0.3f + (1f - normalizedDepth) * 0.7f  // Closer = more opaque

                        // Dim edges if filtering is active or if not connected to selection
                        if (isFiltered) {
                            alpha *= 0.2f
                        } else if (selectedNodeId != null && !isConnectedToSelected) {
                            alpha *= 0.3f
                        }

                        val edgeColor = when (edge.type) {
                            EdgeType.EXPECT_ACTUAL -> Color(0xFFFFD700)  // Gold for expect-actual
                            EdgeType.DEPENDS_ON -> Color(0xFF4444AA)     // Blue for dependencies
                        }

                        drawLine(
                            color = edgeColor.copy(alpha = alpha),
                            start = Offset(centerX + fromNode.screenX, centerY + fromNode.screenY),
                            end = Offset(centerX + toNode.screenX, centerY + toNode.screenY),
                            strokeWidth = if (edge.type == EdgeType.EXPECT_ACTUAL) 4f else 2f
                        )
                    }
                }

                // Draw nodes (back to front, already sorted by perspectiveProject)
                projectedNodes.forEach { pn ->
                    val normalizedDepth = (pn.depth - minDepth) / depthRange
                    var alpha = 0.4f + (1f - normalizedDepth) * 0.6f  // Closer = more opaque

                    // Determine if node is selected, neighbor, or other
                    val isSelected = pn.node.id == selectedNodeId
                    val isNeighbor = neighborIds.contains(pn.node.id)
                    val isFiltered = filteredNodeIds != null && !filteredNodeIds.contains(pn.node.id)

                    // Size multiplier based on selection state
                    val sizeMultiplier = when {
                        isSelected -> 1.8f
                        isNeighbor -> 1.3f
                        else -> 1.0f
                    }

                    // Adjust alpha based on selection and filtering
                    if (isFiltered) {
                        alpha *= 0.2f
                    } else if (selectedNodeId != null && !isSelected && !isNeighbor) {
                        alpha *= 0.4f  // Dim non-selected, non-neighbor nodes
                    }

                    // Color by source set
                    val baseColor = when (pn.node.sourceSet) {
                        SourceSet.COMMON -> Color(0xFF5CE1E6)      // Cyan
                        SourceSet.ANDROID -> Color(0xFF00E676)     // Green
                        SourceSet.IOS -> Color(0xFFFF9800)         // Orange
                        SourceSet.JVM, SourceSet.DESKTOP -> Color(0xFF9C27B0)  // Purple
                        SourceSet.UNKNOWN -> when (pn.node.language) {
                            "Kotlin" -> Color(0xFF5CE1E6)
                            "Java" -> Color(0xFFFFC857)
                            else -> Color(0xFF9B5DE5)
                        }
                    }

                    val nodeX = centerX + pn.screenX
                    val nodeY = centerY + pn.screenY
                    val radius = 12f * pn.apparentSize * sizeMultiplier

                    // Draw glow effect (outer circle) - stronger for selected/neighbors
                    val glowMultiplier = if (isSelected) 2.5f else if (isNeighbor) 2.0f else 1.8f
                    drawCircle(
                        color = baseColor.copy(alpha = alpha * 0.4f),
                        radius = radius * glowMultiplier,
                        center = Offset(nodeX, nodeY)
                    )

                    // Draw solid node
                    drawCircle(
                        color = baseColor.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(nodeX, nodeY)
                    )
                }
            }
        }
    }
}
