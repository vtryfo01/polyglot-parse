package com.bazzas.polyglotparse

import com.bazzas.polyglotparse.core.FileItem
import com.bazzas.polyglotparse.core.FileSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class DesktopFileSystem : FileSystem {

    override val startPath: String
        get() = System.getProperty("user.home")

    override suspend fun getFiles(path: String): List<FileItem> {
        val file = File(path)
        val filesArray = file.listFiles()

        if (filesArray == null) {
            return ArrayList<FileItem>()
        }

        val resultList = ArrayList<FileItem>()

        for (f in filesArray) {
            resultList.add(
                FileItem(
                    path = f.absolutePath,
                    name = f.name,
                    isDirectory = f.isDirectory
                )
            )
        }

        Collections.sort(resultList, object : Comparator<FileItem> {
            override fun compare(a: FileItem, b: FileItem): Int {
                if (a.isDirectory == b.isDirectory) {
                    return a.name.compareTo(b.name)
                } else {
                    return if (a.isDirectory) -1 else 1
                }
            }
        })

        return resultList
    }

    override suspend fun readFile(path: String): String {
        // Force usage of Java's String constructor
        val bytes = Files.readAllBytes(Paths.get(path))
        return java.lang.String(bytes).toString()
    }
}