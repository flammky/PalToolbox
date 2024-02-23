package dev.dexsr.gmod.palworld.trainer.composeui

import androidx.compose.runtime.*

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <T> rememberMutableStateOf(
    vararg keys: Any?,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    noinline initializer: @DisallowComposableCalls () -> T,
) = remember(*keys) { mutableStateOf(initializer()) }