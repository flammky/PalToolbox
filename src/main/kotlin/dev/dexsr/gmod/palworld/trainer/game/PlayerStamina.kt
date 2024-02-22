package dev.dexsr.gmod.palworld.trainer.game

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import kotlinx.coroutines.delay

suspend fun DefaultPalworldTrainer.infStaminaStatValue(
    sp: Int,
    delay: Long
) {
    val kernel32 = Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")

    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    try {
        val moduleBaseAddr = kernel32.getModuleBaseAddress(processName, procId)
            ?: error("Cannot get Module Base Address")

        val baseAddrOffset = 0x0891EB30

        var r = moduleBaseAddr
        val pointerOffsets = listOf(baseAddrOffset, 0x20, 0x58, 0x68, 0xB0, 0xF8, 0x30)
        val pointerOffset = 0x300

        pointerOffsets.fastForEach { off ->
            val buf = Memory(8)
            kernel32.ReadProcessMemory(procHandle, r.share(off.toLong()), buf, buf.size().toInt(), null)
            r = Pointer(buf.getLong(0))
        }
        r = r.share(pointerOffset.toLong())

        while (true) {
            if (kernel32.ReadProcessMemory(procHandle, r, Memory(8), 8, null)) {
                val buf = Memory(4).apply { setInt(0, sp) }
                kernel32.WriteProcessMemory(procHandle, r, buf, 4, null)
            } else break
            delay(delay)
        }
    } finally {
        kernel32.CloseHandle(procHandle)
    }
}

suspend fun DefaultPalworldTrainer.instancePeriodicSetPlayerStamina(
    // value is stored as UInt
    stamina: Int?,
    delay: Long
) {
    val kernel32 = Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")

    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    try {
        val moduleBaseAddr = kernel32.getModuleBaseAddress(processName, procId)
            ?: error("Cannot get Module Base Address")

        val baseAddrOffset = 0x089139C0

        var r = moduleBaseAddr
        val pointerOffsets = listOf(baseAddrOffset, 0x0, 0x20, 0x198, 0xC0, 0x8, 0x6C8)
        val pointerOffset = 0x2F0

        pointerOffsets.fastForEach { off ->
            val buf = Memory(8)
            if (!kernel32.ReadProcessMemory(procHandle, r.share(off.toLong()), buf, buf.size().toInt(), null)) {
                return
            }
            r = Pointer(buf.getLong(0))
        }
        r = r.share(pointerOffset.toLong())

        var stamina = stamina?.times(1000)
        while (true) {
            if (kernel32.ReadProcessMemory(procHandle, r, Memory(8), 8, null)) {
                if (stamina == null) {
                    stamina = getPlayerStaminaStatValue(
                        getPlayerStaminaStatAddr(kernel32, procHandle, moduleBaseAddr) ?: break,
                        kernel32, procHandle
                    ) ?: break
                }
                val buf = Memory(4).apply { setInt(0, stamina) }
                kernel32.WriteProcessMemory(procHandle, r, buf, 4, null)
            } else break
            delay(delay)
        }
    } finally {
        kernel32.CloseHandle(procHandle)
    }
}

// TODO put player stat value
fun DefaultPalworldTrainer.infStamina2() {
    val kernel32 = Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")

    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    try {
        val injectAddr = Pointer(0x7FF65CA6BEC0)

        val code = byteArrayOf(
            0xC7.toByte(), 0x81.toByte(), 0xF0.toByte(), 0x02, 0x00, 0x00, 0x40, 0x42, 0x0F, 0x00, // mov [rcx+2F0], (int) 1000000,
            0x48, 0x8B.toByte(), 0x81.toByte(), 0xF0.toByte(), 0x02, 0x00, 0x00,
            0x48, 0x8B.toByte(), 0xC2.toByte(),
            0xC3.toByte(),
            0xCC.toByte(),
            0xCC.toByte()
        )

        val written = IntByReference()
        val write = kernel32.WriteProcessMemory(
            procHandle,
            injectAddr,
            code,
            code.size,
            written
        )
    } finally {
        kernel32.CloseHandle(procHandle)
    }
}

fun DefaultPalworldTrainer.revertInfStamina2() {
    val kernel32 = Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")

    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    try {
        val injectAddr = Pointer(0x7FF65CA6BEC0)

        val code = byteArrayOf(
            0x48, 0x8B.toByte(), 0x81.toByte(), 0xF0.toByte(), 0x02, 0x00, 0x00,
            0x48, 0x89.toByte(), 0x02,
            0x48, 0x8B.toByte(), 0xC2.toByte(),
            0xC3.toByte(),
            0xCC.toByte(),
            0xCC.toByte()
        )

        val written = IntByReference()
        val write = kernel32.WriteProcessMemory(
            procHandle,
            injectAddr,
            code,
            code.size,
            written
        )
    } finally {
        kernel32.CloseHandle(procHandle)
    }
}

private fun DefaultPalworldTrainer.getPlayerStaminaStatAddr(
    kernel32: JnaKernel32Ext,
    procHandle: WinNT.HANDLE,
    moduleAddr: Pointer
): Pointer? {
    val baseAddrOffset = 0x0891FFC0

    var r = moduleAddr
    val pointerOffsets = listOf(baseAddrOffset, 0x0, 0x50, 0x698, 0x108)
    val pointerOffset = 0x390

    pointerOffsets.fastForEach { off ->
        val buf = Memory(8)
        if (!kernel32.ReadProcessMemory(procHandle, r.share(off.toLong()), buf, buf.size().toInt(), null)) {
            return null
        }
        r = Pointer(buf.getLong(0))
    }
    r = r.share(pointerOffset.toLong())
    return r
}

private fun DefaultPalworldTrainer.getPlayerStaminaStatValue(
    addr: Pointer,
    kernel32: JnaKernel32Ext,
    procHandle: HANDLE
): Int? {
    val buf = Memory(8)
    val read = kernel32.ReadProcessMemory(procHandle, addr, buf, buf.size().toInt(), null)
    return if (read) buf.getInt(0) else null
}