package dev.dexsr.gmod.palworld.toolbox.savegame.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GVAS_PROPERTY_DECODER
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GVAS_PROPERTY_ENCODER
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.PALWORLD_CUSTOM_PROPERTY_CODEC
import java.io.ByteArrayOutputStream

fun ConvertGvasToSavFile(
    gvas: GvasFile,
    customPropertiesCodec: Map<String, Pair<GVAS_PROPERTY_DECODER, GVAS_PROPERTY_ENCODER>> = PALWORLD_CUSTOM_PROPERTY_CODEC
) {
    val outStream = ByteArrayOutputStream()
    val writer = GvasWriter(
        customPropertiesCodec,
        outStream
    )

    WriteGvasFile(writer, gvas)

    val saveType = if (
        "Pal.PalWorldSaveGame" in gvas.header.saveGameClassName ||
        "Pal.PalLocalWorldSaveGame" in gvas.header.saveGameClassName
    ) 0x32 else 0x31
}