package com.server.notatki

import androidx.compose.ui.platform.Clipboard
import platform.UIKit.UIDevice
import platform.UIKit.UIPasteboard

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual suspend fun getClipboardText(clipboard: Clipboard): String? {
    return UIPasteboard.generalPasteboard.string
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    UIPasteboard.generalPasteboard.string = text
}