package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.model.GraphTheme

@Composable
fun GraphLegend(
    graphTheme: GraphTheme,
    modifier: Modifier = Modifier
) {
    // Semi-transparent glassy background
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1F3A).copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            // Source Set Colors
            Text(
                text = "Source Sets",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            LegendItem(color = Color(0xFF5CE1E6), label = "commonMain")
            LegendItem(color = Color(0xFF00E676), label = "androidMain")
            LegendItem(color = Color(0xFFFF9800), label = "iosMain")
            LegendItem(color = Color(0xFF9C27B0), label = "jvmMain")

            Spacer(Modifier.height(4.dp))

            // Edge Types
            Text(
                text = "Edge Types",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(Color(0xFF4444AA))
                )
                Text(
                    text = "Dependency",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(Color(0xFFFFD700))
                )
                Text(
                    text = if (graphTheme == GraphTheme.SOLAR_SYSTEM) "Wormhole (Expect/Actual)" else "Expect/Actual",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(4.dp))

            // Languages
            Text(
                text = "Languages",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            LegendItem(color = Color(0xFF5CE1E6), label = "Kotlin", isCircle = false)
            LegendItem(color = Color(0xFFFFC857), label = "Java", isCircle = false)
            LegendItem(color = Color(0xFFFF9800), label = "Swift", isCircle = false)
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isCircle: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCircle) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        } else {
            Box(
                Modifier
                    .width(12.dp)
                    .height(8.dp)
                    .background(color)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}
