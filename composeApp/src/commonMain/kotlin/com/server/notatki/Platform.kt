package com.server.notatki

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform