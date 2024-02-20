package dev.dexsr.gmod.palworld.trainer.game

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.win32.W32APIOptions
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import kotlinx.coroutines.delay

// TODO: impl for NOP writer

/**
    default impl for writing [maxHunger] periodically with [delay]
 */
suspend fun DefaultPalworldTrainer.instancePeriodicSetHunger(
    maxHunger: Int,
    delay: Long
) {
    val kernel32 = Native.load("kernel32", JnaKernel32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")

    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    val moduleBaseAddr = kernel32.getModuleBaseAddress(processName, procId)
        ?: error("Cannot get Module Base Address")

    val baseAddrOffset = 0x0891EB30

    var r = moduleBaseAddr
    val pointerOffsets = listOf(baseAddrOffset, 0X20, 0x58, 0x68, 0xB0, 0xF8, 0x30)
    val pointerOffset = 0x300

    pointerOffsets.fastForEach { off ->
        val buf = Memory(8)
        kernel32.ReadProcessMemory(procHandle, r.share(off.toLong()), buf, buf.size().toInt(), null)
        r = Pointer(buf.getLong(0))
    }
    r = r.share(pointerOffset.toLong())

    while (true) {
        if (kernel32.ReadProcessMemory(procHandle, r, Memory(8), 8, null)) {
            val buf = Memory(4).apply { setFloat(0, maxHunger.toFloat()) }
            kernel32.WriteProcessMemory(procHandle, r, buf, 4, null)
        } else break
        delay(delay)
    }

    kernel32.CloseHandle(procHandle)
}