package dev.dexsr.gmod.palworld.toolbox.savegame

import dev.dexsr.gmod.palworld.toolbox.core.Core
import dev.dexsr.gmod.palworld.toolbox.core.MainDispatcher
import dev.dexsr.gmod.palworld.toolbox.savegame.inventory.PlayerInventoryEntry
import dev.dexsr.gmod.palworld.trainer.java.jFile
import dev.dexsr.gmod.palworld.trainer.ue.gvas.GvasFileProperties
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SaveGameWorldEdit {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Core.MainDispatcher)
    private var _jFile: jFile? = null
    private val mtx = Mutex()
    private val parser = SaveGameWorldFileParser(coroutineScope)
    private val listeners = mutableListOf<SaveGameWorldEditListener>()
    private var decompressed: ByteArray? = null
    private var pos: Long = -1
    private var _properties: GvasFileProperties? = null

    private val inventories = mutableMapOf<String, SaveGamePlayerInventoryEdit>()

    var players: List<SaveGamePlayersParsedData.Player>? = null

    // use atomic-fu plugin ?
    private var openable = true

    suspend fun open(jFile: jFile) {
        mtx.withLock {
            require(openable)
            openable = false
            _jFile = jFile
        }
    }

    fun addListener(listener: SaveGameWorldEditListener) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    suspend fun headerCheck() {
        coroutineScope.launch {
            val decompress =  parser.decompressFileAsync(requireNotNull(_jFile)).run {
                listeners.fastForEach { it.onDecompressing() }
                await().also {
                    it.err?.let { error(it) }
                    listeners.fastForEach { it.onDecompressed() }
                }
            }

            decompressed = decompress.data

            val header = parser.parseFileHeaderAsync(checkNotNull(decompressed), true).run {
                listeners.fastForEach { it.onCheckingHeader() }
                await().also {
                    it.err?.let { error(it) }
                    this@SaveGameWorldEdit.pos = it.data!!.pos
                    listeners.fastForEach { it.onCheckedHeader() }
                }
            }

        }.join()
    }

    suspend fun parsePlayers() {
        coroutineScope.launch {
            val parse = parser.parsePlayersAsync(input = requireNotNull(decompressed), offset = pos).run {
                await().also {
                    it.err?.let { error(it) }
                    this@SaveGameWorldEdit.players = it.data!!.players
                    this@SaveGameWorldEdit._properties = it.data.properties
                }
            }
        }.join()
    }

    suspend fun getOrOpenPlayerInventoryAsync(uid: String) = coroutineScope.async {
        inventories.getOrPut(uid) {
            SaveGamePlayerInventoryEdit(this@SaveGameWorldEdit)
                .apply { openWithParentSource(requireSaveGameSource(), uid) }
        }
    }

    internal fun parsePlayerInventoryDataAsync(
        inventories: List<String>
    ) = coroutineScope.async {
        val data = doGetOrParseInventoriesData()

        mutableMapOf<String, List<PlayerInventoryEntry>>()
            .apply {
                inventories.forEach { uid ->
                    data.inventories[uid]?.let { put(uid, it) }
                }
            }
    }

    private var getOrParseInventoriesData: Deferred<SaveGameParsePlayersInventoriesData>? = null
    private suspend fun doGetOrParseInventoriesData(): SaveGameParsePlayersInventoriesData {
        getOrParseInventoriesData?.let {
            if (it.isActive) {
                return it.await()
            } else if (it.isCompleted) {
                return it.getCompleted()
            }
        }
        val task = coroutineScope.async {
            val result = parser.parsePlayersInventoryAsync(requireNotNull(_properties)).await()
            if (result.err != null) {
                throw CancellationException(result.err)
            }
            checkNotNull(result.data)
        }
        getOrParseInventoriesData = task
        return task.await()
    }

    private fun requireSaveGameSource(): jFile = requireNotNull(_jFile) {
        "SaveGameEdit wasn't opened with jFile as source"
    }
}

interface SaveGameWorldEditListener {

    fun onDecompressing() {}
    fun onDecompressed() {}
    fun onCheckingHeader() {}

    fun onCheckedHeader() {}
}

fun SaveGameWorldEditListener(
    onDecompressing: () -> Unit = {},
    onDecompressed: () -> Unit = {},
    onCheckingHeader: () -> Unit = {},
    onCheckedHeader: () -> Unit = {}
) = object : SaveGameWorldEditListener {

    override fun onDecompressing() = onDecompressing()
    override fun onDecompressed() = onDecompressed()
    override fun onCheckingHeader() = onCheckingHeader()
    override fun onCheckedHeader() {
        onCheckedHeader()
    }
}