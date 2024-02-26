package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BaseCamp

sealed class BaseCampDict : OpenGvasDict()

class BaseCampData(
    val id: String,
    val name: String,
    val state: Byte,
    val transform: GvasTransform,
    val areaRange: Float,
    val groupIdBelongTo: String,
    val fastTravelLocalTransform: GvasTransform,
    val ownerMapObjectInstanceId: String
) : BaseCampDict()

fun BaseCamp.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
    require(typeName == "ArrayProperty") {
        "Expected ArrayProperty, got=$typeName"
    }

    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value
        .cast<GvasArrayDict>()
    val dataBytes = arrayDict.value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = ByteArrayRawData(
        customType = typeName,
        id = arrayDict.id,
        value = decodeBytes(reader, dataBytes)
    )
    return value
}

private fun BaseCamp.decodeBytes(
    parentReader: GvasReader,
    dataBytes: ByteArray
): BaseCampData {
    val reader = parentReader.copy(ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN))
    val data = BaseCampData(
        id = reader.uuid().toString(),
        name = reader.fstring(),
        state = reader.readByte(),
        transform = reader.ftransform(),
        areaRange = reader.readFloat(),
        groupIdBelongTo = reader.uuid().toString(),
        fastTravelLocalTransform = reader.ftransform(),
        ownerMapObjectInstanceId = reader.uuid().toString()
    )
    check(reader.isEof()) {
        "BaseCamp_decodeBytes: EOF not reached"
    }
    return data
}

fun BaseCamp.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}