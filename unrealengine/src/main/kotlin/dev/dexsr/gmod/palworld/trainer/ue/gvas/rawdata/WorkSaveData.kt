package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WorkSaveData

sealed class WorkSaveDataDict : OpenGvasDict()
sealed class WorkSaveDataProperty : WorkSaveDataDict()

private val WORK_BASE_TYPES = setOf(
    // "EPalWorkableType::Illegal",
    "EPalWorkableType::Progress",
    // "EPalWorkableType::CollectItem",
    // "EPalWorkableType::TransportItem",
    "EPalWorkableType::TransportItemInBaseCamp",
    "EPalWorkableType::ReviveCharacter",
    // "EPalWorkableType::CollectResource",
    "EPalWorkableType::LevelObject",
    "EPalWorkableType::Repair",
    "EPalWorkableType::Defense",
    "EPalWorkableType::BootUp",
    "EPalWorkableType::OnlyJoin",
    "EPalWorkableType::OnlyJoinAndWalkAround",
    "EPalWorkableType::RemoveMapObjectEffect",
    "EPalWorkableType::MonsterFarm",
)

class WorkSaveDataRaw(
    val values: ByteArray
) : WorkSaveDataProperty()

class WorkSaveDataClass(
    val workableData: WorkSaveDataWorkableData? = null,
    val transform: WorkSaveDataTransform
) : WorkSaveDataProperty()

sealed class WorkSaveDataWorkableData()

class WorkSaveWorkableBase(
    val id: String,
    val workableBounds: WorkSaveBaseWorkableBounds,
    val baseCampIdBelongTo: String,
    val ownerMapObjectModelId: String,
    val ownerMapObjectConcreteModelId: String,
    val currentState: Byte,
    val assignLocations: ArrayList<WorkSaveBaseAssignLocation>,
    val behaviorType: Byte,
    val assignDefineDataId: String,
    val overrideWorkType: Byte,
    val assignableFixedType: Byte,
    val assignableOtomo: Boolean,
    val canTriggerWorkerEvent: Boolean,
    val canStealAssign: Boolean,
    val workableData: WorkSaveBaseWorkableData?,
) : WorkSaveDataWorkableData()

class WorkSaveBaseWorkableBounds(
    val location: GvasVector,
    val rotation: GvasQuat,
    val boxSphereBounds: WorkSaveBaseBoxSphereBounds,
)

class WorkSaveBaseBoxSphereBounds(
    val origin: GvasVector,
    val boxExtent: GvasVector,
    val sphereRadius: Double,
)

class WorkSaveBaseAssignLocation(
    val location: GvasVector,
    val facingDirection: GvasVector
)

sealed class WorkSaveBaseWorkableData : WorkSaveDataWorkableData()

class WorkSaveBaseWorkableDefense(
    val defenseCombatType: Byte
) : WorkSaveBaseWorkableData()
class WorkSaveBaseWorkableProgress(
    val requiredWorkAmount: Float,
    val workExp: Int,
    val currentWorkAmount: Float,
    val autoWorkSelfAmountBySec: Float
) : WorkSaveBaseWorkableData()
class WorkSaveBaseWorkableReviveCharacter(
    val targetIndividualId: TargetIndividualId
) : WorkSaveBaseWorkableData()

class TargetIndividualId(
    val playerUid: String,
    val instanceId: String
)

class WorkSaveWorkableAssign(
    val handleId: String,
    val locationIndex: Int,
    val assignType: Byte,
    val assignedIndividualId: TargetIndividualId,
    val state: Byte,
    val fixed: Int,
) : WorkSaveDataWorkableData()

class WorkSaveWorkableLevelObject(
    val assign: WorkSaveWorkableAssign,
    val targetMapObjectModelId: String
) : WorkSaveDataWorkableData()

class WorkSaveDataTransform(
    val transformType: Byte,
    val v2: Int,
    val data: WorkSaveDataTransformData
)

sealed class WorkSaveDataTransformData()

class WorkSaveDataTransformType1(
    val rotation: GvasQuat,
    val translation: GvasVector,
    val scale3D: GvasVector,
) : WorkSaveDataTransformData()

class WorkSaveDataTransformType2(
    val mapObjectInstanceId: String,
) : WorkSaveDataTransformData()

class WorkSaveDataTransformType3(
    val guid: String,
    val instanceId: String,
) : WorkSaveDataTransformData()

class WorkSaveDataTransformRawData(
    val rawData: ByteArray
) : WorkSaveDataTransformData()

class WorkSaveWorkAssignData(
    val id: String,
    val locationIndex: Int,
    val assignType: Byte,
    val assignedIndividualId: TargetIndividualId,
    val state: Byte,
    val fixed: Boolean
) : WorkSaveDataDict()

fun WorkSaveData.decode(
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
    for (element in arrayDict.value.cast<GvasStructArrayPropertyValue>().values) {
        val workBytes = element.cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>().value
            .cast<GvasArrayDict>().value
            .cast<GvasAnyArrayPropertyValue>().values
            .cast<GvasByteArrayValue>().value
        val workType = element.cast<GvasStructMap>().v["WorkableType"]
            .cast<GvasProperty>().value
            .cast<GvasEnumDict>().enumValue.value

        element.cast<GvasStructMap>().v["RawData"]
            .cast<GvasProperty>().value = decodeBytes(reader, workBytes, workType)

        for (workAssignMap in element.cast<GvasStructMap>().v["WorkAssignMap"].cast<GvasProperty>().value.cast<GvasMapDict>().value) {
            val map = workAssignMap["value"]
                .cast<GvasStructMap>()
                .v

            val workAssignBytes = map["RawData"]
                .cast<GvasProperty>().value
                .cast<GvasArrayDict>().value
                .cast<GvasAnyArrayPropertyValue>().values
                .cast<GvasByteArrayValue>().value

            map["RawData"]
                .cast<GvasProperty>().value = decodeWorkAssignBytes(reader, workAssignBytes)
        }
    }

    value.value = ByteArrayRawData(
        customType = typeName,
        id = arrayDict.id,
        value = value.value
    )

    return value
}

