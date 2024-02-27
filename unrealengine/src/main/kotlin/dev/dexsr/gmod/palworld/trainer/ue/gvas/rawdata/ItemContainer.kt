package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ItemContainer

sealed class ItemContainerDict() : OpenGvasDict()


class GvasItemContainerData(
    val permission: GvasItemContainerPermission
) : ItemContainerDict()

class GvasItemContainerPermission(
    val typeA: ByteArray,
    val typeB: ByteArray,
    val itemStaticIds: ArrayList<String>
)

fun ItemContainer.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String,
) : GvasProperty {
    require(typeName == "ArrayProperty") {
        "ItemContainer.decode: ExpectedArrayProperty, got $typeName"
    }
    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value
        .cast<GvasArrayDict>()
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

private fun ItemContainer.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
): GvasItemContainerData? {
    if (bytes.isEmpty()) return null
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    val data = GvasItemContainerData(
        permission = GvasItemContainerPermission(
            typeA = reader.readByteArray(),
            typeB = reader.readByteArray(),
            itemStaticIds = reader.readArray { reader.fstring() }
        )
    )
    check(reader.isEof()) {
        "EOF not reached"
    }
    return data
}

fun ItemContainer.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}