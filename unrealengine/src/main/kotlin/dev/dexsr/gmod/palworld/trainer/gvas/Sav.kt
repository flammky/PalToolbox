package dev.dexsr.gmod.palworld.trainer.gvas

// 0A 09 00 00 D9 06 00 00 [50 6C 5A] 31 78
internal const val PALWD_SAV_MAGICBYTES = "PlZ"

interface SavFileDecoder {

    fun decode(arr: ByteArray): Any
}

interface SavFileEncoder {
}

interface SavFileCodec : SavFileDecoder, SavFileEncoder {
}