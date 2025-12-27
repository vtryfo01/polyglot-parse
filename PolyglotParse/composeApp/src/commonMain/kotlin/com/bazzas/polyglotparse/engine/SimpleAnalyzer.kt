package com.bazzas.polyglotparse.engine

import com.bazzas.polyglotparse.model.CodeNode
import com.bazzas.polyglotparse.model.NodeType
import com.bazzas.polyglotparse.model.NodeKind
import com.bazzas.polyglotparse.model.SourceSet

class SimpleAnalyzer {

    // Binary file extensions to skip
    private val binaryExtensions = setOf(
        ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".webp",  // Images
        ".zip", ".jar", ".aar", ".apk", ".ipa", ".tar", ".gz",     // Archives
        ".class", ".so", ".dll", ".exe", ".bin", ".o",              // Binaries
        ".ttf", ".otf", ".woff", ".woff2",                          // Fonts
        ".mp3", ".mp4", ".avi", ".mov", ".wav"                      // Media
    )

    // Check if file is binary (should be skipped)
    fun isBinary(name: String): Boolean {
        return binaryExtensions.any { name.lowercase().endsWith(it) }
    }

    // Helper: detect if XML is config vs resource
    private fun isConfigXml(filePath: String): Boolean {
        return filePath.contains("build") ||
               filePath.contains("config") ||
               filePath.contains("AndroidManifest") ||
               filePath.contains("pom.xml")
    }

    // Takes raw file text and returns a stats object
    fun analyze(content: String, path: String, name: String): CodeNode {
        val lines = content.lines()
        val lowerName = name.lowercase()

        // 1. Determine NodeKind and Language
        val (kind, language) = when {
            // CODE files
            lowerName.endsWith(".kt") -> NodeKind.CODE to "Kotlin"
            lowerName.endsWith(".java") -> NodeKind.CODE to "Java"
            lowerName.endsWith(".swift") -> NodeKind.CODE to "Swift"
            lowerName.endsWith(".js") || lowerName.endsWith(".ts") -> NodeKind.CODE to "JavaScript"
            lowerName.endsWith(".py") -> NodeKind.CODE to "Python"
            lowerName.endsWith(".rb") -> NodeKind.CODE to "Ruby"
            lowerName.endsWith(".go") -> NodeKind.CODE to "Go"
            lowerName.endsWith(".rs") -> NodeKind.CODE to "Rust"
            lowerName.endsWith(".c") || lowerName.endsWith(".h") -> NodeKind.CODE to "C"
            lowerName.endsWith(".cpp") || lowerName.endsWith(".hpp") -> NodeKind.CODE to "C++"
            lowerName.endsWith(".cs") -> NodeKind.CODE to "C#"

            // CONFIG files
            lowerName.endsWith(".gradle") || lowerName.endsWith(".gradle.kts") -> NodeKind.CONFIG to "Gradle"
            lowerName.endsWith(".toml") -> NodeKind.CONFIG to "TOML"
            lowerName.endsWith(".yaml") || lowerName.endsWith(".yml") -> NodeKind.CONFIG to "YAML"
            lowerName.endsWith(".json") -> NodeKind.CONFIG to "JSON"
            lowerName.endsWith(".properties") -> NodeKind.CONFIG to "Properties"
            lowerName.endsWith(".xml") && isConfigXml(path) -> NodeKind.CONFIG to "XML"
            lowerName == "podfile" || lowerName.endsWith(".podspec") -> NodeKind.CONFIG to "CocoaPods"
            lowerName == "package.json" -> NodeKind.CONFIG to "NPM"
            lowerName == "pubspec.yaml" -> NodeKind.CONFIG to "Flutter"

            // RESOURCE files
            lowerName.endsWith(".xml") -> NodeKind.RESOURCE to "XML"  // Android layouts/strings
            lowerName.endsWith(".plist") -> NodeKind.RESOURCE to "PList"
            lowerName.endsWith(".strings") -> NodeKind.RESOURCE to "Strings"
            lowerName.endsWith(".storyboard") || lowerName.endsWith(".xib") -> NodeKind.RESOURCE to "Interface Builder"

            // OTHER
            lowerName.endsWith(".md") || lowerName.endsWith(".txt") -> NodeKind.OTHER to "Text"
            lowerName.endsWith(".html") || lowerName.endsWith(".htm") -> NodeKind.OTHER to "HTML"
            lowerName.endsWith(".css") || lowerName.endsWith(".scss") -> NodeKind.OTHER to "CSS"

            else -> NodeKind.OTHER to "Other"
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
            kind = kind,  // NEW: NodeKind categorization
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