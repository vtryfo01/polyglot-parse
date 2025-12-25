package com.bazzas.polyglotparse.engine

import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.NodeType
import com.bazzas.polyglotparse.model.SourceSet

class SimpleAnalyzer {

    // Takes raw file text and returns a stats object
    fun analyze(content: String, path: String, name: String): CodeNode {
        val lines = content.lines()

        // 1. Determine Language
        val language = when {
            name.endsWith(".kt") -> "Kotlin"
            name.endsWith(".java") -> "Java"
            name.endsWith(".swift") -> "Swift"
            name.endsWith(".xml") -> "XML"
            name.endsWith(".gradle.kts") -> "Gradle"
            else -> "Other"
        }

        // 2. Detect Source Set from path
        val sourceSet = when {
            path.contains("/commonMain/") || path.contains("\\commonMain\\") -> SourceSet.COMMON
            path.contains("/androidMain/") || path.contains("\\androidMain\\") -> SourceSet.ANDROID
            path.contains("/iosMain/") || path.contains("\\iosMain\\") -> SourceSet.IOS
            path.contains("/jvmMain/") || path.contains("\\jvmMain\\") -> SourceSet.JVM
            path.contains("/desktopMain/") || path.contains("\\desktopMain\\") -> SourceSet.DESKTOP
            else -> SourceSet.UNKNOWN
        }

        // 3. Detect expect/actual keywords
        val expectRegex = Regex("""expect\s+(class|fun|val|interface|object)\s+(\w+)""")
        val actualRegex = Regex("""actual\s+(class|fun|val|interface|object)\s+(\w+)""")

        val expectMatch = expectRegex.find(content)
        val actualMatch = actualRegex.find(content)

        val isExpect = expectMatch != null
        val isActual = actualMatch != null
        val expectActualName = expectMatch?.groupValues?.get(2) ?: actualMatch?.groupValues?.get(2)

        // 4. Calculate "Cyclomatic Complexity" (The Heuristic)
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
            sourceSet = sourceSet,
            isExpect = isExpect,
            isActual = isActual,
            expectActualName = expectActualName,
            complexity = complexityScore,
            linesOfCode = lines.size,
            content = content
        )
    }
}