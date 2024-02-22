package dev.dexsr.gmod.palworld.trainer.win32

import kotlin.jvm.optionals.getOrNull

object WinHelper

fun WinHelper.queryProcessByExecName(name: String): List<WindowProcessInfo> {
    val result = mutableListOf<WindowProcessInfo>()
    ProcessHandle.allProcesses().forEach { proc ->
        val exec = proc.info().command().getOrNull()
            ?: return@forEach
        val execName = exec.takeLastWhile { it != '\\' }
        if (execName != name) return@forEach
        val pid = proc.pid() ?: return@forEach
        result.add(
            WindowProcessInfo(
                name = execName,
                path = exec,
                id = pid
            )
        )
    }
    return result
}