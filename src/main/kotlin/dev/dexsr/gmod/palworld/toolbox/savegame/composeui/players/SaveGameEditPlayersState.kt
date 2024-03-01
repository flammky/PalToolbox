package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.players

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.toolbox.base.strictResultingLoop
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.util.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    var pagingData by mutableStateOf<PlayersPagingData?>(null)
        private set


    fun onRemembered() {
        init()
    }

    fun dispose() {}

    private fun init() {
        coroutineScope.launch(MainUIDispatcher) {
            pagingData = PlayersPagingData(
                playersCount = 20,
                buckets = listOf(
                    PageBucket(
                        pages = listOf(PlayersPagedData(List(20) { it.toString().padStart(20, '0') })),
                        pageFirstIndex = 0,
                        pageSize = 20
                    )
                ),
                intentLoadToOffset = {}
            )
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
    //  bucket2 = [[31, ..35]
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