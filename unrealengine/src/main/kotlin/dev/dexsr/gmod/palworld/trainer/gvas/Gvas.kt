package dev.dexsr.gmod.palworld.trainer.gvas

class GvasFile(
    val header: GvasHeader,
    val properties: GvasMap<String, Any>,
    val trailer: ByteArray
) {

    companion object
}