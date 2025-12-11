package com.bazzas.polyglotparse.model

enum class NodeType { FILE, CLASS, FUNCTION }

data class CodeNode(
    val id: String,
    val name: String,
    val type: NodeType,
    val language: String,
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
