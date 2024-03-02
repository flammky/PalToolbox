package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.sun.jna.Native
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.WindowProc
import com.sun.jna.win32.W32APIOptions
import dev.dexsr.gmod.palworld.trainer.platform.win32.CustomDecorationParameters
import dev.dexsr.gmod.palworld.trainer.platform.win32.User32Ex
import java.awt.Frame.MAXIMIZED_BOTH
import java.awt.Toolkit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Stable
internal class CustomWin32TitleBarBehavior(
    private val window: WindowsMainAwtWindow,
    onCloseClicked: () -> Unit
) : TitleBarBehavior {

    private var winproc: WindowProcedureHandle? = null

    private var hWnd: WinDef.HWND? = null
    private var defWndProc: LONG_PTR? = null

    private var USER32_INSTANCEex: User32Ex? = null

    private var windowRestoreState = mutableStateOf(false)
    private var titleBarHeightStatePx = mutableStateOf(0)

    // the animation is not handled by us, the flag will not be set to extended until then
    private var promiseWindowWillBeMaximized = false
    private var promiseWindowWillBeRestored = false
    private var promiseWindowWillBeMinimized = false

    private val _onCloseClicked = onCloseClicked

    private var lastValidatedWindowSize: IntSize = IntSize.Zero
    // maybe: combine to Position with IntRect ?
    private var lastValidatedFloatingWindowSize: IntSize = IntSize.Zero
    private var lastValidatedFloatingWindowOffset: IntOffset = IntOffset.Zero

    fun init(hWnd: WinDef.HWND) {
        this.hWnd = hWnd

        prepareUser32Binding().let { binding ->
            prepareWinProcHandler().let { handler ->
                defWndProc =
                    if (is64Bit())
                        binding.SetWindowLongPtr(
                            hWnd, User32Ex.GWLP_WNDPROC,
                            handler
                        )
                    else
                        binding.SetWindowLong(hWnd, User32Ex.GWLP_WNDPROC, handler)
            }
            binding.SetWindowPos(
                hWnd, hWnd, 0, 0, 0, 0,
                WinUser.SWP_NOMOVE or WinUser.SWP_NOSIZE or WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED
            )
        }

        revalidateWindowStateSnapshots()
    }

    private fun prepareUser32Binding(): User32Ex {
        val binding = USER32_INSTANCEex
            ?: Native
                .load("user32", User32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS)
                .cast<User32Ex>()
                .also { USER32_INSTANCEex = it }
        return binding
    }

    private fun prepareWinProcHandler(): WindowProcedureHandle {
        val winproc = winproc
            ?: WindowProcedureHandle()
                .also { winproc = it }
        return winproc
    }

    private fun requireWindowPointer(): WinDef.HWND {
        return hWnd ?: error("TitleBarBehavior wasn't initialized")
    }

    // based on https://github.com/kalibetre/CustomDecoratedJFrame
    private inner class WindowProcedureHandle : WindowProc {

        override fun callback(
            hwnd: WinDef.HWND?,
            uMsg: Int,
            wParam: WinDef.WPARAM?,
            lParam: WinDef.LPARAM?
        ): WinDef.LRESULT {

            /*println("WindowProcedureHandle, callback, msg=$uMsg")*/
            val lresult: WinDef.LRESULT
            when (uMsg) {
                WM_NCCALCSIZE -> return WinDef.LRESULT(0)
                WM_NCHITTEST -> {
                    lresult = this.BorderLessHitTest(hwnd, uMsg, wParam, lParam)
                    if (lresult.toInt() == WinDef.LRESULT(0).toInt()) {
                        return prepareUser32Binding().CallWindowProc(defWndProc, hwnd, uMsg, wParam, lParam)
                    }
                    return lresult
                }
                WinUser.WM_DESTROY -> {
                    prepareUser32Binding().let {
                        if (is64Bit()) it.SetWindowLongPtr(hwnd, User32Ex.GWLP_WNDPROC, checkNotNull(defWndProc))
                        else it.SetWindowLong(hwnd, User32Ex.GWLP_WNDPROC, checkNotNull(defWndProc))
                    }
                    return WinDef.LRESULT(0)
                }
                else -> {
                    lresult = prepareUser32Binding().CallWindowProc(defWndProc, hwnd, uMsg, wParam, lParam)
                    return lresult
                }
            }
        }

        fun BorderLessHitTest(hWnd: HWND?, message: Int, wParam: WPARAM?, lParam: LPARAM?): LRESULT {
            val borderOffset: Int = CustomDecorationParameters.maximizedWindowFrameThickness
            val borderThickness: Int = CustomDecorationParameters.frameResizeBorderThickness

            val ptMouse = POINT()
            val rcWindow = RECT()
            User32.INSTANCE.GetCursorPos(ptMouse)
            User32.INSTANCE.GetWindowRect(hWnd, rcWindow)

            var uRow = 1
            var uCol = 1
            var fOnResizeBorder = false
            var fOnFrameDrag = false

            val topOffset =
                if (CustomDecorationParameters.titleBarHeight == 0) borderThickness else CustomDecorationParameters.titleBarHeight
            if (ptMouse.y >= rcWindow.top && ptMouse.y < rcWindow.top + topOffset + borderOffset) {
                fOnResizeBorder = (ptMouse.y < (rcWindow.top + borderThickness)) // Top Resizing
                if (!fOnResizeBorder) {
                    fOnFrameDrag =
                        ((ptMouse.y <= rcWindow.top + CustomDecorationParameters.titleBarHeight + borderOffset)
                                && (ptMouse.x < (rcWindow.right - (CustomDecorationParameters.controlBoxWidth
                                + borderOffset + CustomDecorationParameters.extraRightReservedWidth)))
                                && (ptMouse.x > (rcWindow.left + CustomDecorationParameters.iconWidth
                                + borderOffset + CustomDecorationParameters.extraLeftReservedWidth)))
                }
                uRow = 0 // Top Resizing or Caption Moving
            } else if (ptMouse.y < rcWindow.bottom && ptMouse.y >= rcWindow.bottom - borderThickness) uRow =
                2 // Bottom Resizing

            if (ptMouse.x >= rcWindow.left && ptMouse.x < rcWindow.left + borderThickness) uCol = 0 // Left Resizing
            else if (ptMouse.x < rcWindow.right && ptMouse.x >= rcWindow.right - borderThickness) uCol =
                2 // Right Resizing


            val HTTOPLEFT = 13
            val HTTOP = 12
            val HTCAPTION = 2
            val HTTOPRIGHT = 14
            val HTLEFT = 10
            val HTNOWHERE = 0
            val HTRIGHT = 11
            val HTBOTTOMLEFT = 16
            val HTBOTTOM = 15
            val HTBOTTOMRIGHT = 17
            val HTSYSMENU = 3

            val hitTests = arrayOf(
                intArrayOf(
                    HTTOPLEFT,
                    if (fOnResizeBorder) HTTOP else if (fOnFrameDrag) HTCAPTION else HTNOWHERE,
                    HTTOPRIGHT
                ),
                intArrayOf(HTLEFT, HTNOWHERE, HTRIGHT),
                intArrayOf(HTBOTTOMLEFT, HTBOTTOM, HTBOTTOMRIGHT),
            )

            return LRESULT(hitTests[uRow][uCol].toLong())
        }
    }

    override val showRestoreWindow: Boolean
        get() = windowRestoreState.value

    override val titleBarHeightPx: Int
        get() = titleBarHeightStatePx.value


    override fun minimizeClicked() {
        // same behavior as caption minimize
        val promiseMinimized = User32.INSTANCE.CloseWindow(requireWindowPointer())
        if (promiseMinimized) promiseWindowWillBeMinimized()
        revalidateWindowStateSnapshots()
    }

    override fun restoreClicked() {
        var promiseRestored = false
        val device = window.graphicsConfiguration.device
        val window = this.window
        // TODO: check behavior
        if (device.fullScreenWindow == window) {
            device.fullScreenWindow = null
        } else {
            val maximizedState = window.extendedState or MAXIMIZED_BOTH
            if (window.extendedState == maximizedState || promiseWindowWillBeMaximized) {
                promiseRestored =
                    User32.INSTANCE.SetWindowPlacement(
                        requireWindowPointer(),
                        WinUser.WINDOWPLACEMENT()
                            .apply {
                                flags = WinUser.WINDOWPLACEMENT.WPF_ASYNCWINDOWPLACEMENT
                                showCmd = WinUser.SW_RESTORE
                                rcNormalPosition = RECT()
                                    .apply {
                                        val pos = lastValidatedFloatingWindowOffset
                                        val size = lastValidatedFloatingWindowSize
                                        val insets = getScreenInsets()
                                        left = pos.x - insets.left
                                        top = pos.y - insets.top
                                        right = left + size.width
                                        bottom = top + size.height
                                    }
                            }
                    ).booleanValue()
            }
        }
        if (promiseRestored) promiseWindowWillBeRestored()
        return revalidateWindowStateSnapshots()
    }

    override fun maximizeClicked() {
        val window = this.window
        val maximizedState = window.extendedState or MAXIMIZED_BOTH
        var promiseMaximized = false
        if (window.extendedState != maximizedState) {
            promiseMaximized =
                User32.INSTANCE.SetWindowPlacement(
                    requireWindowPointer(),
                    WinUser.WINDOWPLACEMENT()
                        .apply {
                            flags = WinUser.WINDOWPLACEMENT.WPF_ASYNCWINDOWPLACEMENT
                            showCmd = WinUser.SW_MAXIMIZE
                        }
                ).booleanValue()
        }

        if (promiseMaximized) promiseWindowWillBeMaximized()
        return revalidateWindowStateSnapshots()
    }

    override fun closeClicked() {
        _onCloseClicked.invoke()
    }

    private fun revalidateWindowStateSnapshots() {
        var setShowRestore = false

        if (!promiseWindowWillBeRestored) {
            // check full-screen
            run {
                val device = window.graphicsConfiguration.device
                if (device.fullScreenWindow == this.window) {
                    setShowRestore = true
                }
            }

            // check maximized
            run {
                val window = this.window
                val maximizedState = window.extendedState or MAXIMIZED_BOTH
                if (window.extendedState == maximizedState) {
                    setShowRestore = true
                }
            }
        }
        lastValidatedFloatingWindowSize =
            if (!promiseWindowWillBeRestored) getWindowSize()
            else lastValidatedFloatingWindowSize
        lastValidatedFloatingWindowOffset =
            if (!promiseWindowWillBeRestored) getWindowPosition()
            else lastValidatedFloatingWindowOffset
        lastValidatedWindowSize =
            if (promiseWindowWillBeMaximized) getMaximizedWindowSize(false)
            else if (promiseWindowWillBeRestored) lastValidatedFloatingWindowSize
            else getWindowSize()
        windowRestoreState.value = promiseWindowWillBeMaximized || setShowRestore
    }

    private fun promiseWindowWillBeMaximized() {
        promiseWindowWillBeMaximized = true
        promiseWindowWillBeMinimized = false
        promiseWindowWillBeRestored = false
    }

    private fun promiseWindowWillBeMinimized() {
        promiseWindowWillBeMinimized = true
        promiseWindowWillBeMaximized = false
        promiseWindowWillBeRestored = false
    }

    private fun promiseWindowWillBeRestored() {
        promiseWindowWillBeRestored = true
        promiseWindowWillBeMaximized = false
        promiseWindowWillBeMinimized = false
    }


    private fun getMaximizedWindowSize(ignoreInsets: Boolean): IntSize {
        val screenBounds = window.graphicsConfiguration.bounds
        val screenSize =
            if (ignoreInsets) {
                IntSize(screenBounds.width, screenBounds.height)
            } else {
                val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.graphicsConfiguration)
                IntSize(
                    screenBounds.width - screenInsets.left - screenInsets.right,
                    screenBounds.height - screenInsets.top - screenInsets.bottom
                )
            }
        return screenSize
    }

    private fun getWindowSize(): IntSize {
        return IntSize(window.width, window.height)
    }

    private fun getWindowPosition(): IntOffset {
        return IntOffset(window.x, window.y)
    }

    private fun getWindowCenterAlignedPosition(
        windowSize: IntSize
    ): IntOffset {
        val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.graphicsConfiguration)
        val screenBounds = window.graphicsConfiguration.bounds
        val size = windowSize
        val screenSize = IntSize(
            screenBounds.width - screenInsets.left - screenInsets.right,
            screenBounds.height - screenInsets.top - screenInsets.bottom
        )
        val location = Alignment.Center.align(size, screenSize, LayoutDirection.Ltr)
        return IntOffset(
            x = screenBounds.x + screenInsets.left + location.x,
            y = screenBounds.y + screenInsets.top + location.y
        )
    }

    private fun getScreenBounds(ignoreInsets: Boolean): IntSize {
        val rect = window.graphicsConfiguration.bounds
        if (ignoreInsets) return IntSize(rect.width, rect.height)
        val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.graphicsConfiguration)
        return IntSize(
            width = rect.width - screenInsets.left - screenInsets.right,
            height = rect.width - screenInsets.left - screenInsets.right
        )
    }

    // maybe: find equivalent in compose lib
    private fun getScreenInsets(): java.awt.Insets {
        return Toolkit.getDefaultToolkit().getScreenInsets(window.graphicsConfiguration)
    }

    companion object {
        private const val WM_NCCALCSIZE: Int = 0x0083
        private const val WM_NCHITTEST: Int = 0x0084

        private fun is64Bit(): Boolean {
            val model = System.getProperty(
                "sun.arch.data.model",
                System.getProperty("com.ibm.vm.bitmode")
            )
            if (model != null) {
                return "64" == model
            }
            return false
        }
    }
}


@OptIn(ExperimentalContracts::class)
private inline fun <reified R> Any?.cast(): R {
    contract {
        returns() implies (this@cast is R)
    }
    return this as R
}