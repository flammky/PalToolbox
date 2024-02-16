package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.ApplicationScope
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import dev.dexsr.gmod.palworld.trainer.composeui.LocalComposeApplicationScope
import dev.dexsr.gmod.palworld.trainer.composeui.LocalWindow
import javax.swing.UIManager

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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (e: Exception) {
        }
        val window = this

        pane.setContent {
            CompositionLocalProvider(
                LocalComposeApplicationScope provides applicationScope,
                LocalWindow provides this,
                LocalTitleBarBehavior provides titleBarBehavior,
                LocalRippleTheme provides remember { object : RippleTheme {
                    @Composable
                    override fun defaultColor(): Color = RippleTheme.defaultRippleColor(
                        contentColor = remember { Color(250, 250,250) },
                        lightTheme = false
                    )

                    @Composable
                    override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
                        contentColor = remember { Color(250, 250,250) },
                        lightTheme = false
                    )
                } }
            ) {
                MainScreen()
            }
        }

        contentPane.add(pane)

        window
            .apply {
                setSize(800, 600)
                title = "PalWorld Trainer"
                iconImage = run {
                    val resourcePath = "drawable/palworld_p_icon.png"
                    val contextClassLoader = Thread.currentThread().contextClassLoader!!
                    val resource = contextClassLoader.getResourceAsStream(resourcePath)
                    requireNotNull(resource) {
                        "Resource $resourcePath not found"
                    }.use(::loadImageBitmap).toAwtImage()
                }
            }
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