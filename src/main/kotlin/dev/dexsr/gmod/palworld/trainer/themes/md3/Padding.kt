package dev.dexsr.gmod.palworld.trainer.themes.md3

object Padding {
    val MD3_SPEC_PADDING_INCREMENT_VALUE_DP = 4F
}

val MD3Spec.padding
    get() = Padding

fun Padding.incrementsDp(n: Int): Float = n * Padding.MD3_SPEC_PADDING_INCREMENT_VALUE_DP