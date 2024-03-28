package dev.dexsr.gmod.palworld.toolbox.savegame.gvas

import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFileHeader

fun WriteGvasHeader(
    writer: GvasWriter,
    header: GvasFileHeader
) {
    writer.writeBytes(header.magicBytes)
    writer.writeInt(header.saveGameVersion)
    writer.writeInt(header.packageFileVersionUE4)
    writer.writeInt(header.packageFileVersionUE5)
    writer.writeUShort(header.engineVersionMajor)
    writer.writeUShort(header.engineVersionMinor)
    writer.writeUShort(header.engineVersionPatch)
    writer.writeUInt(header.engineVersionChangelist)
    writer.writeStr(header.engineVersionBranch)
    writer.writeInt(header.customVersionFormat)
    writer.writeList(header.customVersions, ::writeCustomVersion)
    writer.writeStr(header.saveGameClassName)
}

private fun writeCustomVersion(writer: GvasWriter, value: Pair<String, Int>) {
    writer.writeUId(value.first)
    writer.writeInt(value.second)
}