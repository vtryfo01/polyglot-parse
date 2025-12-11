package com.bazzas.polyglotparse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform