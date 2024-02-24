package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ItemContainerSlot

class GvasItemContainerSlotData(
    val permission: GvasItemContainerPermission,
    val corruptionProgressValue: Float
) : OpenGvasDict()

fun ItemContainerSlot.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String,
) : GvasProperty {
    require(typeName == "ArrayProperty") {
        "ItemContainerSlot.decode: expected ArrayProperty got $typeName"
    }
    val value = reader.property(typeName, size, path, nestedCallerPath =  path)
    val dataBytes = value.value
        .cast<GvasArrayDict>().value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = decodeBytes(reader, dataBytes)
    return value
}

private fun ItemContainerSlot.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
): GvasDict? {
    if (bytes.isEmpty()) return null
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    val data = GvasItemContainerSlotData(
        permission = GvasItemContainerPermission(
            typeA = reader.readByteArray(),
            typeB = reader.readByteArray(),
            itemStaticIds = reader.readArray { reader.fstring() }
        ),
        corruptionProgressValue = reader.readFloat()
    )
    check(reader.isEof()) {
        "EOF not reached"
    }
    return data
}

fun ItemContainerSlot.encode(
    reader: GvasReader,
    typeName: String,
    map: GvasMap<String, Any>
): Int {
    TODO()
}