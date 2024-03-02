package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WorkerDirector

sealed class WorkerDirectorDict() : OpenGvasDict()

class WorkerDirectorData(
    val id: String,
    val spawnTransform: GvasTransform,
    val currentOrderType: Byte,
    val currentBattleType: Byte,
    val containerId: String
) : WorkerDirectorDict()

fun WorkerDirector.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
) : GvasProperty {
    check(typeName == "ArrayProperty") {
        "WorkerDirector_decode: expected ArrayProperty, got=$typeName"
    }
    val value = reader.property(typeName, size, path, nestedCallerPath = path)
    val arrayDict = value
        .value
        .cast<GvasArrayDict>()
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

private fun WorkerDirector.decodeBytes(
    parentReader: GvasReader,
    dataBytes: ByteArray
): WorkerDirectorData {
    val reader = parentReader.copy(ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = WorkerDirectorData(
        id = reader.uuid().toString(),
        spawnTransform = reader.ftransform(),
        currentOrderType = reader.readByte(),
        currentBattleType = reader.readByte(),
        containerId = reader.uuid().toString()
    )

    check(reader.isEof()) {
        "WorkerDirector_decodeBytes: EOF not reached"
    }

    return data
}

fun WorkerDirector.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}