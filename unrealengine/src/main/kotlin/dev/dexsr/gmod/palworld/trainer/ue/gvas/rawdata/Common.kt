package dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasReader

class PalItemAndNumRead(
    val itemId: PalItemId,
    val num: Int
) {

    companion object
}

fun PalItemAndNumRead.Companion.fromBytes(reader: GvasReader): PalItemAndNumRead {

    return PalItemAndNumRead(
        itemId = PalItemId(
            staticId = reader.fstring(),
            dynamicId = PalItemDynamicId(
                createdWorldId = reader.uuid().toString(),
                localIdInCreatedWorld = reader.uuid().toString()
            )
        ),
        num = reader.readInt()
    )
}

class PalItemId(
    val staticId: String,
    val dynamicId: PalItemDynamicId
)

class PalItemDynamicId(
    val createdWorldId: String,
    val localIdInCreatedWorld: String
)