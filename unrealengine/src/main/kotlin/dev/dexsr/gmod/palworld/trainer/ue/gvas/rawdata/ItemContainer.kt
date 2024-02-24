package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ItemContainer

class GvasItemContainerData(
    val permission: GvasItemContainerPermission
) : OpenGvasDict()

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
    val dataBytes = value.value
        .cast<GvasArrayDict>().value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = decodeBytes(reader, dataBytes)
    return value
}

private fun ItemContainer.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
): GvasDict? {
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
    reader: GvasReader,
    typeName: String,
    map: GvasMap<String, Any>
): Int {
    TODO()
}