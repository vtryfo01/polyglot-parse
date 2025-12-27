package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.model.GraphTheme

@Composable
fun FloatingToolbar(
    is3DMode: Boolean,
    isAutoRotate: Boolean,
    graphTheme: GraphTheme,
    onToggle3DMode: (Boolean) -> Unit,
    onToggleAutoRotate: (Boolean) -> Unit,
    onToggleTheme: (GraphTheme) -> Unit,
    onResetCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pill-shaped floating toolbar with glassy background
    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1A1F3A).copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2D/3D Toggle
        CompactButton(
            text = if (is3DMode) "3D" else "2D",
            color = if (is3DMode) Color(0xFF5CE1E6) else Color(0xFF4444AA),
            onClick = { onToggle3DMode(!is3DMode) }
        )

        // Auto-rotate Toggle (only in 3D mode)
        if (is3DMode) {
            CompactButton(
                text = if (isAutoRotate) "⟳" else "◯",
                color = if (isAutoRotate) Color(0xFFFF9800) else Color(0xFF666666),
                onClick = { onToggleAutoRotate(!isAutoRotate) }
            )
        }

        // Reset Camera
        CompactButton(
            text = "↻",
            color = Color(0xFF666666),
            onClick = onResetCamera
        )

        // Vertical divider
        Box(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )

        // Theme Toggle
        CompactButton(
            text = if (graphTheme == GraphTheme.SOLAR_SYSTEM) "☀" else "◈",
            color = if (graphTheme == GraphTheme.SOLAR_SYSTEM) Color(0xFFFFD700) else Color(0xFF9C27B0),
            onClick = {
                val newTheme = if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
                    GraphTheme.DEFAULT
                } else {
                    GraphTheme.SOLAR_SYSTEM
                }
                onToggleTheme(newTheme)
            }
        )
    }
}

@Composable
private fun CompactButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (color.luminance() > 0.5f) Color.Black else Color.White
        )
    }
}

// Extension to calculate luminance for contrast
private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
