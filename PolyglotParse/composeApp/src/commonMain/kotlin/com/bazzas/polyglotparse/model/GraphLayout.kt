package com.bazzas.polyglotparse.model
import kotlin.math.PI;
data class PositionedNode(
    val node: CodeNode,
    val x: Float,
    val y: Float
)

data class LaidOutGraph(
    val nodes: List<PositionedNode>,
    val edges: List<CodeEdge>
)

class CircularLayouter {
    fun layout(graph: CodeGraph): LaidOutGraph {
        if (graph.nodes.isEmpty()) return LaidOutGraph(emptyList(), emptyList())

        val radius = 300f
        val centerX = 0f
        val centerY = 0f
        val step = (2 * PI / graph.nodes.size).toFloat()

        val positioned = graph.nodes.mapIndexed { index, node ->
            val angle = step * index
            PositionedNode(
                node = node,
                x = centerX + radius * kotlin.math.cos(angle),
                y = centerY + radius * kotlin.math.sin(angle)
            )
        }

        return LaidOutGraph(positioned, graph.edges)
    }
}
