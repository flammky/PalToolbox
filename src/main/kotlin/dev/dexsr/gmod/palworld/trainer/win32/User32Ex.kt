package dev.dexsr.gmod.palworld.trainer.win32

import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser.WindowProc


internal interface User32Ex : User32 {
    fun SetWindowLong(hWnd: HWND?, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun SetWindowLong(hWnd: HWND?, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
    fun SetWindowLongPtr(hWnd: HWND?, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun SetWindowLongPtr(hWnd: HWND?, nIndex: Int, wndProc: LONG_PTR): LONG_PTR
    fun CallWindowProc(proc: LONG_PTR?, hWnd: HWND?, uMsg: Int, uParam: WPARAM?, lParam: LPARAM?): LRESULT

    companion object {
        const val GWLP_WNDPROC: Int = -4
    }
}