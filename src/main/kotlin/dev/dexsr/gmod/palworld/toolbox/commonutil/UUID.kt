package dev.dexsr.gmod.palworld.toolbox.commonutil

object UUIDUtil {

    fun stripSeparator(uid: String) = when(uid.length) {
        32 -> {
            uid.forEachIndexed { i, c ->
                if (!c.isLetterOrDigit()) {
                    illegalArgument("unexpected char (c=$c, i=$i)")
                }
            }
            uid
        }
        36 -> {
            buildString { uid.forEachIndexed { i, c ->
                if (i == 8 || i == 13 || i == 18 || i == 23) {
                    if (c != '-') illegalArgument("unexpected char on separator position (c=$c, i=$i)")
                    return@forEachIndexed
                }
                if (!c.isLetterOrDigit()) {
                    illegalArgument("unexpected char (c=$c, i=$i)")
                }
                append(c)
            } }
        }
        else -> illegalArgument("Invalid UID length=${uid.length}")
    }

    fun stripSeparatorOrNull(uid: String): String? = when(uid.length) {
        32 -> {
            uid.forEach { c ->
                if (!c.isLetterOrDigit()) {
                    return null
                }
            }
            uid
        }
        36 -> {
            buildString { uid.forEachIndexed { i, c ->
                if (i == 8 || i == 13 || i == 18 || i == 23) {
                    if (c != '-') return null
                    return@forEachIndexed
                }
                if (!c.isLetterOrDigit()) {
                    return null
                }
                append(c)
            } }
        }
        else -> null
    }

    fun putSeparator(uid: String): String = if (uid.length == 36) {
        uid.forEachIndexed { i, c ->
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                if (c != '-') illegalArgument("unexpected char on separator position (c=$c, i=$i)")
                return@forEachIndexed
            }
            if (!c.isLetterOrDigit()) {
                illegalArgument("unexpected char (c=$c, i=$i)")
            }
        }
        uid
    } else if (uid.length == 32) {
        buildString { uid.forEachIndexed { i, c ->
            if (i == 8 || i == 12 || i == 16 || i == 20) {
                append('-')
            }
            append(c)
        } }
    } else illegalArgument("cannot put separator: Invalid UID length=${uid.length}")
}