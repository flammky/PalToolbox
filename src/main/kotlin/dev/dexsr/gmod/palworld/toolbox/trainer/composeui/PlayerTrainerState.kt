package dev.dexsr.gmod.palworld.toolbox.trainer.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.trainer.game.*
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher
import kotlin.coroutines.resume

@Composable
fun rememberPlayerTrainerState(): PlayerTrainerState {
    val state = remember { PlayerTrainerState() }

    DisposableEffect(Unit) {
        state.init()
        onDispose { state.dispose() }
    }

    return state
}

class PlayerTrainerState() {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope get() = requireNotNull(_coroutineScope) {
        "coroutineScope wasn't initialized"
    }

    // statically type them for now

    private var infiniteHPWorker: PlayerInfiniteHpTrainerWorker? = null
    private var infiniteStaminaWorker: PlayerInfiniteStaminaTrainerWorker? = null

    var infiniteHPEnabled by mutableStateOf(false)
        private set

    var infiniteHPLoading by mutableStateOf(false)
        private set

    var infiniteStaminaEnabled by mutableStateOf(false)
        private set

    var infiniteStaminaLoading by mutableStateOf(false)
        private set

    fun init() {
        _coroutineScope = CoroutineScope(SupervisorJob() + MainUIDispatcher)
    }

    fun dispose() {
        coroutineScope.cancel()
        infiniteHPWorker?.dispose()
        infiniteStaminaWorker?.dispose()
    }

    fun toggleInfiniteHP(enable: Boolean) {
        if (infiniteHPEnabled == enable) return
        if (enable) {
            infiniteHPWorker = PlayerInfiniteHpTrainerWorker(
                coroutineScope,
                onEnableChanged = { infiniteHPEnabled = it },
                onLoadingChanged = { infiniteHPLoading = it }
            ).apply { enable() }
        } else {
            infiniteHPWorker?.disable()
        }
    }

    fun toggleInfiniteStamina(enable: Boolean) {
        if (infiniteStaminaEnabled == enable) return
        if (enable) {
            infiniteStaminaWorker = PlayerInfiniteStaminaTrainerWorker(
                coroutineScope,
                onEnableChanged = { infiniteStaminaEnabled = it },
                onLoadingChanged = { infiniteStaminaLoading = it }
            ).apply { enable() }
        } else {
            infiniteStaminaWorker?.disable()
        }
    }
}

///
/// write this for now, we implement domain interface later
///

private class PlayerInfiniteHpTrainerWorker(
    private val coroutineScope: CoroutineScope,
    private val onEnableChanged: (Boolean) -> Unit,
    private val onLoadingChanged: (Boolean) -> Unit
) {

    private var task: Job? = null

    // TODO write trainer impl
    fun enable() {
        task?.cancel()
        task = coroutineScope.launch(MainUIDispatcher) {
            val ctx = coroutineContext
            onLoadingChanged(true)
            DefaultPalworldTrainer.instancePeriodicSetHP(9999, 300,
                onSuccessWrite = {
                    ctx.ensureActive()
                    onLoadingChanged(false)
                    onEnableChanged(true)
                }
            )
            onLoadingChanged(false)
        }
    }

    // TODO write trainer impl
    fun disable() {
        task?.cancel()
        onLoadingChanged(false)
        onEnableChanged(false)
    }

    // TODO enqueue disable on dispose
    fun dispose() {
        disable()
        task?.cancel()
    }
}

private class PlayerInfiniteStaminaTrainerWorker(
    private val coroutineScope: CoroutineScope,
    private val onEnableChanged: (Boolean) -> Unit,
    private val onLoadingChanged: (Boolean) -> Unit
) {

    private var task: Job? = null
    private var disable: Job? = null

    // TODO write trainer impl
    fun enable() {
        if (disable?.isActive == true) return
        task?.cancel()
        task = coroutineScope.launch(MainUIDispatcher) {
            val ctx = coroutineContext
            onLoadingChanged(true)
            withContext(Dispatchers.IO) {
                DefaultPalworldTrainer.instancePeriodicSetPlayerStamina(null, 300) {
                    ctx.ensureActive()
                    onLoadingChanged(false)
                    onEnableChanged(true)
                }
            }
            onLoadingChanged(false)
        }
    }

    // TODO write trainer impl
    fun disable() {
        if (disable?.isActive == true) return
        task?.let { task ->
            disable = coroutineScope.launch(MainUIDispatcher) {
                task.cancel()
                suspendCancellableCoroutine<Unit> { cont ->
                    task.invokeOnCompletion { cont.resume(Unit) }
                }
                onLoadingChanged(false)
                onEnableChanged(false)
            }
        }
    }

    // TODO enqueue disable on dispose
    fun dispose() {
        task?.cancel()
        disable?.cancel()
    }
}
