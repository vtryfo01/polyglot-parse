package com.bazzas.polyglotparse.core

// A simple model to represent a file or folder
data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean
)

// The interface that platforms (Android/Desktop) must implement
interface FileSystem {
    // Returns a list of files in a specific folder
    suspend fun getFiles(path: String): List<FileItem>

    // Reads the text content of a specific file
    suspend fun readFile(path: String): String

    // A starting point path (e.g. user home or a default test folder)
    val startPath: String
}