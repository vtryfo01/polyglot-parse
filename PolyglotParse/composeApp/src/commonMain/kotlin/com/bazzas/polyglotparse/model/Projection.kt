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

    val focalLength = 600f  // Controls perspective strength
    val cameraDistance = 800f  // How far back the camera sits

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

        // 2. Apply camera pan
        val xPanned = xRotY + camera.panX
        val yPanned = yRotX + camera.panY

        // 3. Calculate depth (z after rotation + camera distance)
        val depth = zRotX + cameraDistance

        // 4. Perspective projection
        // Prevent division by zero or negative depth
        val safeDepth = if (depth > 10f) depth else 10f

        val screenX = (xPanned * focalLength) / safeDepth
        val screenY = (yPanned * focalLength) / safeDepth

        // 5. Calculate apparent size based on depth (closer = bigger)
        // Size scales from 0.5x (far) to 1.5x (close)
        val apparentSize = (focalLength / safeDepth) * camera.zoom * 1.2f

        ProjectedNode(
            node = pn.node,
            screenX = screenX,
            screenY = screenY,
            depth = depth,
            apparentSize = apparentSize.coerceIn(0.3f, 2.5f)
        )
    }

    // Sort by depth (farthest first = render first, so closer nodes draw on top)
    return projected.sortedBy { it.depth }
}
