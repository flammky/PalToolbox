package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FoliageModelInstance

class FoliageModelInstanceData(
    val modelInstanceId: String,
    val worldTransform: FoliageModelInstanceWorldTransform,
    val hp: Int,
) : OpenGvasDict()

class FoliageModelInstanceWorldTransform(
    val rotator: FoliageModelInstanceRotator,
    val vector: FoliageModelInstanceVector,
    val scaleX: Float
) : OpenGvasDict()

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
    val dataBytes = value.value
        .cast<GvasArrayDict>().value
        .cast<GvasAnyArrayPropertyValue>().values
        .cast<GvasByteArrayValue>().value
    value.value = decodeBytes(reader, dataBytes)
    return value
}

private fun FoliageModelInstance.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): FoliageModelInstanceData {
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))
    val modelInstanceId =  reader.uuid().toString()
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

fun FoliageModelInstance.decode(
    reader: GvasReader,
    typeName: String,
    map: GvasMap<String, Any>
): Int {
    TODO()
}