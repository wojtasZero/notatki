package com.server.notatki

import androidx.compose.ui.platform.Clipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return withContext(Dispatchers.IO) {
        Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor)
    }.toString()
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    withContext(Dispatchers.IO) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}