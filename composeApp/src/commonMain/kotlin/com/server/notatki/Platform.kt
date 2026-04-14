package com.server.notatki

import androidx.compose.ui.platform.Clipboard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect suspend fun getClipboardText(clipboard: Clipboard): String?