package dev.dexsr.gmod.palworld.toolbox.game

sealed class PalGender {

    data object Male : PalGender()
    data object Female : PalGender()

    // black merchant / dark trader
    /*data object Unspecified : PalGender()*/

    data class Named(val name: String): PalGender()

    companion object
}

fun PalGender.Companion.parseKnownOrNull(str: String): PalGender? = when(str) {
    "EPalGenderType::Female" -> PalGender.Female
    "EPalGenderType::Male" -> PalGender.Male
    else -> null
}

fun PalGender.Companion.parseOrNamed(str: String): PalGender = when(str) {
    "EPalGenderType::Female" -> PalGender.Female
    "EPalGenderType::Male" -> PalGender.Male
    else -> PalGender.Named(str)
}