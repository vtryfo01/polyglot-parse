// androidMain/kotlin/com/bazzas/polyglotparse/AndroidFileSystem.kt
package com.bazzas.polyglotparse

import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.core.FileSystem

class AndroidFileSystem : FileSystem {
    override val startPath: String
        get() = "/" // or some safe default / test folder

    override suspend fun getFiles(path: String): List<FileItem> {
        // For now, return empty list or a fake folder until you wire real storage
        return emptyList()
    }

    override suspend fun readFile(path: String): String {
        return ""
    }
}
