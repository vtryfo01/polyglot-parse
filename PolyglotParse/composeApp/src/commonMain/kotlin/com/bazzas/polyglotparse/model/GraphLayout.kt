package com.bazzas.polyglotparse.model
import kotlin.math.PI

data class PositionedNode(
    val node: CodeNode,
    val x: Float,
    val y: Float,
    val z: Float = 0f
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
                y = centerY + radius * kotlin.math.sin(angle),
                z = 0f
            )
        }

        return LaidOutGraph(positioned, graph.edges)
    }
}

class KmpLayout3D {
    fun layout(graph: CodeGraph): LaidOutGraph {
        if (graph.nodes.isEmpty()) return LaidOutGraph(emptyList(), emptyList())

        // Group nodes by source set
        val commonNodes = graph.nodes.filter { it.sourceSet == SourceSet.COMMON }
        val androidNodes = graph.nodes.filter { it.sourceSet == SourceSet.ANDROID }
        val iosNodes = graph.nodes.filter { it.sourceSet == SourceSet.IOS }
        val jvmNodes = graph.nodes.filter { it.sourceSet == SourceSet.JVM || it.sourceSet == SourceSet.DESKTOP }
        val unknownNodes = graph.nodes.filter { it.sourceSet == SourceSet.UNKNOWN }

        val positioned = mutableListOf<PositionedNode>()

        // Layer 0: commonMain (center, inner sphere)
        positioned += distributeOnSphere(commonNodes, radius = 200f, phiOffset = 0f)

        // Layer 1: androidMain (outer sphere, front-right quadrant)
        positioned += distributeOnSphere(androidNodes, radius = 350f, phiOffset = 0f)

        // Layer 2: iosMain (outer sphere, front-left quadrant)
        positioned += distributeOnSphere(iosNodes, radius = 350f, phiOffset = PI.toFloat())

        // Layer 3: jvmMain (outer sphere, back quadrant)
        positioned += distributeOnSphere(jvmNodes, radius = 350f, phiOffset = PI.toFloat() / 2f)

        // Unknown nodes: place in flat ring (fallback)
        positioned += distributeOnSphere(unknownNodes, radius = 450f, phiOffset = -PI.toFloat() / 2f)

        return LaidOutGraph(positioned, graph.edges)
    }

    /**
     * Distributes nodes evenly on the surface of a sphere.
     * Uses spherical coordinates (θ, φ) and converts to Cartesian (x, y, z).
     */
    private fun distributeOnSphere(
        nodes: List<CodeNode>,
        radius: Float,
        phiOffset: Float
    ): List<PositionedNode> {
        if (nodes.isEmpty()) return emptyList()

        val count = nodes.size
        val goldenAngle = PI * (3.0 - kotlin.math.sqrt(5.0))  // ~137.5 degrees for even distribution

        return nodes.mapIndexed { i, node ->
            // Use Fibonacci sphere distribution for even spacing
            val y = 1 - (i / (count - 1f).coerceAtLeast(1f)) * 2  // y from 1 to -1
            val radiusAtY = kotlin.math.sqrt(1 - y * y)

            val theta = goldenAngle * i + phiOffset
            val x = kotlin.math.cos(theta.toFloat()) * radiusAtY
            val z = kotlin.math.sin(theta.toFloat()) * radiusAtY

            PositionedNode(
                node = node,
                x = x * radius,
                y = y.toFloat() * radius,
                z = z * radius
            )
        }
    }
}
