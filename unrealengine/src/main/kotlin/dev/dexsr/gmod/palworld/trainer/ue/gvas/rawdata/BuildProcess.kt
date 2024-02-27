package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BuildProcess

sealed class BuildProcessDict : OpenGvasDict()

class BuildProcessData(
    val state: Byte,
    val id: String
) : BuildProcessDict()

fun BuildProcess.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): BuildProcessDict {

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = BuildProcessData(
        state = reader.readByte(),
        id = reader.uuid().toString()
    )

    check(reader.isEof()) {
        "EOF not reached"
    }

    return data
}