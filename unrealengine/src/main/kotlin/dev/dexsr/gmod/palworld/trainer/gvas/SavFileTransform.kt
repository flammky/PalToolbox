package dev.dexsr.gmod.palworld.trainer.gvas

import java.io.InputStream

class SavFileTransform private constructor(
    val inputStream: InputStream
) {

    var isFileEmpty = false
        private set

    var isFileTooSmall = false
        private set

    var invalidFile = false
        private set

    var invalidFileMsgKind: String? = null
        private set

    var invalidFileMsg: String? = null
        private set

    var unhandled: Boolean = false
        private set

    var contentMagicBytes: ByteArray? = null
        private set

    var contentDecompressedData: ByteArray? = null
        private set

    var contentCompressedData: ByteArray? = null
        private set

    var compressionType: ByteArray? = null
        private set

    private var _codec: SavFileCodec? = null
        private set

    val codec: SavFileCodec
        get() = requireNotNull(_codec)

    fun markFileEmpty() {
        this.isFileEmpty = true
    }

    fun markFileTooSmall() {
        this.isFileTooSmall = true
    }

    fun markInvalidFile(
        kind: String,
        msg: String = ""
    ) {
        this.invalidFile = true
        this.invalidFileMsgKind = kind
        this.invalidFileMsg = msg
    }

    fun markUnhandled() {
        this.unhandled = true
    }

    fun setDecompressedData(
        data: ByteArray,
        type: ByteArray
    ) {
        this.contentDecompressedData = data
        this.compressionType = type
    }

    fun setCompressedData(
        data: ByteArray,
        type: ByteArray
    ) {

    }

    fun setMagicBytes(
        bytes: ByteArray
    ) {
        this.contentMagicBytes = bytes
    }

    private fun byCodec(codec: SavFileCodec) {
        this._codec = codec
    }

    companion object {

        fun open(
            inputStream: InputStream,
            codec: SavFileCodec,
        ): SavFileTransform {
            return SavFileTransform(inputStream).apply { byCodec(codec) }
        }
    }
}

val SavFileTransform.MSG_INVALID_COMPRESSION_INFO get() = "invalid compression info"
val SavFileTransform.MSG_WRONG_COMPRESSION_INFO get() = "wrong compression info"
val SavFileTransform.MSG_UNKNOWN_COMPRESSION_INFO get() = "unknown compression info"
val SavFileTransform.MSG_COMPRESSION_INFO_EMPTY get() = "compression info empty"
val SavFileTransform.MSG_WRONG_MAGIC_BYTES get() = "wrong magic bytes"

val SavFileTransform.KNOWN_COMPRESSION_TYPES get() = byteArrayOf(0x30, 0x31, 0x32)