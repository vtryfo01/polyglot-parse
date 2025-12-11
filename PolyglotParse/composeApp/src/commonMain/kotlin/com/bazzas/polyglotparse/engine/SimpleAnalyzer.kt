package com.bazzas.polyglotparse.engine

import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.NodeType

class SimpleAnalyzer {

    // Takes raw file text and returns a stats object
    fun analyze(content: String, path: String, name: String): CodeNode {
        val lines = content.lines()

        // 1. Determine Language
        val language = when {
            name.endsWith(".kt") -> "Kotlin"
            name.endsWith(".java") -> "Java"
            name.endsWith(".xml") -> "XML"
            name.endsWith(".gradle.kts") -> "Gradle"
            else -> "Other"
        }

        // 2. Calculate "Cyclomatic Complexity" (The Heuristic)
        // We count how many decision points exist.
        val complexityScore = lines.count { line ->
            val t = line.trim()
            t.startsWith("if") ||
                    t.startsWith("for") ||
                    t.startsWith("while") ||
                    t.startsWith("when") ||
                    t.contains("&&") ||
                    t.contains("||")
        }

        return CodeNode(
            id = path,
            name = name,
            type = NodeType.FILE,
            language = language,
            complexity = complexityScore,
            linesOfCode = lines.size,
            content = content
        )
    }
}