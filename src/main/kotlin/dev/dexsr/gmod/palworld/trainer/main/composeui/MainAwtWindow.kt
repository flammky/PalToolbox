package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.ui.window.ApplicationScope
import javax.swing.JFrame
import com.sun.jna.Platform as JnaPlatform

open class MainAwtWindow protected constructor() : JFrame() {
}

fun ApplicationScope.PlatformMainAwtWindow(): MainAwtWindow {
    return when {
        JnaPlatform.isWindows() -> WindowsMainAwtWindow(this)
        else -> TODO("No Impl for platform code=${JnaPlatform.getOSType()}")
    }
}

