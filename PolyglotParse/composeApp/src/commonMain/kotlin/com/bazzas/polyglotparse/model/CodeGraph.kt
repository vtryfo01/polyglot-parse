package com.bazzas.polyglotparse.model

enum class NodeType { FILE, CLASS, FUNCTION }

enum class SourceSet {
    COMMON,
    ANDROID,
    IOS,
    JVM,
    DESKTOP,
    UNKNOWN
}

data class CodeNode(
    val id: String,
    val name: String,
    val type: NodeType,
    val language: String,
    val sourceSet: SourceSet = SourceSet.UNKNOWN,
    val isExpect: Boolean = false,
    val isActual: Boolean = false,
    val expectActualName: String? = null,
    val complexity: Int = 0,
    val linesOfCode: Int = 0,
    val content: String = ""
)

enum class EdgeType { DEPENDS_ON, EXPECT_ACTUAL }

data class CodeEdge(
    val fromId: String,
    val toId: String,
    val type: EdgeType
)

data class CodeGraph(
    val nodes: List<CodeNode>,
    val edges: List<CodeEdge>
)
