package dev.dexsr.gmod.palworld.trainer.composeui.main

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.application
import dev.dexsr.gmod.palworld.trainer.PalTrainerApplication
import java.awt.Dimension
import java.awt.Toolkit

@Suppress("FunctionName")
fun MainGUI(
    application: PalTrainerApplication
) {
    application {
        // we should already be in Swing EQ
        // let compose manage the lifecycle

        // TODO: complete it
        AwtWindow(
            visible = true,
            create = {
                println("AwtWindow: Create")
                PlatformMainAwtWindow()
                    .apply {
                        minimumSize = Dimension(600, 400)
                        size = Dimension(1280, 720)

                        // first show at center
                        run {
                            val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
                            val screenBounds = graphicsConfiguration.bounds
                            val size = IntSize(size.width, size.height)
                            val screenSize = IntSize(
                                screenBounds.width - screenInsets.left - screenInsets.right,
                                screenBounds.height - screenInsets.top - screenInsets.bottom
                            )
                            val location = Alignment.Center.align(size, screenSize, LayoutDirection.Ltr)

                            setLocation(
                                screenBounds.x + screenInsets.left + location.x,
                                screenBounds.y + screenInsets.top + location.y
                            )
                        }
                    }
            },
            dispose = {
                it.dispose()
            },
            update = {
                println("AwtWindow: Update")
            },
        )
    }
}

