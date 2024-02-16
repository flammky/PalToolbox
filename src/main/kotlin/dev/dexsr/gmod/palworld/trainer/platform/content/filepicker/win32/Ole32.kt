package dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.win32

import com.sun.jna.Native
import com.sun.jna.Pointer


object Ole32 {
    init {
        Native.register("ole32")
    }

    external fun OleInitialize(pvReserved: Pointer?): Pointer?
    external fun CoTaskMemFree(pv: Pointer?)
}