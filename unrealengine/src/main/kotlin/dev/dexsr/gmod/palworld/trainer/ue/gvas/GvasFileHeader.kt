package dev.dexsr.gmod.palworld.trainer.ue.gvas

import okio.Buffer

class GvasFileHeader(
    val magicBytes: ByteArray,
    val saveGameVersion: Int,
    val packageFileVersionUE4: Int,
    val packageFileVersionUE5: Int,
    val engineVersionMajor: UShort,
    val engineVersionMinor: UShort,
    val engineVersionPatch: UShort,
    val engineVersionChangelist: Int,
    val engineVersionBranch: String,
    val customVersionFormat: Int,
    // todo(l): check if we can use HashMap
    val customVersions: List<Pair<String, Int>>,
    val saveGameClassName: String
) {

    companion object {

        val MAGIC_BYTES_NAME = "magic"
        val SAVE_GAME_VERSION_NAME = "saveGameVersion"
        val PACKAGE_FILE_VERSION_UE4_NAME = "packageFileVersionUe4"
        val PACKAGE_FILE_VERSION_UE5_NAME = "packageFileVersionUe5"
        val ENGINE_VERSION_MAJOR_NAME = "engineVersionMajor"
        val ENGINE_VERSION_MINOR_NAME = "engineVersionMinor"
        val ENGINE_VERSION_PATCH_NAME = "engineVersionPatch"
        val ENGINE_VERSION_CHANGELIST_NAME = "engineVersionChangelist"
        val ENGINE_VERSION_BRANCH_NAME = "engineVersionBranch"
        val CUSTOM_VERSION_FORMAT_NAME = "customVersionFormat"
        val CUSTOM_VERSIONS_NAME = "customVersions"
        val SAVE_GAME_CLASSNAME_NAME = "saveGameClassName"
    }
}

// should we show Base64 ?
fun GvasFileHeader.magicBytesToJsonValue() = Buffer().write(magicBytes).readIntLe()