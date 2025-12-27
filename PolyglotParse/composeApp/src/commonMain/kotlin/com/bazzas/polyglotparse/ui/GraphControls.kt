package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.model.GraphTheme

@Composable
fun GraphControls(
    is3DMode: Boolean,
    isAutoRotate: Boolean,
    graphTheme: GraphTheme,
    onToggle3DMode: (Boolean) -> Unit,
    onToggleAutoRotate: (Boolean) -> Unit,
    onToggleTheme: (GraphTheme) -> Unit,
    onResetCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2D/3D Toggle Button
        Button(
            onClick = { onToggle3DMode(!is3DMode) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (is3DMode) Color(0xFF5CE1E6) else Color(0xFF4444AA)
            )
        ) {
            Text(if (is3DMode) "3D Mode" else "2D Mode")
        }

        // Auto-rotate Toggle
        if (is3DMode) {
            Button(
                onClick = { onToggleAutoRotate(!isAutoRotate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAutoRotate) Color(0xFFFF9800) else Color(0xFF666666)
                )
            ) {
                Text(if (isAutoRotate) "⟳ Auto" else "Auto-rotate")
            }
        }

        // Reset Camera Button
        Button(
            onClick = onResetCamera,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
        ) {
            Text("Reset")
        }

        // Theme Toggle Button
        Button(
            onClick = {
                val newTheme = if (graphTheme == GraphTheme.SOLAR_SYSTEM) {
                    GraphTheme.DEFAULT
                } else {
                    GraphTheme.SOLAR_SYSTEM
                }
                onToggleTheme(newTheme)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (graphTheme == GraphTheme.SOLAR_SYSTEM) Color(0xFFFFD700) else Color(0xFF9C27B0)
            )
        ) {
            Text(if (graphTheme == GraphTheme.SOLAR_SYSTEM) "☀ Solar" else "◈ Default")
        }

        Spacer(Modifier.weight(1f))

        // Info text
        Text(
            text = if (is3DMode) "Drag to rotate" else "2D View",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
