package com.server.notatki

import androidx.compose.ui.platform.Clipboard
import io.ktor.client.HttpClient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect suspend fun getClipboardText(clipboard: Clipboard): String?