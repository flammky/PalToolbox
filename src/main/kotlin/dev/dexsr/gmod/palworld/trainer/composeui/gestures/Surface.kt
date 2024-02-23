package dev.dexsr.gmod.palworld.trainer.composeui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput

// fixme: in windows, consuming pointer event on title bar spot should mean to provide matrix info to the TitleBar as well
// so we should expose a Composable rather than modifier factory
fun Modifier.defaultSurfaceGestureModifiers() = consumeDownGesture(
    requireUnconsumed = true,
    eventPass = PointerEventPass.Main
)

private fun Modifier.consumeDownGesture(
    requireUnconsumed: Boolean = true,
    eventPass: PointerEventPass = PointerEventPass.Main
): Modifier = pointerInput(requireUnconsumed, eventPass) {
    awaitEachGesture {
        awaitFirstDown(
            requireUnconsumed = requireUnconsumed,
            pass = eventPass
        ).apply(PointerInputChange::consume)
    }
}