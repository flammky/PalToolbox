package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasTransform
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MapModel

sealed class MapModelDict : OpenGvasDict()

class MapModelData(
    val instanceId: String,
    val concreteModelInstanceId: String,
    val baseCampIdBelongTo: String,
    val groupIdBelongTo: String,
    val hp: MapModelHpData,
    val initialTransformCache: GvasTransform,
    val repairWorkId: String,
    val ownerSpawnerLevelObjectInstanceId: String,
    val ownerInstanceId: String,
    val buildPlayerUid: String,
    val interactRestrictType: Byte,
    val stageInstanceIdBelongTo: StageInstanceIdOwner,
    val createdAt: Long
) : MapModelDict()

class MapModelHpData(
    val current: Int,
    val max: Int
) : MapModelDict()

class StageInstanceIdOwner(
    val id: String,
    val valid: Boolean
) : MapModelDict()

fun MapModel.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): MapModelDict {

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = MapModelData(
        instanceId = reader.uuid().toString(),
        concreteModelInstanceId = reader.uuid().toString(),
        baseCampIdBelongTo = reader.uuid().toString(),
        groupIdBelongTo = reader.uuid().toString(),
        hp = MapModelHpData(
            current = reader.readInt(),
            max = reader.readInt()
        ),
        initialTransformCache = reader.ftransform(),
        repairWorkId = reader.uuid().toString(),
        ownerSpawnerLevelObjectInstanceId = reader.uuid().toString(),
        ownerInstanceId = reader.uuid().toString(),
        buildPlayerUid = reader.uuid().toString(),
        interactRestrictType = reader.readByte(),
        stageInstanceIdBelongTo = StageInstanceIdOwner(
            id = reader.uuid().toString(),
            valid = reader.readInt() > 0
        ),
        createdAt = reader.readLong()
    )

    check(reader.isEof()) {
        "EOF not reached"
    }

    return data
}
