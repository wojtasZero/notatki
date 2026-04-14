package com.server.notatki

import androidx.compose.ui.platform.Clipboard

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return ""
}