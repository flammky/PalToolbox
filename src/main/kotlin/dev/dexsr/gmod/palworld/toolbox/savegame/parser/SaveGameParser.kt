package dev.dexsr.gmod.palworld.toolbox.savegame.parser

import dev.dexsr.gmod.palworld.toolbox.util.cast
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.*
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.ByteArrayRawData
import dev.dexsr.gmod.palworld.trainer.ue.gvas.rawdata.GvasCharacterData
import kotlinx.coroutines.*
import java.nio.ByteBuffer

class SaveGameParser(
    private val coroutineScope: CoroutineScope
) {

    fun decompressFile(file: jFile): SaveGameDecompressHandle {
        return SaveGameDecompressInstance(file, coroutineScope).apply { doCompress() }.handle
    }

    fun parseFileHeader(input: ByteArray, decompressed: Boolean): SaveGameHeaderParseHandle {
        return SaveGameHeaderParseInstance(input, coroutineScope, decompressed).apply { doParse() }.handle
    }

    fun parseFile(file: jFile): SaveGameParseHandle {
        return SaveGameParseInstance(file, coroutineScope).apply { doParse() }.handle
    }

}

interface SaveGameDecompressHandle {

    fun cancel()

    suspend fun await(): SaveGameDecompressResult
}

interface SaveGameHeaderParseHandle {

    fun cancel()

    suspend fun await(): SaveGameHeaderParseResult
}

interface SaveGameParseHandle {

    fun cancel()

    suspend fun await(): SaveGameParseResult
}

private class ActualSaveGameParseHandle() : SaveGameParseHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameParseResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameParseResult {
        return completion.await()
    }

    fun complete(result: SaveGameParseResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class ActualSaveGameDecompressHandle() : SaveGameDecompressHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameDecompressResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameDecompressResult {
        return completion.await()
    }

    fun complete(result: SaveGameDecompressResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameDecompressResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameDecompressResult(err = "Something Unexpected happened, decompressor finished abnormally"))
    }
}

private class ActualSaveGameHeaderParseHandle() : SaveGameHeaderParseHandle {

    val lifetime = SupervisorJob()

    private val completion = CompletableDeferred<SaveGameHeaderParseResult>(lifetime)

    override fun cancel() {
        lifetime.cancel()
    }

    override suspend fun await(): SaveGameHeaderParseResult {
        return completion.await()
    }

    fun complete(result: SaveGameHeaderParseResult) = completion.complete(result)

    fun onCompletion(ex: Exception?) {
        ex?.let {
            completion.complete(
                SaveGameHeaderParseResult(err = "Something Unexpected happened, type: ${ex::class.simpleName}")
            )
        }
        if (!completion.isCompleted)
            completion.complete(SaveGameHeaderParseResult(err = "Something Unexpected happened, parser finished abnormally"))
    }
}

