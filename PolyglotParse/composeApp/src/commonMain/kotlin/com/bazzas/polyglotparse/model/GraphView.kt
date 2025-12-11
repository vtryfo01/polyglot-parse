package com.bazzas.polyglotparse.model

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun GraphView(
    laidOutGraph: LaidOutGraph,
    onNodeClick: (CodeNode) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF050810))
    ) {
        if (laidOutGraph.nodes.isEmpty()) {
            // helpful fallback text
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Kotlin/Java files found in this folder")
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                laidOutGraph.edges.forEach { edge ->
                    val from = laidOutGraph.nodes.firstOrNull { it.node.id == edge.fromId }
                    val to = laidOutGraph.nodes.firstOrNull { it.node.id == edge.toId }
                    if (from != null && to != null) {
                        drawLine(
                            color = Color(0xFF4444AA),
                            start = Offset(centerX + from.x, centerY + from.y),
                            end = Offset(centerX + to.x, centerY + to.y),
                            strokeWidth = 2f
                        )
                    }
                }

                laidOutGraph.nodes.forEach { pn ->
                    drawCircle(
                        color = when (pn.node.language) {
                            "Kotlin" -> Color(0xFF5CE1E6)
                            "Java" -> Color(0xFFFFC857)
                            else -> Color(0xFF9B5DE5)
                        },
                        radius = 10f,
                        center = Offset(centerX + pn.x, centerY + pn.y)
                    )
                }
            }
        }
    }
}
