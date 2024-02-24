package dev.dexsr.gmod.palworld.toolbox.game

// https://palworld.fandom.com/wiki/Pals#Pal_Variations

sealed class PalVariation {

    data object ALPHA : PalVariation()

    data object LUCKY : PalVariation()

    data object LEGENDARY : PalVariation()

    data object SUBSPECIES : PalVariation()
}