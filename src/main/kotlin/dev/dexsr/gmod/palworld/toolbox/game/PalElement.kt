package dev.dexsr.gmod.palworld.toolbox.game

sealed class PalElement {

    data object DARK : PalElement()
    data object DRAGON : PalElement()
    data object GROUND : PalElement()
    data object ELECTRIC : PalElement()
    data object FIRE : PalElement()
    data object ICE : PalElement()
    data object GRASS : PalElement()
    data object NEUTRAL : PalElement()
    data object WATER : PalElement()
}