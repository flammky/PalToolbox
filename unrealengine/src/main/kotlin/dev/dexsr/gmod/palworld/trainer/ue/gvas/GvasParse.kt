package dev.dexsr.gmod.palworld.trainer.ue.gvas

import java.nio.ByteBuffer

// TODO

class GvasFileParseResult(
    val header: GvasHeaderParseResult?,
    val properties: GvasPropertiesParseResult?,
    val data: GvasFile? = null
) {


    fun headerParseFailure() {}
    fun propertiesParseFailure() {}
}

// TODO: complete it
// TODO: make this suspend
suspend fun ParseGvasFile(
    data: ByteArray
): GvasFileParseResult {
    val buf = ByteBuffer.wrap(data)
    val header = ParseGvasHeader(buf)
    val (file, properties) = header.valueOrNull
        ?.let { headerValue ->
            val parseProperties = ParseGvasProperties(buf)
            val properties = parseProperties.valueOrNull
                ?: return GvasFileParseResult(header, parseProperties, null)
            val trailer = run {
                val dst = ByteArray(buf.remaining())
                buf.get(dst)
                dst
            }
            if (!trailer.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x00))) {
                println("${trailer.size} bytes of trailer data, file may not have fully parsed")
            }
            GvasFile(
                headerValue,
                properties,
                trailer
            ) to parseProperties
        }
        ?: return GvasFileParseResult(header, null)
            .apply {
                headerParseFailure()
            }
    return GvasFileParseResult(
        header = header,
        properties = properties,
        data = file
    )
}