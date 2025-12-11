package com.bazzas.polyglotparse

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "PolyglotParse") {
        val fileSystem = remember { DesktopFileSystem() }
        App(fileSystem)
    }
}