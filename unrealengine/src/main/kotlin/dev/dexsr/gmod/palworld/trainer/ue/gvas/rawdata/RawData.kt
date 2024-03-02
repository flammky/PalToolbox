package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasDict
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict

sealed class CustomRawData(
    val customType: String
) : OpenGvasDict()

class CustomByteArrayRawData(
    customType: String,
    val id: String?,
    val value: GvasDict?,
): CustomRawData(customType)