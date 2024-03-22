package dev.dexsr.gmod.palworld.toolbox.savegame.player

class PlayerFileSaveData(
    val uid: String,
    val individualId: IndividualId,
    val inventoryInfo: InventoryInfo,
    val technologyPoint: Int,
    val unlockedTechnologyRecipes: List<String>,
    val palStorageContainerId: String
) {


    class InventoryInfo(
        val entries: LinkedHashMap<String, String>
    )

    class RecordData(
        val tribeCaptureCount: Int,
        val palCaptureCount: Map<String, Int>,
        val palDeckUnlock: Map<String, Boolean>,
        val fastTravelUnlock: Map<String, Boolean>
    )

    class IndividualId(
        val playerUid: String,
        val instanceUid: String
    )
}