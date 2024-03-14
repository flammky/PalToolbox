package dev.dexsr.gmod.palworld.toolbox.core

import kotlinx.coroutines.asCoroutineDispatcher

private object Dispatcher {

    val DISPATCHER = Core.MainExecutor.asCoroutineDispatcher()
}

val Core.MainDispatcher
    get() = Dispatcher.DISPATCHER