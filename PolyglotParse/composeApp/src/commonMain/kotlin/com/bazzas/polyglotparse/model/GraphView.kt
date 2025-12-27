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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs

@Composable
fun GraphView(
    laidOutGraph: LaidOutGraph,
    is3DMode: Boolean = true,
    isAutoRotate: Boolean = false,
    graphTheme: GraphTheme = GraphTheme.DEFAULT,
    selectedNodeId: String? = null,
    filteredNodeIds: Set<String>? = null,
    onNodeClick: (CodeNode) -> Unit = {}
) {
    var camera by remember { mutableStateOf(Camera3D()) }
    var clickPosition by remember { mutableStateOf<Offset?>(null) }

    // Auto-rotate effect
    LaunchedEffect(isAutoRotate, is3DMode) {
        if (isAutoRotate && is3DMode) {
            while (true) {
                kotlinx.coroutines.delay(16) // ~60fps
                camera = camera.rotateBy(0f, 0.008f) // Slow yaw rotation
            }
        }
    }

    // Container with dark background and clipping - this defines the graph panel bounds
    // Background color depends on theme
    val backgroundColor = if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
        Color(0xFF000814)  // Deeper space black for Solar System
    } else {
        Color(0xFF050810)  // Default dark blue
    }

    Box(
        modifier = Modifier
            .fillMaxSize()  // Fill the parent container (graph panel only)
            .background(backgroundColor)  // Dark background owned by container
            .clip(RectangleShape)  // CRITICAL: prevent drawing outside this box
    ) {
        if (laidOutGraph.nodes.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No files found", color = Color.White)
            }
        } else {
            // Render the 3D graph - Canvas fills ONLY this container
            Canvas(
                modifier = Modifier
                    .fillMaxSize()  // Fill the container (not the window)
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
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f

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

                // Compute bounding box of projected nodes
                val padding = 48f
                val minX = projectedNodes.minOfOrNull { it.screenX } ?: 0f
                val maxX = projectedNodes.maxOfOrNull { it.screenX } ?: 0f
                val minY = projectedNodes.minOfOrNull { it.screenY } ?: 0f
                val maxY = projectedNodes.maxOfOrNull { it.screenY } ?: 0f
                val bbWidth = (maxX - minX).coerceAtLeast(1f)
                val bbHeight = (maxY - minY).coerceAtLeast(1f)
                val bbCenterX = (minX + maxX) / 2f
                val bbCenterY = (minY + maxY) / 2f

                // Auto-fit scale: fit all nodes with padding
                val scaleX = (canvasWidth - 2f * padding) / bbWidth
                val scaleY = (canvasHeight - 2f * padding) / bbHeight
                val autoScale = minOf(scaleX, scaleY).coerceIn(0.3f, 2.0f)

                // Auto-fit translation: center the bounding box
                val translateX = centerX - bbCenterX * autoScale
                val translateY = centerY - bbCenterY * autoScale

                // Handle click detection (with transform)
                clickPosition?.let { click ->
                    val clickThreshold = 30f
                    val clickedNode = projectedNodes.firstOrNull { pn ->
                        val nodeX = pn.screenX * autoScale + translateX
                        val nodeY = pn.screenY * autoScale + translateY
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

                // Solar System mode: Draw radial gradient background + vignette
                if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
                    // Radial gradient from center (bright) to edges (dark)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A1F3A),  // Lighter center
                                Color(0xFF000814)   // Dark edges (vignette)
                            ),
                            center = Offset(centerX, centerY),
                            radius = maxOf(canvasWidth, canvasHeight) * 0.7f
                        ),
                        center = Offset(centerX, centerY),
                        radius = maxOf(canvasWidth, canvasHeight)
                    )

                    // Draw faint orbit ring arcs at specific radii
                    val orbitRadii = listOf(250f, 350f, 450f, 550f)
                    orbitRadii.forEach { radius ->
                        val screenRadius = radius * autoScale
                        drawCircle(
                            color = Color(0xFF2A3F5F).copy(alpha = 0.15f),  // Very faint blue rings
                            radius = screenRadius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }

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

                        // Average depth for edge alpha - DRAMATIC depth fog
                        val avgDepth = (fromNode.depth + toNode.depth) / 2f
                        val normalizedDepth = (avgDepth - minDepth) / depthRange
                        // Exponential falloff: near edges bright, far edges almost invisible
                        var alpha = (1f - normalizedDepth).pow(2.5f) * 0.9f

                        // Dim edges if filtering is active or if not connected to selection
                        if (isFiltered) {
                            alpha *= 0.15f
                        } else if (selectedNodeId != null && !isConnectedToSelected) {
                            alpha *= 0.25f
                        }

                        val edgeColor = when (edge.type) {
                            EdgeType.EXPECT_ACTUAL -> Color(0xFFFFD700)  // Gold for expect-actual
                            EdgeType.DEPENDS_ON -> Color(0xFF4444AA)     // Blue for dependencies
                        }

                        // Apply auto-fit transform to edge coordinates
                        val fromX = fromNode.screenX * autoScale + translateX
                        val fromY = fromNode.screenY * autoScale + translateY
                        val toX = toNode.screenX * autoScale + translateX
                        val toY = toNode.screenY * autoScale + translateY

                        // Solar System mode: Draw curved "wormhole" arcs for expect/actual
                        if (graphTheme == GraphTheme.SOLAR_SYSTEM && edge.type == EdgeType.EXPECT_ACTUAL) {
                            // Create curved path for wormhole effect
                            val midX = (fromX + toX) / 2f
                            val midY = (fromY + toY) / 2f
                            val dx = toX - fromX
                            val dy = toY - fromY
                            val distance = sqrt(dx * dx + dy * dy)

                            // Control point offset perpendicular to line (creates arc)
                            val curvature = distance * 0.2f
                            val perpX = -dy / distance * curvature
                            val perpY = dx / distance * curvature
                            val controlX = midX + perpX
                            val controlY = midY + perpY

                            val path = Path().apply {
                                moveTo(fromX, fromY)
                                quadraticBezierTo(controlX, controlY, toX, toY)
                            }

                            // Draw glowing wormhole arc
                            drawPath(
                                path = path,
                                color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),  // Gold outer glow
                                style = Stroke(width = 8f)
                            )
                            drawPath(
                                path = path,
                                color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.9f),  // Bright white core
                                style = Stroke(width = 3f)
                            )
                        } else if (graphTheme == GraphTheme.SOLAR_SYSTEM && edge.type == EdgeType.DEPENDS_ON) {
                            // Very faint straight "constellation" lines for dependencies
                            drawLine(
                                color = edgeColor.copy(alpha = alpha * 0.3f),  // Much fainter
                                start = Offset(fromX, fromY),
                                end = Offset(toX, toY),
                                strokeWidth = 1f
                            )
                        } else {
                            // Default mode: standard straight lines
                            drawLine(
                                color = edgeColor.copy(alpha = alpha),
                                start = Offset(fromX, fromY),
                                end = Offset(toX, toY),
                                strokeWidth = if (edge.type == EdgeType.EXPECT_ACTUAL) 4f else 2f
                            )
                        }
                    }
                }

                // Draw nodes (back to front, already sorted by perspectiveProject)
                projectedNodes.forEach { pn ->
                    val normalizedDepth = (pn.depth - minDepth) / depthRange
                    // DRAMATIC depth fog: near nodes BRILLIANT, far nodes nearly invisible
                    // Exponential falloff for strong 3D perception
                    var alpha = (1f - normalizedDepth).pow(2.0f)

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
                        alpha *= 0.15f
                    } else if (selectedNodeId != null && !isSelected && !isNeighbor) {
                        alpha *= 0.35f  // Dim non-selected, non-neighbor nodes
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

                    // Apply auto-fit transform to node coordinates
                    val nodeX = pn.screenX * autoScale + translateX
                    val nodeY = pn.screenY * autoScale + translateY
                    val radius = 12f * pn.apparentSize * sizeMultiplier * autoScale

                    // Solar System mode: Enhanced planet rendering
                    if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
                        val isSun = pn.node.sourceSet == SourceSet.COMMON

                        if (isSun) {
                            // Sun: Pulsing glow effect (use time-based animation)
                            val currentTime = System.currentTimeMillis()
                            val pulsePhase = (currentTime % 2000) / 2000f  // 2-second pulse cycle
                            val pulseIntensity = 0.8f + 0.2f * sin(pulsePhase * 2f * PI.toFloat())

                            // Multi-layer glow for Sun
                            drawCircle(
                                color = baseColor.copy(alpha = alpha * 0.3f * pulseIntensity),
                                radius = radius * 4.0f,
                                center = Offset(nodeX, nodeY)
                            )
                            drawCircle(
                                color = baseColor.copy(alpha = alpha * 0.5f * pulseIntensity),
                                radius = radius * 2.5f,
                                center = Offset(nodeX, nodeY)
                            )
                            // Bright Sun core
                            drawCircle(
                                color = Color.White.copy(alpha = alpha * 0.9f),
                                radius = radius * 1.2f,
                                center = Offset(nodeX, nodeY)
                            )
                            drawCircle(
                                color = baseColor.copy(alpha = alpha),
                                radius = radius,
                                center = Offset(nodeX, nodeY)
                            )
                        } else {
                            // Planets: Specular highlight + soft halo
                            val glowMultiplier = if (isSelected) 3.0f else if (isNeighbor) 2.5f else 2.0f

                            // Soft outer halo
                            drawCircle(
                                color = baseColor.copy(alpha = alpha * 0.3f),
                                radius = radius * glowMultiplier,
                                center = Offset(nodeX, nodeY)
                            )

                            // Planet body
                            drawCircle(
                                color = baseColor.copy(alpha = alpha),
                                radius = radius,
                                center = Offset(nodeX, nodeY)
                            )

                            // Specular highlight (top-left of planet)
                            val highlightOffsetX = -radius * 0.3f
                            val highlightOffsetY = -radius * 0.3f
                            drawCircle(
                                color = Color.White.copy(alpha = alpha * 0.6f),
                                radius = radius * 0.4f,
                                center = Offset(nodeX + highlightOffsetX, nodeY + highlightOffsetY)
                            )
                        }
                    } else {
                        // Default mode: standard glow + solid circle
                        val glowMultiplier = if (isSelected) 2.5f else if (isNeighbor) 2.0f else 1.8f
                        drawCircle(
                            color = baseColor.copy(alpha = alpha * 0.4f),
                            radius = radius * glowMultiplier,
                            center = Offset(nodeX, nodeY)
                        )
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
}
