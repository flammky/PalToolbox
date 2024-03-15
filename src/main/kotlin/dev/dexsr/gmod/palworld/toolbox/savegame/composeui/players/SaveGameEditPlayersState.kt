package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.base.breakLoop
import dev.dexsr.gmod.palworld.toolbox.base.continueLoop
import dev.dexsr.gmod.palworld.toolbox.base.looper
import dev.dexsr.gmod.palworld.toolbox.base.strictResultingLoop
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGameWorldFileParser
import dev.dexsr.gmod.palworld.toolbox.savegame.SaveGamePlayersParsedData
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberSaveGameEditPlayersState(
    editState: SaveGameEditState
): SaveGameEditPlayersState {
    val coroutineScope = rememberCoroutineScope()
    val state =  remember(editState) { SaveGameEditPlayersState(editState, coroutineScope) }

    DisposableEffect(state) {
        state.onRemembered()
        onDispose { state.dispose() }
    }

    return state
}

@Stable
class SaveGameEditPlayersState(
    private val editState: SaveGameEditState,
    private val coroutineScope: CoroutineScope
) {

    private var _workers = mutableMapOf<Int, Job>()
    private var _buckets = mutableListOf<PageBucket>()
    private val parser = SaveGameWorldFileParser(coroutineScope)

    var pagingData by mutableStateOf<PlayersPagingData?>(null)
        private set

    private var _data by mutableStateOf<SaveGamePlayersParsedData?>(null)
    private var players by mutableStateOf<List<SaveGamePlayersParsedData.Player>>(emptyList(), neverEqualPolicy())


    fun onRemembered() {
        init()
    }

    fun dispose() {

    }

    fun findPlayer(uid: String) = players.find { it.attribute.uid == uid }

    private fun init() {
        coroutineScope.launch(MainUIDispatcher) {
            val data = strictResultingLoop<PlayersPagingData> {

                runCatching {

                    withContext(Dispatchers.Default) {

                        runCatching {
                            val edit = editState.saveGameEditor!!
                            val worldEdit = edit.getOrOpenWorldEditAsync().await()
                                .apply { parsePlayers() }
                            checkNotNull(worldEdit.players)
                        }.fold(
                            onSuccess =  { players ->
                                PlayersPagingData(
                                    playersCount = players.size,
                                    buckets = listOf(PageBucket(listOf(PlayersPagedData(players.map { it.attribute.uid })), 0, players.size)),
                                    // no impl
                                    intentLoadToOffset = {}
                                ).also {
                                    this@SaveGameEditPlayersState.players = players
                                }
                            },
                            onFailure = {
                                looper continueLoop delay(Long.MAX_VALUE)
                            }
                        )
                    }

                }.fold(
                    onSuccess = { looper breakLoop it },
                    // ask user to refresh
                    onFailure = { looper continueLoop delay(Long.MAX_VALUE) }
                )
            }

            pagingData = data
        }
    }

    @Immutable
    class PlayersPagingData(
        val playersCount: Int,
        val buckets: List<PageBucket>,
        val intentLoadToOffset: (Int) -> Unit
    )

    class PlayersPagedData(
        val uuids: List<String>
    )

    class PlayerData(

    )

    class MutPlayerData(

    )

    //
    // represent a continuous list of Paging page,
    // example: page size is 10, elements in 21..30 is not requested but 35 does
    //  bucket1 = [[1, ..10], [11, ..20]]
    //  bucket2 = [[31, ..40]
    //
    @Immutable
    class PageBucket(
        private val pages: List<PlayersPagedData>,
        val pageFirstIndex: Int,
        val pageSize: Int
    ) {

        @Volatile
        var _pageTotal: Int? = null

        val pageTotal
            get() = _pageTotal
                ?: pages.sumOf { it.uuids.size }
                    .also { _pageTotal = it }

        val pageLastIndex: Int
            get() = pageFirstIndex + pageTotal - 1

        fun getByRawIndexOrNull(index: Int): String? {
            if (index < pageFirstIndex || index > pageLastIndex) return null
            val relativeIndex = index - pageFirstIndex
            val segment = relativeIndex / pageSize
            return pages[segment].uuids[relativeIndex - pageSize * segment]
        }

        fun appendToPage(list: MutableList<PlayersPagedData>) = list.addAll(pages)
    }

    companion object {
        const val BATCH_DEFAULT_INTERVAL_SIZE = 20
    }
}

fun SaveGameEditPlayersState.PlayersPagingData.peekContent(index: Int): String? {
    if (index < 0) throw IndexOutOfBoundsException("cannot access index=$index")
    if (buckets.isEmpty() ||
        index < buckets.first().pageFirstIndex ||
        index > buckets.last().pageLastIndex
    ) {
        return null
    }
    // array backed
    buckets.fastForEach { bucket -> bucket.getByRawIndexOrNull(index)?.let { return it } }
    return null
}

fun SaveGameEditPlayersState.PlayersPagingData.getContent(index: Int): String? {
    if (index < 0) throw IndexOutOfBoundsException("cannot access index=$index")
    if (buckets.isEmpty() ||
        index < buckets.first().pageFirstIndex ||
        index > buckets.last().pageLastIndex
    ) {
        intentLoadToOffset(index)
        return null
    }
    // array backed
    buckets.fastForEach { bucket -> bucket.getByRawIndexOrNull(index)?.let { return it } }
    intentLoadToOffset(index)
    return null
}