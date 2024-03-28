package dev.dexsr.gmod.palworld.toolbox.savegame.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFile

fun WriteGvasFile(
    writer: GvasWriter,
    file: GvasFile
) {
    WriteGvasHeader(writer, file.header)
    WriteGvasProperties(writer, file.properties)
    writer.writeBytes(file.trailer)
}