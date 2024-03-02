package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object CharacterContainer

sealed class CharacterContainerDict : OpenGvasDict()

class CharacterContainerData(
    val playerUid: String,
    val instanceId: String,
    val permissionTribeId: Byte
) : CharacterContainerDict()

fun CharacterContainer.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
) : GvasProperty {

    if (typeName != "ArrayProperty") {
        error("Expected: ArrayProperty, got=$typeName")
    }

    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value.cast<GvasArrayDict>()
    val dataBytes = arrayDict.value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value

    value.value = CustomByteArrayRawData(
        customType = path,
        id = arrayDict.id,
        value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(decodeBytes(reader, dataBytes))
        )
    )

    return value
}

fun CharacterContainer.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}

private fun decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): CharacterContainerData? {
    if (bytes.isEmpty()) return null

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = CharacterContainerData(
        playerUid = reader.uuid().toString(),
        instanceId = reader.uuid().toString(),
        permissionTribeId = reader.readByte()
    )

    check(reader.isEof()) {
        "EOF not reached"
    }

    return data
}