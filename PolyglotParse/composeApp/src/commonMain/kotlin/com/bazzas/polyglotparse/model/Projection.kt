package com.bazzas.polyglotparse.model

import kotlin.math.cos
import kotlin.math.sin

data class ProjectedNode(
    val node: CodeNode,
    val screenX: Float,
    val screenY: Float,
    val depth: Float,          // Distance from camera (for z-sorting)
    val apparentSize: Float    // Size multiplier based on depth (1.0 = normal, <1.0 = farther, >1.0 = closer)
)

/**
 * Projects 3D nodes onto 2D screen space with perspective.
 * Returns nodes sorted by depth (farthest first for correct rendering order).
 */
fun perspectiveProject(
    nodes: List<PositionedNode>,
    camera: Camera3D
): List<ProjectedNode> {
    if (nodes.isEmpty()) return emptyList()

    val focalLength = 1200f  // STRONG perspective for dramatic 3D effect
    val cameraDistance = 700f  // Camera distance for optimal depth perception

    val projected = nodes.map { pn ->
        var x = pn.x
        var y = pn.y
        var z = pn.z

        // 1. Apply camera rotation (Euler angles: yaw around Y, then pitch around X)
        // Rotate around Y-axis (yaw)
        val cosYaw = cos(camera.yaw)
        val sinYaw = sin(camera.yaw)
        val xRotY = x * cosYaw - z * sinYaw
        val zRotY = x * sinYaw + z * cosYaw

        // Rotate around X-axis (pitch)
        val cosPitch = cos(camera.pitch)
        val sinPitch = sin(camera.pitch)
        val yRotX = y * cosPitch - zRotY * sinPitch
        val zRotX = y * sinPitch + zRotY * cosPitch

        // 2. Apply camera pan (clamped)
        val maxPan = 200f
        val xPanned = xRotY + camera.panX.coerceIn(-maxPan, maxPan)
        val yPanned = yRotX + camera.panY.coerceIn(-maxPan, maxPan)

        // 3. Calculate depth (z after rotation + camera distance)
        // NO huge constant offsets - let the natural z-range shine through
        val depth = zRotX + cameraDistance

        // 4. Perspective projection with strong falloff
        // Prevent division by zero or negative depth
        val safeDepth = if (depth > 50f) depth else 50f

        val screenX = (xPanned * focalLength) / safeDepth
        val screenY = (yPanned * focalLength) / safeDepth

        // 5. Calculate apparent size based on depth (closer = DRAMATICALLY bigger)
        // EXTREME scaling for undeniable 3D: near nodes 5x bigger, far nodes microscopic
        val apparentSize = (focalLength / safeDepth) * camera.zoom * 2.5f

        ProjectedNode(
            node = pn.node,
            screenX = screenX,
            screenY = screenY,
            depth = depth,
            apparentSize = apparentSize.coerceIn(0.15f, 5.0f)  // EXTREME range for WOW factor
        )
    }

    // Sort by depth (farthest first = render first, so closer nodes draw on top)
    return projected.sortedBy { it.depth }
}
