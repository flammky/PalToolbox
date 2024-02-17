package dev.dexsr.gmod.palworld.trainer.gvas

import okio.Buffer

class GvasHeader(
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

    companion object
}