private class SaveGameDecompressInstance(
    private val jFile: jFile,
    private val coroutineScope: CoroutineScope
) {
    val handle = ActualSaveGameDecompressHandle()

    fun doCompress() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                jFile.inputStream().use { ins ->

                    SavFileTransform.open(ins)
                        .apply {
                            decodeZlibCompressed()
                        }
                        .run {
                            contentDecompressedData
                                ?.let(::SaveGameDecompressResult)
                                ?: (userFriendlyErrMessage() ?: "Unable to decompress, no further information provided")
                                    .let(::SaveGameDecompressResult)
                        }
                }
            }.fold(
                onSuccess = { data -> handle.complete(data) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

private class SaveGameHeaderParseInstance private constructor(
    private val jF: jFile?,
    private val byteArray: ByteArray?,
    private val coroutineScope: CoroutineScope,
    private val decompressed: Boolean = false
) {

    constructor(jFile: jFile, coroutineScope: CoroutineScope, decompressed: Boolean = false) : this(
        jFile, null, coroutineScope, decompressed
    )

    constructor(byteArray: ByteArray, coroutineScope: CoroutineScope, decompressed: Boolean) : this(
        null, byteArray, coroutineScope, decompressed
    )


    val handle = ActualSaveGameHeaderParseHandle()

    init {
        require(jF != null || byteArray != null) {
            "both jFile and ByteArray is null"
        }
    }

    fun doParse() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {

                val bytes =
                    if (byteArray != null && decompressed) byteArray
                    else (jF?.let(jFile::inputStream) ?: byteArray!!.let(ByteArray::inputStream)).buffered().use { ins ->

                        if (!decompressed) {
                            val transform = SavFileTransform.open(ins)
                            transform.decodeZlibCompressed()
                            transform.contentDecompressedData
                                ?: return@runCatching SaveGameHeaderParseResult(
                                    err = transform.userFriendlyErrMessage()
                                        ?: "Unable to decompress, no further information provided"
                                )
                        } else ins.readBytes()
                    }

                val parse = ParseGvasHeader(ByteBuffer.wrap(bytes))

                parse.valueOrNull
                    ?.let { data ->
                        SaveGameHeaderParseResult(SaveGameHeaderParsedData(data))
                    }
                    ?: SaveGameHeaderParseResult(err = "Unable to parse header, msg: ${parse.errorMsg}")
            }.fold(
                onSuccess = { v -> handle.complete(v) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )
        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }
}

private class SaveGameParseInstance(
    private val jFile: jFile,
    private val coroutineScope: CoroutineScope
) {

    val handle = ActualSaveGameParseHandle()

    fun doParse() {

        coroutineScope.launch(Dispatchers.IO + handle.lifetime) {

            runCatching {
                jFile.inputStream().use { ins ->

                    val transform = SavFileTransform.open(ins)
                        .apply {
                            decodeZlibCompressed()
                        }

                    transform.contentDecompressedData
                        ?.let { data ->
                            val result  = ParseGvasFile(data)

                            result.header
                                ?.onFailure {
                                    return@use SaveGameParseResult(err = "Unable to parse header, msg: ${it.errorMsg}")
                                }
                                ?: return@use SaveGameParseResult(err = "Unable to start parsing")

                            result.data
                                ?.let { return@use SaveGameParseResult(parseParsedFile(it)) }
                                ?: SaveGameParseResult(err = "Unable to parse properties")
                        }
                        ?: SaveGameParseResult(
                            err = transform.userFriendlyErrMessage() ?: "Unable to decompress, no further information provided"
                        )
                }
            }.fold(
                onSuccess = { v -> handle.complete(v) },
                onFailure = { ex -> handle.onCompletion(ex as Exception) }
            )

        }.invokeOnCompletion { ex ->
            handle.onCompletion(ex as? Exception)
        }
    }

    private fun parseParsedFile(gvasFile: GvasFile): SaveGameParsedData {

        return SaveGameParsedData(
            gvasFile,
            run {
                var name: String?
                gvasFile.properties["worldSaveData"]?.value
                    .cast<GvasStructDict>().value
                    .cast<GvasStructMap>().v["CharacterSaveParameterMap"]?.value
                    .cast<GvasMapDict>().value
                    .first().let {
                        it["value"]
                            .cast<GvasStructMap>()
                            .v["RawData"]?.value
                            .cast<ByteArrayRawData>().value
                            .cast<GvasArrayDict>().value
                            .cast<GvasTransformedArrayValue>().value
                            .cast<GvasCharacterData>().`object`.get("SaveParameter")?.value
                            .cast<GvasStructDict>().value
                            .cast<GvasStructMap>().v.get("NickName")?.value
                            .cast<GvasStrDict>().value.also { name = it }
                    }
                name ?: error("unable to parse name")
            }
        )
    }
}

class SaveGameDecompressResult private constructor(
    val data: ByteArray?,
    val err: String?
) {

    constructor(data: ByteArray) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameParseResult private constructor(
    val data: SaveGameParsedData?,
    val err: String?
) {

    constructor(data: SaveGameParsedData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameHeaderParseResult private constructor(
    val data: SaveGameHeaderParsedData?,
    val err: String?
) {

    constructor(data: SaveGameHeaderParsedData) : this(data, null)

    constructor(err: String) : this(null, err)
}

class SaveGameHeaderParsedData(
    val data: GvasFileHeader
)

class SaveGameParsedData(
    val gvasFile: GvasFile,
    val name: String
)

private fun SavFileTransform.userFriendlyErrMessage(): String? {
    when {
        isFileEmpty -> return "File was empty"
        isFileTooSmall -> return "File was too small"
        unhandledDecompressionType -> return "Save File compression type is not handled"
        invalidFile -> return checkNotNull(invalidFileMsg).ifBlank { "Input File was invalid, no further information provided" }
    }
    return null
}