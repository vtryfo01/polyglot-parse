package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GraphControls(
    is3DMode: Boolean,
    onToggle3DMode: (Boolean) -> Unit,
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

        // Reset Camera Button
        Button(
            onClick = onResetCamera,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
        ) {
            Text("Reset Camera")
        }

        Spacer(Modifier.weight(1f))

        // Info text
        Text(
            text = if (is3DMode) "Drag to rotate • Scroll to zoom" else "2D View",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
