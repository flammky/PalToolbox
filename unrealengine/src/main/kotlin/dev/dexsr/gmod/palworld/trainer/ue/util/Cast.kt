package dev.dexsr.gmod.palworld.trainer.ue.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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