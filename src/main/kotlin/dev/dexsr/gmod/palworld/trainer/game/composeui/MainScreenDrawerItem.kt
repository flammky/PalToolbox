package dev.dexsr.gmod.palworld.trainer.game.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.LayoutDirection
import dev.dexsr.gmod.palworld.trainer.main.composeui.MainDrawerDestination
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.SaveGameFeaturesScreen

@Composable
fun trainerMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { SaveGameFeaturesScreen() }
    // TODO: icon
    val painter = remember {
        object : Painter() {
            override val intrinsicSize: Size
                get() = Size.Unspecified

            override fun applyAlpha(alpha: Float): Boolean {
                return super.applyAlpha(alpha)
            }

            override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
                return super.applyColorFilter(colorFilter)
            }

            override fun applyLayoutDirection(layoutDirection: LayoutDirection): Boolean {
                return super.applyLayoutDirection(layoutDirection)
            }

            override fun DrawScope.onDraw() {
            }
        }
    }
    return remember(painter) {
        MainDrawerDestination(
            id = "trainer",
            icon = painter,
            text = "Game",
            content = content
        )
    }
}