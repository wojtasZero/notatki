package com.server.notatki

import androidx.compose.ui.platform.Clipboard
import kotlinx.browser.window
import kotlinx.coroutines.await

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return try {
        window.navigator.clipboard.readText().await()
    } catch (e: Exception) {
        null
    }
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    try {
        window.navigator.clipboard.writeText(text).await()
    } catch (e: Exception) {
        // ignore
    }
}
