package dev.dexsr.gmod.palworld.toolbox.composeui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

object NoOpPainter : Painter() {

    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        // NO-OP
    }
}