package com.bazzas.polyglotparse.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SolarSystemLayout {
    fun layout(graph: CodeGraph): LaidOutGraph {
        if (graph.nodes.isEmpty()) return LaidOutGraph(emptyList(), emptyList())

        // Group nodes by source set
        val commonNodes = graph.nodes.filter { it.sourceSet == SourceSet.COMMON }
        val androidNodes = graph.nodes.filter { it.sourceSet == SourceSet.ANDROID }
        val iosNodes = graph.nodes.filter { it.sourceSet == SourceSet.IOS }
        val jvmNodes = graph.nodes.filter { it.sourceSet == SourceSet.JVM || it.sourceSet == SourceSet.DESKTOP }
        val unknownNodes = graph.nodes.filter { it.sourceSet == SourceSet.UNKNOWN }

        val positioned = mutableListOf<PositionedNode>()

        // Sun: commonMain at center (0,0,0) - slightly larger presence
        commonNodes.forEachIndexed { index, node ->
            // Place commonMain nodes in a tight cluster at center (the "Sun")
            val angle = (index * 2.0 * PI / commonNodes.size.coerceAtLeast(1)).toFloat()
            val radius = if (commonNodes.size > 1) 50f else 0f  // Tight cluster
            positioned += PositionedNode(
                node = node,
                x = cos(angle) * radius,
                y = sin(angle) * radius * 0.5f,  // Flatten vertically
                z = sin(angle) * radius * 0.5f
            )
        }

        // Orbital rings for platform source sets
        positioned += placeOnOrbit(androidNodes, orbitRadius = 250f, ringIndex = 0)
        positioned += placeOnOrbit(iosNodes, orbitRadius = 350f, ringIndex = 1)
        positioned += placeOnOrbit(jvmNodes, orbitRadius = 450f, ringIndex = 2)
        positioned += placeOnOrbit(unknownNodes, orbitRadius = 550f, ringIndex = 3)

        return LaidOutGraph(positioned, graph.edges)
    }

    /**
     * Place nodes on an orbital ring around the center.
     * Uses stable hash-based positioning so nodes don't jump around.
     */
    private fun placeOnOrbit(
        nodes: List<CodeNode>,
        orbitRadius: Float,
        ringIndex: Int
    ): List<PositionedNode> {
        if (nodes.isEmpty()) return emptyList()

        return nodes.mapIndexed { index, node ->
            // Stable angle based on node ID hash (won't change between renders)
            val hashAngle = node.id.hashCode().toFloat() / Int.MAX_VALUE * PI.toFloat()
            val indexAngle = (index * 2.0 * PI / nodes.size).toFloat()
            val angle = hashAngle + indexAngle  // Combine for even distribution with stability

            // Slight z-wobble for 3D parallax effect
            val zWobble = sin(hashAngle * 3.7f) * 80f  // Varies per node, creates depth

            PositionedNode(
                node = node,
                x = cos(angle) * orbitRadius,
                y = sin(angle) * orbitRadius,
                z = zWobble
            )
        }
    }
}
