package dev.dexsr.gmod.palworld.trainer.game

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.ptr.IntByReference

/**
    ignore value move from player inventory, the injected address are used when:
        - throwing pal sphere
        - reload (this will make the weapon not reload, so you need to enable the disallow consume clip as well)
        - building
 */
suspend fun DefaultPalworldTrainer.noInventoryConsume() {
    val kernel32 = JnaKernel32Ext.INSTANCE
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")
    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)
    try {
        val injectAddr = Pointer(0x7FF65CCC3D6F)

        val code = byteArrayOf(
            0x41.toByte(), 0xB8.toByte(), 0x00, 0x00, 0x00, 0x00,
            0xE9.toByte(), 0x00, 0x00, 0x00, 0x00,
            0x90.toByte()
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

suspend fun DefaultPalworldTrainer.revertNoInventoryConsume() {
    val kernel32 = JnaKernel32Ext.INSTANCE
    val processName = "Palworld-Win64-Shipping.exe"
    val procId = kernel32.getProcIdByExecName(processName)
        ?: error("${"Palworld-Win64-Shipping.exe"} is not running")
    val procHandle = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, procId)

    try {
        val injectAddr = Pointer(0x7FF65CCC3D6F)

        val originalCode = byteArrayOf(
            0x44, 0x8B.toByte(), 0x80.toByte(), 0x04, 0x01, 0x00, 0x00, // mov r8d, [rax+00000104]
            0x45, 0x85.toByte(), 0xC0.toByte(), // test r8d,r8d
            0x74, 0x70 // je 7FF65CCC3DEB
        )

        val bytesWritten = IntByReference()
        val write = kernel32.WriteProcessMemory(
            procHandle,
            injectAddr,
            originalCode,
            originalCode.size,
            bytesWritten
        )
    } finally {
        kernel32.CloseHandle(procHandle)
    }
}