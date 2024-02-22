package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.sun.jna.platform.win32.WinNT
import dev.dexsr.gmod.palworld.trainer.win32.WindowProcessInfo
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberTrainerMainScreenState(): TrainerMainScreenState {
    return remember {
        TrainerMainScreenState()
    }
}

@Stable
class TrainerMainScreenState {

    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope get() = requireNotNull(_coroutineScope) {
        "state class wasn't initialized"
    }

    var selectedProcess: TrainerTargetProcess? = null
        private set

    fun userSelectProcess(processInfo: WindowProcessInfo) {
        selectedProcess = TrainerTargetProcess(processInfo.name, processInfo.id)
    }
}

class TrainerTargetProcess(
    val processName: String,
    val processId: Long,
) {
    var procHandle: WinNT.HANDLE? = null
        private set
}