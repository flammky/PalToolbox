package dev.dexsr.gmod.palworld.trainer.platform.content.filepicker

import dev.dexsr.gmod.palworld.trainer.platform.content.filepicker.win32.JnaFileChooser
import kotlinx.coroutines.*
import java.awt.Window
import java.io.File

class JnaFileChooserWindowHost(
    private val parentWindow: Window,
    private val initialTitle: String,
    private val initialDir: String?
) : Window(parentWindow) {
    private val jnaFileChooser = JnaFileChooser()
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private var current: Deferred<Result<File?>>? = null

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
    }

    override fun dispose() {
        super.dispose()
        coroutineScope.cancel()
        current?.cancel()
    }


    fun openAndInvokeOnCompletion(
        handle: (Result<File?>) -> Unit
    ): DisposableHandle {
        isVisible = true
        val task = current?.takeIf { it.isActive }
            ?: run {
                coroutineScope.async(Dispatchers.IO) {
                    runCatching {
                        jnaFileChooser.setTitle(initialTitle)
                        jnaFileChooser.setCurrentDirectory(initialDir)
                        jnaFileChooser.addFilter("Save File (*.sav)", "sav")
                        jnaFileChooser.showOpenDialog(parent = this@JnaFileChooserWindowHost)
                        jnaFileChooser.selectedFiles.first()
                    }
                }
            }.also { current = it }

        @OptIn(InternalCoroutinesApi::class)
        return task.invokeOnCompletion(onCancelling = true, invokeImmediately = true) { ex ->
            if (ex != null) handle.invoke(Result.failure(ex)) else handle.invoke(task.getCompleted())
        }
    }
}