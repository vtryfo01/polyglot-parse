package com.bazzas.polyglotparse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.SourceSet

@Composable
fun SearchFilterPanel(
    nodes: List<CodeNode>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    selectedSourceSets: Set<SourceSet>,
    onSourceSetToggle: (SourceSet) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search files") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Language filters
        Text("Languages:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val languages = nodes.map { it.language }.distinct().sorted()
            languages.forEach { lang ->
                FilterChip(
                    selected = selectedLanguages.contains(lang),
                    onClick = { onLanguageToggle(lang) },
                    label = { Text(lang, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        // Source set filters
        Text("Source Sets:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val sourceSets = listOf(SourceSet.COMMON, SourceSet.ANDROID, SourceSet.IOS, SourceSet.JVM)
            sourceSets.forEach { sourceSet ->
                val count = nodes.count { it.sourceSet == sourceSet }
                if (count > 0) {
                    FilterChip(
                        selected = selectedSourceSets.contains(sourceSet),
                        onClick = { onSourceSetToggle(sourceSet) },
                        label = { Text("${sourceSet.name} ($count)", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
        }
    }
}
