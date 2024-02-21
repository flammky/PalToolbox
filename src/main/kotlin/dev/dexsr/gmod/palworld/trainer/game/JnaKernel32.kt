package dev.dexsr.gmod.palworld.trainer.game

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Tlhelp32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

interface JnaKernel32Ext : Kernel32 {
    override fun GetProcessId(hProcess: WinNT.HANDLE?): Int

    fun WriteProcessMemory(
        hProcess: WinNT.HANDLE?,
        lpBaseAddress: Pointer?,
        lpBuffer: ByteArray?,
        nSize: Int,
        lpNumberOfBytesWritten: IntByReference?
    ): Boolean

    fun ReadProcessMemory(
        hProcess: WinNT.HANDLE?,
        lpBaseAddress: Pointer?,
        lpBuffer: ByteArray?,
        nSize: Int,
        lpNumberOfBytesRead: IntByReference?
    ): Boolean

    fun VirtualProtectEx(
        hProcess: WinNT.HANDLE?,
        lpAddress: Pointer?,
        dwSize: BaseTSD.SIZE_T,
        flNewProtect: Int,
        lpflOldProtect: IntByReference?
    ): Boolean

    companion object {
        val INSTANCE by lazy { Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS) }
    }
}

internal fun Kernel32.getModuleBaseAddress(moduleName: String, processId: Int): Pointer? {
    val snapshot = CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPMODULE, WinDef.DWORD(processId.toLong()))
    val me32 = Tlhelp32.MODULEENTRY32W()
    while (Module32NextW(snapshot, me32)) {
        if (Native.toString(me32.szModule).equals(moduleName, ignoreCase = true)) {
            CloseHandle(snapshot)
            return me32.modBaseAddr
        }
    }
    CloseHandle(snapshot)
    return null
}

internal fun Kernel32.getProcIdByExecName(processName: String): Int? {
    val snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, WinDef.DWORD(0))

    val pe32 = Tlhelp32.PROCESSENTRY32()
    while (Process32Next(snapshot, pe32)) {
        if (Native.toString(pe32.szExeFile).equals(processName, ignoreCase = true)) {
            CloseHandle(snapshot)
            return pe32.th32ProcessID.toInt()
        }
    }
    CloseHandle(snapshot)
    return null
}