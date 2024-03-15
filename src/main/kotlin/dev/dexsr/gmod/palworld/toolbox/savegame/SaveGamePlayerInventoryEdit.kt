package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.core.Core
import dev.dexsr.gmod.palworld.toolbox.core.MainDispatcher
import dev.dexsr.gmod.palworld.toolbox.savegame.inventory.PlayerInventoryEntry
import dev.dexsr.gmod.palworld.toolbox.savegame.player.PlayerFileHeaderParsedData
import dev.dexsr.gmod.palworld.toolbox.savegame.player.SaveGamePlayerFileParser
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFileProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SaveGamePlayerInventoryEdit(
    // TODO internal
    private val worldEdit: SaveGameWorldEdit
) {

    private var _jFile: jFile? = null
    private var _parentJFile: jFile? = null
    private val mtx = Mutex()
    private var openable = true
    private val openJob = Job()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Core.MainDispatcher)

    // event sink ?
    private val listeners = mutableListOf<SaveGamePlayerInventoryEditListener>()

    private var _uid: String? = null
    private var _strippedUid: String? = null

    private var resolvedFile = false
    private var _decompressedSource: ByteArray? = null
    private var _header: PlayerFileHeaderParsedData? = null
    private var _inventoriesUidMap: LinkedHashMap<String, String>? = null
    private var _lazyProperties: GvasFileProperties? = null
    private val _inventoriesEntry = mutableMapOf<String, List<PlayerInventoryEntry>>()

    private val worldFile = SaveGameWorldFileParser(coroutineScope)
    private val playerFileParser = SaveGamePlayerFileParser(coroutineScope)

    val inventoriesUidMap
        get() = _inventoriesUidMap

    suspend fun openWithParentSource(jFile: jFile, uid: String) {
        mtx.withLock {
            require(openable) {
                "SaveGameInventoryEdit was already opened"
            }
            openable = false
            _parentJFile = jFile
            _uid = uid
            _strippedUid = when(uid.length) {
                32 -> {
                    check(uid.all { it.isLetterOrDigit() })
                    uid
                }
                36 -> {
                    val removeDashes = buildString { uid.forEach { c -> if (c != '-') append(c) } }
                    check(removeDashes.length == 32)
                    check(removeDashes.all { it.isLetterOrDigit() })
                    removeDashes
                }
                else -> error("Invalid UID length=${uid.length}")
            }
            openJob.complete()
        }
    }

    fun addListener(listener: SaveGamePlayerInventoryEditListener) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    suspend fun prepare() {
        coroutineScope.launch {
            resolvedFile = false
            resolveFile()
            if (!resolvedFile) return@launch
        }.join()
    }

    suspend fun parseInventoryUIDMap() {
        coroutineScope.launch {
            if (!resolvedFile) return@launch
            _inventoriesUidMap ?: run {
                val decompressedSource = _decompressedSource ?: run {
                    decompressSource()
                    _decompressedSource ?: return@launch
                }
                val header = _header ?: run {
                    doParseHeader(decompressedSource)
                    _header ?: return@launch
                }
                doParseInventoryUIDMap(decompressedSource, header.pos.toInt())
                _inventoriesUidMap.also { println(it) } ?: return@launch
            }
        }.join()
    }

    fun getOrParseInventoryEntryAsync(name: String) = coroutineScope.async {
        val map = _inventoriesUidMap ?: run {
            parseInventoryUIDMap()
            _inventoriesUidMap ?: return@async null
        }
        val uid = map[name]
        _inventoriesEntry[uid] ?: run {
            doParseInventoryEntry(name)
            _inventoriesEntry[uid]
        }
    }

    private suspend fun doParseInventoryEntry(name: String) {
        if (!resolvedFile) return
        val uidMap = _inventoriesUidMap ?: run {
            parseInventoryUIDMap()
            _inventoriesUidMap ?: return
        }
        // TODO: Error.ContainerIdInfoNotPresent
        val common = uidMap[name] ?: return
        worldEdit.parsePlayerInventoryDataAsync(listOf(common)).await().also {
            it[common]?.let { entry -> _inventoriesEntry[common] = entry }
        }
    }

    private suspend fun decompressSource() {
        val file = _jFile ?: return
        val decompress = playerFileParser.decompressPlayerFileAsync(file).await()
        if (decompress.err != null) {
            listeners.fastForEach { l -> l.onError(Error.DecompressFailed(decompress.err)) }
            return
        }
        val decompressed = checkNotNull(decompress.data)
        _decompressedSource = decompressed
    }

    private suspend fun doParseHeader(
        decompressed: ByteArray
    ) {
        val parse = playerFileParser.parsePlayerFileHeaderAsync(decompressed, true).await()
        if (parse.err != null) {
            listeners.fastForEach { l -> l.onError(Error.HeaderParseFailed(parse.err)) }
            return
        }
        _header = checkNotNull(parse.data)
    }

    private suspend fun doParseInventoryUIDMap(
        decompressed: ByteArray,
        offset: Int
    ) {
        val parse = playerFileParser.parsePlayerInventories(decompressed, offset).await()
        if (parse.err != null) {
            listeners.fastForEach { l -> l.onError(Error.InventoryParseFailed(parse.err)) }
            return
        }
        checkNotNull(parse.data)
        _inventoriesUidMap = parse.data.inventories.also {
            _lazyProperties = parse.data.properties
        }
    }

    private fun resolveFile() {
        val uid = requireNotNull(_strippedUid)
        val file = _jFile
            ?: run {
                val folder = jFile(requireNotNull(_parentJFile).folderOrParent(), "Players")
                val folderExist = folder.exists()
                if (!folderExist) {
                    listeners.fastForEach { it.onError(Error.PlayersFolderFNF(folder)) }
                    return
                }
                jFile(folder, "${uid}.sav")
            }
        val exist = file.exists() && file.name == "${uid}.sav"
        if (!exist) {
            listeners.fastForEach { it.onError(Error.PlayerFileFNF(_uid.toString(), uid, file.folderOrParent(), file)) }
            return
        }
        resolvedFile = true
        _jFile = file
    }

    private fun jFile.folderOrParentOrNull(): jFile? = if (isFile) parentFile.takeIf { it.isDirectory } else this
    private fun jFile.folderOrParent(): jFile = if (isFile) parentFile else this


    // typed error
    sealed class Error {

        data class PlayersFolderFNF(
            val file: jFile
        ) : Error()

        data class PlayerFileFNF(
            val uid: String,
            val strippedUid: String,
            val parentFolder: jFile,
            val file: jFile
        ) : Error()

        data class DecompressFailed(
            val msg: String
        ) : Error()

        data class HeaderParseFailed(
            val msg: String
        ) : Error()

        data class InventoryParseFailed(
            val msg: String
        ) : Error()
    }

    sealed class Progress {

        data object ResolvingFile : Progress()
        data object DecompressingFile : Progress()
        data object ParsingHeader : Progress()
    }
}


interface SaveGamePlayerInventoryEditListener {

    fun onError(typedError: SaveGamePlayerInventoryEdit.Error) {}
    fun onProgress(typedProgress: SaveGamePlayerInventoryEdit.Progress) {}
}

fun SaveGamePlayerInventoryEditListener(
    onError: (SaveGamePlayerInventoryEdit.Error) -> Unit,
    onProgress: (SaveGamePlayerInventoryEdit.Progress) -> Unit
) = object : SaveGamePlayerInventoryEditListener {

    override fun onError(typedError: SaveGamePlayerInventoryEdit.Error) {
        onError(typedError)
    }

    override fun onProgress(typedProgress: SaveGamePlayerInventoryEdit.Progress) {
        onProgress(typedProgress)
    }
}