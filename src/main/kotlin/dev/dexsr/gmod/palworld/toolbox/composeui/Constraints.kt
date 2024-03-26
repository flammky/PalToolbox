package dev.dexsr.gmod.palworld.toolbox.composeui

import androidx.compose.ui.unit.Constraints

fun Constraints.noMinConstraints() = copy(minWidth = 0, minHeight = 0)