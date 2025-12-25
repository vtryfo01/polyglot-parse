package com.bazzas.polyglotparse.model

import kotlin.math.PI

data class Camera3D(
    val pitch: Float = 30f * (PI / 180f).toFloat(), // Rotation around X-axis (degrees to radians)
    val yaw: Float = 45f * (PI / 180f).toFloat(),   // Rotation around Y-axis (degrees to radians)
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f
) {
    fun rotateBy(deltaPitch: Float, deltaYaw: Float): Camera3D {
        // Limit pitch to avoid gimbal lock (±80 degrees)
        val maxPitch = 80f * (PI / 180f).toFloat()
        val newPitch = (pitch + deltaPitch).coerceIn(-maxPitch, maxPitch)

        return copy(
            pitch = newPitch,
            yaw = yaw + deltaYaw
        )
    }

    fun zoomBy(factor: Float): Camera3D {
        // Clamp zoom between 0.5x and 3.0x
        val newZoom = (zoom * factor).coerceIn(0.5f, 3.0f)
        return copy(zoom = newZoom)
    }

    fun panBy(dx: Float, dy: Float): Camera3D {
        return copy(
            panX = panX + dx,
            panY = panY + dy
        )
    }
}
