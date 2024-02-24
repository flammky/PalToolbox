package dev.dexsr.gmod.palworld.toolbox.game

sealed class PalDropKind {

    data object NORMAL : PalDropKind()
    data object BOSS : PalDropKind()
}