package dev.dexsr.gmod.palworld.trainer.composeui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.window.ApplicationScope
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import dev.dexsr.gmod.palworld.trainer.composeui.LocalComposeApplicationScope

class WindowsMainAwtWindow(
    private val applicationScope: ApplicationScope
) : DesktopMainAwtWindow() {

    private val titleBarBehavior = CustomWin32TitleBarBehavior(
        this,
        onCloseClicked = applicationScope::exitApplication
    )

    private val pane = ComposePanel()

    private var windowHandle: Long? = null

    init {
        System.setProperty("compose.swing.render.on.graphics", "true")

        val window = this

        pane.setContent {
            CompositionLocalProvider(
                LocalComposeApplicationScope provides applicationScope,
                LocalTitleBarBehavior provides titleBarBehavior
            ) {
                MainScreen()
            }
        }

        contentPane.add(pane)

        window.setSize(800, 600)
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        titleBarBehavior.init(hWnd())
    }

    private fun prepareWindowHandle(): Long {
        val ptr = windowHandle
            ?: Pointer.nativeValue(Native.getWindowPointer(this))
        return ptr
    }

    private fun hWnd(): WinDef.HWND = WinDef.HWND().apply { pointer = Pointer(prepareWindowHandle()) }
}