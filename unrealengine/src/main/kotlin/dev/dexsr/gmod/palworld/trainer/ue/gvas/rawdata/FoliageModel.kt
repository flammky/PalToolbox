package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FoliageModel

sealed class FoliageModelDict() : OpenGvasDict()


class FoliageModelData(
    val modelId: String,
    val foliagePresetType: Byte,
    val cellCoord: FoliageModelCellCoord
) : FoliageModelDict()

class FoliageModelCellCoord(
    val x: Long,
    val y: Long,
    val z: Long
) : FoliageModelDict()

fun FoliageModel.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
    require(typeName == "ArrayProperty") {
        "Expected ArrayProperty, got: $typeName"
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

fun FoliageModel.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}

private fun FoliageModel.decodeBytes(
    parentReader: GvasReader,
    dataBytes: ByteArray
): FoliageModelData {
    val reader = parentReader.copy(ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN))
    val data = FoliageModelData(
        modelId = reader.fstring(),
        foliagePresetType = reader.readByte(),
        cellCoord = FoliageModelCellCoord(
            x = reader.readLong(),
            y = reader.readLong(),
            z = reader.readLong()
        )
    )
    check(reader.isEof()) {
        "FoliageModel_decodeBytes: EOF not reached"
    }
    return data
}