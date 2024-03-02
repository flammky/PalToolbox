package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasProperty
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasWriter
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict

object Skip

sealed class SkipDict : OpenGvasDict()

class SkipMapDict(
    val skipType: String,
    val keyType: String,
    val valueType: String,
    val id: String?,
    val value: ByteArray
) : SkipDict()

class SkipArrayDict(
    val skipType: String,
    val arrayType: String,
    val id: String?,
    val value: ByteArray
) : SkipDict()

class SkipStructDict(
    val skipType: String,
    val structType: String,
    val structId: String,
    val id: String?,
    val value: ByteArray
) : SkipDict()

fun Skip.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {

    val dict = when (typeName) {
        "ArrayProperty" -> {
            SkipArrayDict(
                skipType = typeName,
                arrayType = reader.fstring(),
                id = reader.uuidOrNull()?.toString(),
                value = reader.readBytes(size)
            )
        }
        "MapProperty" -> {
            SkipMapDict(
                skipType = typeName,
                keyType = reader.fstring(),
                valueType = reader.fstring(),
                id = reader.uuidOrNull()?.toString(),
                value = reader.readBytes(size)
            )
        }
        "StructProperty" -> {
            SkipStructDict(
                skipType = typeName,
                structType = reader.fstring(),
                structId = reader.uuid().toString(),
                id = reader.uuidOrNull()?.toString(),
                value = reader.readBytes(size)
            )
        }
        else -> error("Expected ArrayProperty or MapProperty or StructProperty, got $typeName in $path")
    }

    return GvasProperty(
        typeName,
        dict
    )
}

fun Skip.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}

