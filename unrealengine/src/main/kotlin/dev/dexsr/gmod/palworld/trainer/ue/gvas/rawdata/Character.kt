package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Character

sealed class CharacterDict : OpenGvasDict()

class GvasCharacterData(
    val `object`: GvasMap<String, GvasProperty>,
    val unknownBytes: ByteArray,
    val groupId: String
): CharacterDict()

fun Character.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
    check(typeName == "ArrayProperty") {
        "CharacterKt.encode: Expected ArrayProperty, got$typeName"
    }
    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value.cast<GvasArrayDict>()
    val dataBytes = arrayDict.value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = ByteArrayRawData(
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

fun Character.encode(
    reader: GvasWriter,
    typeName: String,
    map: GvasProperty
): Int {
    TODO()
}

private fun Character.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): GvasCharacterData {
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    val data = GvasCharacterData(
        `object` = reader.properties(""),
        unknownBytes = reader.readBytes(4),
        groupId = reader.uuid().toString()
    )
    check(reader.isEof()) {
        "EOF not reached while decoding Character bytes"
    }
    return data
}