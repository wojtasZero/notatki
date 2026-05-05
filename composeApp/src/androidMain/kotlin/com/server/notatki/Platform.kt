package com.server.notatki

import android.content.ClipData
import android.os.Build
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return clipboard.getClipEntry()?.clipData?.getItemAt(0)?.text?.toString()
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("text", text)))
}
