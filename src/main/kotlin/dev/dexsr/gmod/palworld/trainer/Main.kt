package dev.dexsr.gmod.palworld.trainer

import dev.dexsr.gmod.palworld.trainer.main.composeui.MainGUI
import dev.dexsr.gmod.palworld.trainer.utilskt.fastForEach

suspend fun main(args: Array<String>) {
    val app = PalTrainerApplication(args)

    if (args.isNotEmpty())
        handleMainArgs(app, args)
    else MainGUI(app)
}

private suspend fun handleMainArgs(
    application: PalTrainerApplication,
    args: Array<String>
) {
    var hasValidArgs = false
    args.fastForEach { arg ->
        if (arg.endsWith(".sav")) {
            hasValidArgs = true
        }
    }
    if (hasValidArgs) MainGUI(application)
}