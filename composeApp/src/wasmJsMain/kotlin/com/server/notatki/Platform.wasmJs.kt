package com.server.notatki

import androidx.compose.ui.platform.Clipboard
import kotlinx.browser.window
import kotlinx.coroutines.await

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return try {
        window.navigator.clipboard.readText().await()
    } catch (e: Exception) {
        null
    }
}