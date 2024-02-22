package dev.dexsr.gmod.palworld.trainer.main.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonFontScaled
import dev.dexsr.gmod.palworld.trainer.composeui.text.nonScaledFontSize
import dev.dexsr.gmod.palworld.trainer.game.composeui.trainerMainScreenDrawerItem
import dev.dexsr.gmod.palworld.trainer.savegame.composeui.saveGameMainScreenDrawerItem
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.*


@Composable
fun MainScreen() {
    val state = rememberMainScreenState()
    MainScreen(state)
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun MainScreen(
    state: MainScreenState
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainScreenLayoutSurface(
            modifier = Modifier,
            color = remember { Color(24, 20, 28) }
        )
        MainScreenLayoutContent(
            contentPadding = PaddingValues(horizontal = MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp)
        )
    }
}

@Composable
fun MainScreenLayoutSurface(
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .defaultSurfaceGestureModifiers()
    )
}

@Composable
fun MainScreenLayoutContent(
    contentPadding: PaddingValues
) {
    val leftPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
    val rightPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
    Column(
        modifier = Modifier
            .padding(start = leftPadding, end = rightPadding)
    ) {
        MainScreenLayoutTopBar(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()))
        MainScreenLayoutBody()
    }
}

@Composable
fun MainScreenLayoutTopBar(
    contentPadding: PaddingValues = PaddingValues()
) {
    Box {
        Row(
            modifier = Modifier.padding(contentPadding).height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainScreenLayoutIconTitle(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
            )
            MainScreenLayoutCaptionControls()
        }
    }
}

@Composable
private fun MainScreenLayoutIconTitle(
    modifier: Modifier
) {
    Row(modifier) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource("drawable/palworld_p_icon.png"),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = "PALWORLD TRAINER",
            style = MaterialTheme.typography.subtitle2,
            fontSize = MaterialTheme.typography.subtitle2.nonScaledFontSize(),
            color = Color.White,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainScreenLayoutCaptionControls(

) {
    val titleBarBehavior = LocalTitleBarBehavior.current
    Row {
        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.minimizeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_minimize_win1.png"),
                contentDescription = null,
                tint = Color.White
            )
        }

        run {
            // keep lambda and painter in sync
            val showRestore = titleBarBehavior.showRestoreWindow
            Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
                if (showRestore) titleBarBehavior.restoreClicked() else titleBarBehavior.maximizeClicked()
            }) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    painter = if (!showRestore)
                        painterResource("drawable/windowcontrol_maximized_win.png")
                    else
                        painterResource("drawable/windowcontrol_restore_down.png"),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.closeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_close2.png"),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun MainScreenLayoutBody() {
    BoxWithConstraints {
        val maxWidth = maxWidth
        Row {
            val dest = remember { mutableStateOf<MainDrawerDestination?>(null) }
            Column(
                modifier = Modifier
                    .width(
                        (15f / 100 * maxWidth.value)
                            .coerceIn(150f..250f).dp
                    )
            ) {
                MainScreenLayoutDrawerNavigationPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    onDestinationClicked = dest::value::set,
                    currentDestinationId = dest.value?.id
                )
                Box(modifier = Modifier.height(80.dp).fillMaxWidth())
            }
            Spacer(modifier = Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
            MainScreenLayoutScreenHost(dest.value)
        }
    }
}

@Composable
fun MainScreenLayoutDrawerNavigationPanel(
    modifier: Modifier,
    onDestinationClicked: (MainDrawerDestination) -> Unit,
    currentDestinationId: String?
) {
    Column(modifier.fillMaxSize()) {
        run {
            val saveGame = trainerMainScreenDrawerItem()
            val isSelected = currentDestinationId == saveGame.id
            DrawerNavigationPanelItem(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .composed {
                        Modifier
                            .then(
                                if (isSelected)
                                    Modifier.background(remember { Color(31, 26, 36) })
                                else
                                    Modifier
                            )
                    }
                    .clickable(
                        enabled = !isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) { onDestinationClicked(saveGame) },
                item = saveGame
            )
        }
        run {
            val saveGame = saveGameMainScreenDrawerItem()
            val isSelected = currentDestinationId == saveGame.id
            DrawerNavigationPanelItem(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .composed {
                        Modifier
                            .then(
                                if (isSelected)
                                    Modifier.background(remember { Color(31, 26, 36) })
                                else
                                    Modifier
                            )
                    }
                    .clickable(
                        enabled = !isSelected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) { onDestinationClicked(saveGame) },
                item = saveGame
            )
        }
    }
}

@Composable
fun MainScreenLayoutScreenHost(
    currentDestination: MainDrawerDestination?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()

    ) {
        currentDestination
            ?.let { dest ->
                key(dest.id) { dest.content.invoke() }
            }
            ?: HostNoDestinationSelected()
    }
}

@Composable
private fun DrawerNavigationPanelItem(
    modifier: Modifier,
    item: MainDrawerDestination
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 10.dp, horizontal = 15.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
            painter = item.icon,
            tint = Color.Unspecified,
            contentDescription = null
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = item.text,
            style = MaterialTheme.typography.subtitle2.nonFontScaled(),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = Color.White
        )
    }
}

@Composable
private fun HostNoDestinationSelected() {

}
