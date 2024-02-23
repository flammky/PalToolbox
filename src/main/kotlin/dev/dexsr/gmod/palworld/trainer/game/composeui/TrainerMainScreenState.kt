package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.runtime.*
import dev.dexsr.gmod.palworld.trainer.win32.Win32ProcessHandle
import dev.dexsr.gmod.palworld.trainer.win32.WinHelper
import dev.dexsr.gmod.palworld.trainer.win32.WindowProcessInfo
import dev.dexsr.gmod.palworld.trainer.win32.isProcessAlive
import kotlinx.coroutines.*
import org.jetbrains.skiko.MainUIDispatcher

@Composable
fun rememberTrainerMainScreenState(): TrainerMainScreenState {
    val state = remember {
        TrainerMainScreenState()
    }
    DisposableEffect(state) {
        state.init()
        onDispose { state.dispose() }
    }
    return state
}

@Stable
class TrainerMainScreenState {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope get() = requireNotNull(_coroutineScope) {
        "state class wasn't initialized"
    }

    var selectedProcess: TrainerTargetProcess? by mutableStateOf(null)
        private set

    private var selectedProcessLifetimeChecker: Job? = null

    fun userSelectProcess(processInfo: WindowProcessInfo) {
        val target = TrainerTargetProcess(processInfo.name, processInfo.id, processInfo.path)
        selectedProcess = target
        launchProcessLifetimeChecker(target)
    }

    private fun launchProcessLifetimeChecker(targetProcess: TrainerTargetProcess) {
        selectedProcessLifetimeChecker?.cancel()
        selectedProcessLifetimeChecker = coroutineScope.launch(MainUIDispatcher) {
            while (true) {
                if (!WinHelper.isProcessAlive(targetProcess.processId, targetProcess.processPath)) {
                    targetProcess.onDead()
                    selectedProcess = null
                    break
                }
                delay(1000)
            }
        }
    }

    fun init() {
        _coroutineScope = CoroutineScope(SupervisorJob())
    }

    fun dispose() {
        coroutineScope.cancel()
        selectedProcess?.onDispose()
    }
}

@Stable
class TrainerTargetProcess(
    val processName: String,
    val processId: Long,
    val processPath: String
) {
    var procHandle: Win32ProcessHandle? = null
        private set

    fun onDead() {

    }

    fun onDispose() {

    }
}