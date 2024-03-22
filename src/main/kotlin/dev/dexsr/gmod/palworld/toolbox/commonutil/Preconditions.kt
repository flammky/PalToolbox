package dev.dexsr.gmod.palworld.toolbox.commonutil

@Suppress("NOTHING_TO_INLINE")
inline fun illegalArgument(msg: Any): Nothing = throw IllegalArgumentException(msg.toString())