package dev.dexsr.gmod.palworld.trainer.ue.gvas

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class GvasFile(
    val header: GvasFileHeader,
    val properties: GvasFileProperties,
    val trailer: ByteArray
) {

    companion object
}

@OptIn(ExperimentalEncodingApi::class)
fun GvasFile.trailerToJsonValue() = Base64.encode(trailer)