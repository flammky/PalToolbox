package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FoliageModelInstance

sealed class FoliageModelInstanceDict() : OpenGvasDict()


class FoliageModelInstanceData(
    val modelInstanceId: String,
    val worldTransform: FoliageModelInstanceWorldTransform,
    val hp: Int,
) : FoliageModelInstanceDict()

class FoliageModelInstanceWorldTransform(
    val rotator: FoliageModelInstanceRotator,
    val vector: FoliageModelInstanceVector,
    val scaleX: Float
) : FoliageModelInstanceDict()

class FoliageModelInstanceRotator(
    val pitch: Float?,
    val yaw: Float?,
    val roll: Float?
)

class FoliageModelInstanceVector(
    val x: Float?,
    val y: Float?,
    val z: Float?
)

fun FoliageModelInstance.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
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
        customType = typeName,
        id = arrayDict.id,
        value = decodeBytes(reader, dataBytes)
    )
    return value
}

private fun FoliageModelInstance.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): FoliageModelInstanceData {
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    val modelInstanceId =  reader.uuid().toString()
    println("guid=$modelInstanceId")
    val (pitch, yaw, roll) = reader.compressedShortRotator()
    val (x, y, z) = reader.packedVector(1)
    val worldTransform = FoliageModelInstanceWorldTransform(
        rotator = FoliageModelInstanceRotator(pitch, yaw, roll),
        vector = FoliageModelInstanceVector(x, y, z),
        scaleX = reader.readFloat()
    )
    val hp = reader.readInt()
    check(reader.isEof()) {
        "EOF not Reached"
    }
    return FoliageModelInstanceData(
        modelInstanceId,
        worldTransform,
        hp
    )
}

fun FoliageModelInstance.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}