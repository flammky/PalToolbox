package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.util.cast
import dev.dexsr.gmod.palworld.trainer.ue.util.fastForEach
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BaseCampModule

sealed class BaseCampModuleDict() : OpenGvasDict()

private val PASSIVE_EFFECT = mapOf(
    0 to "EPalBaseCampPassiveEffectType::None",
    1 to "EPalBaseCampPassiveEffectType::WorkSuitability",
    2 to "EPalBaseCampPassiveEffectType::WorkHard",
)

class BaseCampModuleData(
    val transportItemCharacterInfos: ArrayList<BaseCampModuleTransportItemCharacterInfo>? = null,
    val passiveEffects: ArrayList<BaseCampModulePassiveEffect>? = null,
    val values: ByteArray? = null
) : BaseCampModuleDict()

class BaseCampModuleTransportItemCharacterInfo(
    val itemInfos: ArrayList<PalItemAndNumRead>,
    val characterLocation: GvasVector
) : BaseCampModuleDict()

class BaseCampModulePassiveEffect(
    val type: Int,
    val workHardType: Byte? = null,
    val unknownTrailer: ByteArray? = null
) : BaseCampModuleDict()

private fun BaseCampModulePassiveEffect(reader: GvasReader): BaseCampModulePassiveEffect {
    val type = reader.readByte().toInt()
    check(type in PASSIVE_EFFECT) {
        "Unknown passive effect type=$type"
    }
    if (type == 2) {
        return BaseCampModulePassiveEffect(
            type = 2,
            workHardType = reader.readByte(),
            unknownTrailer = reader.readBytes(4)
        )
    }
    return BaseCampModulePassiveEffect(
        type = type
    )
}

private val NOOP_TYPES = arrayOf(
    "EPalBaseCampModuleType::Energy",
    "EPalBaseCampModuleType::Medical",
    "EPalBaseCampModuleType::ResourceCollector",
    "EPalBaseCampModuleType::ItemStorages",
    "EPalBaseCampModuleType::FacilityReservation",
    "EPalBaseCampModuleType::ObjectMaintenance",
)

fun BaseCampModule.decode(
    reader: GvasReader,
    typeName: String,
    size: Int,
    path: String
): GvasProperty {
    require(typeName == "MapProperty") {
        "Expected MapProperty, got=$typeName"
    }

    val value = reader.property(typeName, size, path, nestedCallerPath = path)

    val moduleMap = value.value
        .cast<GvasMapDict>()

    moduleMap.value.fastForEach { map ->
        val moduleType = map["key"]
            .cast<String>()
        val arrayDict = map["value"].cast<GvasMapStruct>().v["RawData"]
            .cast<GvasProperty>().value
            .cast<GvasArrayDict>()
        val moduleBytes =
            arrayDict.value.cast<GvasAnyArrayPropertyValue>().values
            .cast<GvasByteArrayValue>().value
        map["value"].cast<GvasMapStruct>().v["RawData"]
            .cast<GvasProperty>().value = GvasArrayDict(
            arrayType = arrayDict.arrayType,
            id = arrayDict.id,
            value = GvasTransformedArrayValue(decodeBytes(reader, moduleBytes, moduleType))
        )
    }

    value.value = CustomByteArrayRawData(
        path,
        moduleMap.id,
        value.value
    )

    return value
}

private fun BaseCampModule.decodeBytes(
    parentReader: GvasReader,
    dataBytes: ByteArray,
    moduleType: String
): BaseCampModuleData {

    val reader = parentReader.copy(ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = when(moduleType) {
        in NOOP_TYPES -> BaseCampModuleData()
        "EPalBaseCampModuleType::TransportItemDirector" -> {
            try {
                BaseCampModuleData(
                    transportItemCharacterInfos = reader.readArray {
                        BaseCampModuleTransportItemCharacterInfo(
                            itemInfos = reader.readArray { PalItemAndNumRead.fromBytes(reader) },
                            characterLocation = reader.readVector()
                        )
                    }
                )
            } catch (e: Exception) {
                return BaseCampModuleData(values = dataBytes)
            }
        }
        "EPalBaseCampModuleType::PassiveEffect" -> {
            try {
                BaseCampModuleData(passiveEffects = reader.readArray(::BaseCampModulePassiveEffect))
            } catch (e: Exception) {
                return BaseCampModuleData(values = dataBytes)
            }
        }
        else -> BaseCampModuleData(values = dataBytes)
    }

    return data
}

fun BaseCampModule.encode(
    writer: GvasWriter,
    typeName: String,
    data: GvasProperty
): Int {
    TODO()
}