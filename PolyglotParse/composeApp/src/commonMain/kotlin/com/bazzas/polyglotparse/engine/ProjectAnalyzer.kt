package com.bazzas.polyglotparse.engine

import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.core.FileSystem
import com.bazzas.polyglotparse.model.*

class ProjectAnalyzer(
    private val fileSystem: FileSystem,
    private val simpleAnalyzer: SimpleAnalyzer = SimpleAnalyzer()
) {

    suspend fun analyzeProject(rootPath: String): CodeGraph {
        val allFiles = collectFilesRecursively(rootPath)
            .filter { !it.isDirectory && (it.name.endsWith(".kt") || it.name.endsWith(".java")) }

        val nodes = mutableListOf<CodeNode>()
        val edges = mutableListOf<CodeEdge>()

        // 1) Build a node per file
        for (file in allFiles) {
            val content = fileSystem.readFile(file.path)
            val node = simpleAnalyzer.analyze(
                content = content,
                path = file.path,
                name = file.name
            )
            nodes += node
        }

        // 2) Very simple dependency detection via imports
        val nodeByPath = nodes.associateBy { it.id }

        for (node in nodes) {
            val imports = node.content.lines()
                .map { it.trim() }
                .filter { it.startsWith("import ") }

            for (imp in imports) {
                val simpleName = imp
                    .removePrefix("import")
                    .trim()
                    .substringAfterLast('.')
                    .removeSuffix(";")

                val target = nodeByPath.values.firstOrNull {
                    it.name.removeSuffix(".kt").removeSuffix(".java") == simpleName
                }

                if (target != null) {
                    edges += CodeEdge(
                        fromId = node.id,
                        toId = target.id,
                        type = EdgeType.DEPENDS_ON
                    )
                }
            }
        }

        return CodeGraph(nodes, edges)
    }

    private suspend fun collectFilesRecursively(rootPath: String): List<FileItem> {
        val result = mutableListOf<FileItem>()

        suspend fun walk(path: String) {
            val children = fileSystem.getFiles(path)
            for (child in children) {
                if (child.isDirectory) {
                    walk(child.path)
                } else {
                    result += child
                }
            }
        }

        walk(rootPath)
        return result
    }
}
