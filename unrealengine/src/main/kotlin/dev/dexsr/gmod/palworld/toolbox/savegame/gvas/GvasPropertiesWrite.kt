package dev.dexsr.gmod.palworld.toolbox.savegame.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFileProperties

fun WriteGvasProperties(
    writer: GvasWriter,
    property: GvasFileProperties
) {
    writer.writeGvasPropertyMap(property)
}