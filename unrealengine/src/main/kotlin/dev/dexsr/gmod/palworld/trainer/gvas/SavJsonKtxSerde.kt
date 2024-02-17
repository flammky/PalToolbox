package dev.dexsr.gmod.palworld.trainer.gvas

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class KtxJsonSavCodec : JsonSavFileCodec {

    override fun decode(arr: ByteArray): JsonElement {
        return Json.parseToJsonElement(String(arr))
    }
}