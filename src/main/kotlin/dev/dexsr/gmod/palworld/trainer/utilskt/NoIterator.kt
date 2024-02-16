package dev.dexsr.gmod.palworld.trainer.utilskt

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// replace iterator with IntRange.
// only use on collection that has fast random access such as ArrayList, don't use on Node collection such as LinkedList
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

// replace iterator with IntRange.
@OptIn(ExperimentalContracts::class)
inline fun <T> Array<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

