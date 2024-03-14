package dev.dexsr.gmod.palworld.toolbox.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// TODO: same linter as the cast (as/as?) operator

@OptIn(ExperimentalContracts::class)
internal inline fun <reified R> Any?.cast(): R {
    contract {
        returns() implies (this@cast is R)
    }
    return this as R
}

@OptIn(ExperimentalContracts::class)
internal inline fun <reified R> Any?.castOrNull(): R? {
    contract {
        returns() implies (this@castOrNull is R?)
    }
    return this as? R
}

@OptIn(ExperimentalContracts::class)
internal inline fun <reified R> Any?.castOrElse(
    block: () -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        returns() implies (this@castOrElse is R)
    }
    return if (this is R) this else block()
}