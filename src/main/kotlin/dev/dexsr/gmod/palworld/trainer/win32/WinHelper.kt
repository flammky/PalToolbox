package dev.dexsr.gmod.palworld.trainer.win32

import kotlin.jvm.optionals.getOrNull

object WinHelper

class Win32ProcessHandle {

}

typealias jProcessHandle = java.lang.ProcessHandle

fun WinHelper.queryProcessByExecName(name: String): List<WindowProcessInfo> {
    val result = mutableListOf<WindowProcessInfo>()
    jProcessHandle.allProcesses().forEach { proc ->
        val exec = proc.info()?.command()?.getOrNull()
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

fun WinHelper.isProcessAlive(pid: Long, execPath: String): Boolean {
    return jProcessHandle.allProcesses().anyMatch { proc ->
        if (pid != proc.pid())
            return@anyMatch  false
        if (execPath != proc.info()?.command()?.getOrNull())
            return@anyMatch false
        true
    }
}