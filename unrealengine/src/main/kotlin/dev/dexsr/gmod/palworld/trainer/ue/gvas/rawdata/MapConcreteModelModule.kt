package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader
import dev.dexsr.gmod.palworld.trainer.ue.gvas.OpenGvasDict
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
# EPalMapObjectConcreteModelModuleType::None = 0,
# EPalMapObjectConcreteModelModuleType::ItemContainer = 1,
# EPalMapObjectConcreteModelModuleType::CharacterContainer = 2,
# EPalMapObjectConcreteModelModuleType::Workee = 3,
# EPalMapObjectConcreteModelModuleType::Energy = 4,
# EPalMapObjectConcreteModelModuleType::StatusObserver = 5,
# EPalMapObjectConcreteModelModuleType::ItemStack = 6,
# EPalMapObjectConcreteModelModuleType::Switch = 7,
# EPalMapObjectConcreteModelModuleType::PlayerRecord = 8,
# EPalMapObjectConcreteModelModuleType::BaseCampPassiveEffect = 9,
# EPalMapObjectConcreteModelModuleType::PasswordLock = 10,
 */

object MapConcreteModelModule

sealed class MapConcreteModelModuleDict : OpenGvasDict()

class MapConcreteModelModuleRawData(
    val values: ByteArray
) : MapConcreteModelModuleDict()

class MapConcreteModelModuleData(
    val item: MapConcreteModelModuleItemDict?
) : MapConcreteModelModuleDict()

sealed class MapConcreteModelModuleItemDict() : MapConcreteModelModuleDict()

class MapConcreteModelModuleItemContainer(
    val targetContainerId: String,
    val slotAttributeIndexes: ArrayList<ModuleSlotIndexes>,
    val allSlotAttribute: ArrayList<Byte>,
    val dropItemAtDisposed: Boolean,
    val usageType: Byte
) : MapConcreteModelModuleItemDict()

class MapConcreteModuleCharacterContainer(
    val targetContainerId: String
) : MapConcreteModelModuleItemDict()

class MapConcreteModuleWorkee(
    val targetWorkId: String
) : MapConcreteModelModuleItemDict()

class MapConcreteModuleSwitch(
    val switchState: Byte
) : MapConcreteModelModuleItemDict()

class MapConcreteModulePasswordLock(
    val lockState: Byte,
    val password: String,
    val playerInfos: ArrayList<PlayerLockInfo>
) : MapConcreteModelModuleItemDict()

class ModuleSlotIndexes(
    val attribute: Byte,
    val indexes: ArrayList<Int>
) : MapConcreteModelModuleItemDict()

class PlayerLockInfo(
    val playerUid: String,
    val tryFailedCount: Int,
    val trySuccessCache: Boolean
) : MapConcreteModelModuleItemDict()

fun MapConcreteModelModule.decodeBytes(
    parentReader: GvasReader,
    bytes: ByteArray,
    moduleType: String
) : MapConcreteModelModuleDict {
    if (bytes.isEmpty()) return MapConcreteModelModuleRawData(bytes)

    val reader = parentReader.copy(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN))

    val data = MapConcreteModelModuleData(
        item = when(moduleType) {
            "EPalMapObjectConcreteModelModuleType::ItemContainer" -> MapConcreteModelModuleItemContainer(
                targetContainerId = reader.uuid().toString(),
                slotAttributeIndexes = reader.readArray {
                    ModuleSlotIndexes(
                        attribute = reader.readByte(),
                        indexes = reader.readArray { it.readInt() }
                    )
                },
                allSlotAttribute = reader.readArray { it.readByte() },
                dropItemAtDisposed = reader.readInt() > 0,
                usageType = reader.readByte()
            )
            "EPalMapObjectConcreteModelModuleType::CharacterContainer" -> MapConcreteModuleCharacterContainer(
                targetContainerId = reader.uuid().toString()
            )
            "EPalMapObjectConcreteModelModuleType::Workee" -> MapConcreteModuleWorkee(
                targetWorkId = reader.uuid().toString()
            )
            "EPalMapObjectConcreteModelModuleType::Energy",
            "EPalMapObjectConcreteModelModuleType::StatusObserver",
            "EPalMapObjectConcreteModelModuleType::ItemStack" -> null
            "EPalMapObjectConcreteModelModuleType::Switch" -> MapConcreteModuleSwitch(
                switchState = reader.readByte()
            )
            "EPalMapObjectConcreteModelModuleType::PlayerRecord",
            "EPalMapObjectConcreteModelModuleType::BaseCampPassiveEffect" -> null
            "EPalMapObjectConcreteModelModuleType::PasswordLock" -> MapConcreteModulePasswordLock(
                lockState = reader.readByte(),
                password = reader.fstring(),
                playerInfos = reader.readArray { PlayerLockInfo(
                    playerUid = reader.uuid().toString(),
                    tryFailedCount = reader.readInt(),
                    trySuccessCache = reader.readInt() > 0
                ) }
            )
            else -> null
        }
    )

    check(reader.isEof()) {
        "EOF not reached for module type: $moduleType"
    }

    return data
}