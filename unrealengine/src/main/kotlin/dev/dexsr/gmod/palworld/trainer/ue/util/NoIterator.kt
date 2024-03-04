package dev.dexsr.gmod.palworld.trainer.ue.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// optimize iteration on collection such as ArrayList
// do note that certain collection structure such as Node based LinkedList will be much slower

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> Array<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> Array<T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(index, item)
    }
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> Array<T>.fastMap(transform: (T) -> R): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEach {
        target += transform(it)
    }
    return target
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T, reified R> Array<T>.fastMapToTypedArray(transform: (T) -> R): Array<R> {
    contract { callsInPlace(transform) }
    val target = arrayOfNulls<R>(size)
    fastForEachIndexed { i, v ->
        target[i] = transform(v)
    }
    return target as Array<R>
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun ByteArray.fastForEach(action: (Byte) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> ByteArray.fastMap(transform: (Byte) -> T): List<T> {
    contract { callsInPlace(transform) }
    val target = ArrayList<T>(size)
    fastForEach {
        target += transform(it)
    }
    return target
}

/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun CharSequence.fastForEach(action: (Char) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}


/**
    replace iterator with IntRange.
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <T> CharSequence.fastMap(transform: (Char) -> T): List<T> {
    contract { callsInPlace(transform) }
    val target = ArrayList<T>(length)
    fastForEach {
        target += transform(it)
    }
    return target
}