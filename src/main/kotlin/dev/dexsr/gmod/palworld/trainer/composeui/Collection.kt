package dev.dexsr.gmod.palworld.trainer.composeui

import androidx.compose.runtime.Stable

// TODO: use kotlinx immutable collection library instead ?
@Stable
class StableList <T> (val content: List<T>): List<T> by content