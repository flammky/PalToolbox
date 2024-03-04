package dev.dexsr.gmod.palworld.toolbox.composeui

/*package dev.dexsr.klio.base.composeui*/

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


typealias ComposeUiStableAny<T> = StableAny<T>
typealias ComposeUiImmutableAny<T> = ImmutableAny<T>

// should T: Any ?

// remove this ?

@Stable
open class StableAny<T>(val value: T)

@Immutable
class ImmutableAny<T>(val value: T)

fun <T> T.wrapComposeUiStable() = ComposeUiStableAny(this)

fun <T> T.wrapComposeUiImmutable() = ImmutableAny(this)