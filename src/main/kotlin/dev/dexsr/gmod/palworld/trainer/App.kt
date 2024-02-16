package dev.dexsr.gmod.palworld.trainer

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class PalTrainerApplication(
    val args: Array<String>
) {

    // our application coroutine dispatcher is separate from UI
    val coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}

