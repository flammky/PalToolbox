package dev.dexsr.gmod.palworld.trainer.gvas

interface JsonSavFileCodec : SavFileCodec {

    override fun decode(arr: ByteArray): Any
}