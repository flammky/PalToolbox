package dev.dexsr.gmod.palworld.trainer.gvas

import java.nio.ByteBuffer

class GvasFileParseResult(
    val header: GvasHeaderParseResult,
    val data: GvasFile? = null
) {

    fun headerParseFailure() {}
    fun propertiesParseFailure() {}
}

// TODO: complete it
fun ParseGvasFile(
    data: ByteArray
): GvasFileParseResult {
    val buf = ByteBuffer.wrap(data)
    val header = ParseGvasHeader(buf)
    val file = header.valueOrNull
        ?.let { headerValue ->
            val parseProperties = ParseGvasProperties(buf)
            val properties = parseProperties.getOrNull()
                ?: return GvasFileParseResult(header, null)
                    .apply {
                        propertiesParseFailure()
                    }
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
            )
        }
        ?: return GvasFileParseResult(header)
            .apply {
                headerParseFailure()
            }
    return GvasFileParseResult(
        header = header,
        data = file
    )
}