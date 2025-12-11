package com.bazzas.polyglotparse

import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.core.FileSystem

class AndroidFileSystem : FileSystem {
    override val startPath: String
        get() = "/"

    override suspend fun getFiles(path: String): List<FileItem> {
        return emptyList()
    }

    override suspend fun readFile(path: String): String {
        return ""
    }
}