private fun WorkSaveData.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
    workType: String
): WorkSaveDataProperty {

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val workableData = when (workType) {
        in WORK_BASE_TYPES -> {
            WorkSaveWorkableBase(
                id = reader.uuid().toString(),
                workableBounds = WorkSaveBaseWorkableBounds(
                    location = reader.readVector(),
                    rotation = reader.readQuat(),
                    boxSphereBounds = WorkSaveBaseBoxSphereBounds(
                        origin = reader.readVector(),
                        boxExtent = reader.readVector(),
                        sphereRadius = reader.readDouble()
                    )
                ),
                baseCampIdBelongTo = reader.uuid().toString(),
                ownerMapObjectModelId = reader.uuid().toString(),
                ownerMapObjectConcreteModelId = reader.uuid().toString(),
                currentState = reader.readByte(),
                assignLocations = reader.readArray { r -> WorkSaveBaseAssignLocation(
                    location = r.readVector(),
                    facingDirection = r.readVector()
                ) },
                behaviorType = reader.readByte(),
                assignDefineDataId = reader.fstring(),
                overrideWorkType = reader.readByte(),
                assignableFixedType = reader.readByte(),
                assignableOtomo = reader.readInt() > 0,
                canTriggerWorkerEvent = reader.readInt() > 0,
                canStealAssign = reader.readInt() > 0,
                workableData = when(workType) {
                    "EPalWorkableType::Defense" -> WorkSaveBaseWorkableDefense(
                        defenseCombatType = reader.readByte()
                    )

                    "EPalWorkableType::Progress" -> WorkSaveBaseWorkableProgress(
                        requiredWorkAmount = reader.readFloat(),
                        workExp = reader.readInt(),
                        currentWorkAmount = reader.readFloat(),
                        autoWorkSelfAmountBySec = reader.readFloat()
                    )

                    "EPalWorkableType::ReviveCharacter" -> WorkSaveBaseWorkableReviveCharacter(
                        targetIndividualId = TargetIndividualId(
                            playerUid = reader.uuid().toString(),
                            instanceId = reader.uuid().toString()
                        )
                    )

                    else -> null
                }
            )
        }
        "EPalWorkableType::Assign" -> {
            WorkSaveWorkableAssign(
                handleId = reader.uuid().toString(),
                locationIndex = reader.readInt(),
                assignType = reader.readByte(),
                assignedIndividualId = TargetIndividualId(
                    playerUid = reader.uuid().toString(),
                    instanceId = reader.uuid().toString()
                ),
                state = reader.readByte(),
                fixed = reader.readInt(),
            )
        }
        "EPalWorkableType::LevelObject" -> {
            WorkSaveWorkableLevelObject(
                assign = WorkSaveWorkableAssign(
                    handleId = reader.uuid().toString(),
                    locationIndex = reader.readInt(),
                    assignType = reader.readByte(),
                    assignedIndividualId = TargetIndividualId(
                        playerUid = reader.uuid().toString(),
                        instanceId = reader.uuid().toString()
                    ),
                    state = reader.readByte(),
                    fixed = reader.readInt(),
                ),
                targetMapObjectModelId = reader.uuid().toString()
            )
        }
        else -> null
    }

    if (workableData == null) {
        return WorkSaveDataRaw(bytes)
    }

    val transformType = reader.readByte()

    val transform = WorkSaveDataTransform(
        transformType = transformType,
        v2 = 0,
        data = when(transformType.toInt()) {
            1 -> WorkSaveDataTransformType1(
                rotation = reader.readQuat(),
                translation = reader.readVector(),
                scale3D = reader.readVector()
            )
            2 -> WorkSaveDataTransformType2(
                mapObjectInstanceId = reader.uuid().toString()
            )
            3 -> WorkSaveDataTransformType3(
                guid = reader.uuid().toString(),
                instanceId = reader.uuid().toString()
            )
            else -> WorkSaveDataTransformRawData(
                rawData = reader.readBytes(reader.remaining)
            )
        }
    )

    check(reader.isEof()) {
        "EOF not reached for $workType, remainingBytesCount=${reader.remaining}"
    }

    return WorkSaveDataClass(
        workableData = workableData,
        transform = transform
    )
}

private fun WorkSaveData.decodeWorkAssignBytes(
    parentReader: GvasReader,
    bytes: ByteArray
): WorkSaveWorkAssignData {
    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = WorkSaveWorkAssignData(
        id = reader.uuid().toString(),
        locationIndex = reader.readInt(),
        assignType = reader.readByte(),
        assignedIndividualId = TargetIndividualId(
            playerUid = reader.uuid().toString(),
            instanceId = reader.uuid().toString()
        ),
        state = reader.readByte(),
        fixed = reader.readInt() > 0
    )

    check(reader.isEof()) {
        "EOF not reached"
    }

    return data
}

fun WorkSaveData.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}