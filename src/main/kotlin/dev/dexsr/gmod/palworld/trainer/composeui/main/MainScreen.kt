package dev.dexsr.gmod.palworld.trainer.composeui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary
import dev.dexsr.gmod.palworld.trainer.composeui.LocalComposeApplicationScope
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonScaledFontSize
import dev.dexsr.gmod.palworld.trainer.themes.md3.*
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter


@Composable
fun MainScreen() {
    val state = rememberMainScreenState()
    MainScreen(state)
}

interface Kernel32 : StdCallLibrary {
    // Retrieves the number of milliseconds that have elapsed since the system was started
    fun GetTickCount64(): Long

    companion object {
        val INSTANCE: Kernel32 = Native.load("kernel32", Kernel32::class.java)
    }
}


@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun MainScreen(
    state: MainScreenState
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onPointerEvent(eventType = PointerEventType.Move) {
                println(it.toString())
            }
    ) {
        MainScreenLayoutSurface(
            modifier = Modifier,
            color = remember { Color(43,42,51,255) }
        )
        MainScreenLayoutContent(
            contentPadding = PaddingValues(horizontal = MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp)
        )
    }
}

@Composable
fun MainScreenLayoutSurface(
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .defaultSurfaceGestureModifiers()
    )
}

@Composable
fun MainScreenLayoutContent(
    contentPadding: PaddingValues
) {
    val leftPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
    val rightPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
    Column(
        modifier = Modifier
            .padding(start = leftPadding, end = rightPadding)
    ) {
        MainScreenLayoutTopBar(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()))
    }
}

@Composable
fun MainScreenLayoutTopBar(
    contentPadding: PaddingValues = PaddingValues()
) {
    Box {
        Row(
            modifier = Modifier.padding(contentPadding).height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainScreenLayoutIconTitle(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
            )
            MainScreenLayoutCaptionControls()
        }
    }
}

@Composable
private fun MainScreenLayoutIconTitle(
    modifier: Modifier
) {
    Row(modifier) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource("drawable/palworld_p_icon.png"),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = "PALWORLD TRAINER",
            style = MaterialTheme.typography.subtitle2,
            fontSize = MaterialTheme.typography.subtitle2.nonScaledFontSize(),
            color = Color.White,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainScreenLayoutCaptionControls(

) {
    val app = LocalComposeApplicationScope.current
    val titleBarBehavior = LocalTitleBarBehavior.current
    val invalidateFlag = remember { mutableStateOf(0) }
        .also { it.value }
    val restoreCord = remember {
        mutableStateOf<Rect?>(null)
    }
    Row {
        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.minimizeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_minimize_win1.png"),
                contentDescription = null,
                tint = Color.White
            )
        }

        run {
            // keep lambda and painter in sync
            val showRestore = titleBarBehavior.showRestoreWindow
            Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
                if (showRestore) titleBarBehavior.restoreClicked() else titleBarBehavior.maximizeClicked()

                /*if (window.placement == WindowPlacement.Floating) {
                    val current = Rect(Offset(window.awt.x.toFloat(), window.awt.y.toFloat()), Size(window.awt.width.toFloat(), window.awt.height.toFloat()))
                    val changed = User32.INSTANCE.SetWindowPlacement(
                        WinDef.HWND(Pointer(window.windowHandle)),
                        WinUser.WINDOWPLACEMENT()
                            .apply {
                                flags = WINDOWPLACEMENT.WPF_ASYNCWINDOWPLACEMENT
                                showCmd = SW_MAXIMIZE
                            }
                    )
                    if (changed.booleanValue()) restoreCord.value = current
                } else {
                    User32.INSTANCE.SetWindowPlacement(
                        WinDef.HWND(Pointer(window.windowHandle)),
                        WinUser.WINDOWPLACEMENT()
                            .apply {
                                flags = WINDOWPLACEMENT.WPF_ASYNCWINDOWPLACEMENT
                                showCmd = SW_NORMAL
                                rcNormalPosition = WinDef.RECT()
                                    .apply {
                                        restoreCord.value?.let { rect ->
                                            left = rect.left.toInt() - window.awt.x
                                            top = rect.top.toInt() - window.awt.y
                                            right = rect.right.toInt() - window.awt.x
                                            bottom = rect.bottom.toInt() - window.awt.y
                                        }
                                    }
                            }
                    )
                }
                invalidateFlag.value++
           */ }) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    painter = if (!showRestore)
                        painterResource("drawable/windowcontrol_maximized_win.png")
                    else
                        painterResource("drawable/windowcontrol_restore_down.png"),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.closeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_close2.png"),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun WindowScope.Win32WindowDraggableArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val handler = remember { Win32DragHandler(window) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                handler.register()
            }
        }
    ) {
        content()
    }
}

private class Win32DragHandler(private val window: Window) {
    private var location = window.location.toComposeOffset()
    private var pointStart = MouseInfo.getPointerInfo().location.toComposeOffset()

    private val dragListener = object : MouseMotionAdapter() {
        override fun mouseDragged(event: MouseEvent) = drag()
    }
    private val removeListener = object : MouseAdapter() {
        override fun mouseReleased(event: MouseEvent) {
            window.removeMouseMotionListener(dragListener)
            window.removeMouseListener(this)
        }
    }

    fun register() {
        println("register")
        location = window.location.toComposeOffset()
        pointStart = MouseInfo.getPointerInfo().location.toComposeOffset()
        window.addMouseListener(removeListener)
        window.addMouseMotionListener(dragListener)
    }

    private fun drag() {
        println("drag")
        val point = MouseInfo.getPointerInfo().location.toComposeOffset()
        val location = location + (point - pointStart)
        val hwnd = (window as ComposeWindow).windowHandle
        User32.INSTANCE.MoveWindow(WinDef.HWND(Pointer(hwnd)), location.x, location.y, window.width, window.height, true)
    }

    private fun Point.toComposeOffset() = IntOffset(x, y)
}
