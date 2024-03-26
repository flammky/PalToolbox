package dev.dexsr.gmod.palworld.toolbox.savegame.composeui.pals

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dexsr.gmod.palworld.toolbox.composeui.NoOpPainter
import dev.dexsr.gmod.palworld.toolbox.game.PalGender
import dev.dexsr.gmod.palworld.toolbox.savegame.composeui.SaveGameEditState
import dev.dexsr.gmod.palworld.toolbox.theme.md3.composeui.Material3Theme
import dev.dexsr.gmod.palworld.trainer.composeui.HeightSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.WidthSpacer
import dev.dexsr.gmod.palworld.trainer.composeui.gestures.defaultSurfaceGestureModifiers
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.MD3Spec
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.incrementsDp
import dev.dexsr.gmod.palworld.trainer.uifoundation.themes.md3.padding

@Composable
fun PalsEditPanel(
    modifier: Modifier = Modifier,
    state: SaveGameEditState
) = PalsEditPanel(
    modifier,
    rememberPalsEditPanelState(state)
        .apply {
            remember(this) {
                Mock()
                    .mockInit()
            }
        }
)

@Composable
fun PalsEditPanel(
    modifier: Modifier = Modifier,
    state: PalsEditPanelState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(remember { Color(29, 24, 34) })
            .defaultSurfaceGestureModifiers()
    ) {

        Column {
            PalsTopBarActions(state)
            HeightSpacer(MD3Spec.padding.incrementsDp(4).dp)
            PalsContentColumn(state)
        }

        state.editPal?.let { pal ->
            PalEditPanel(
                Modifier,
                state,
                pal
            )
        }
    }
}

@Composable
private fun PalsEditPanelPalLazyListItem(
    modifier: Modifier,
    getName: () -> String,
    getUid: () -> String,
    getBreedName: () -> String,
    getGender: () -> PalGender?,
    getLevel: () -> String,
    isNamed: Boolean,
    isAlpha: Boolean,
    isLucky: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            modifier = Modifier.width(125.dp),
            text = getName(),
            color = Color(252, 252, 252).copy(alpha = if (isNamed) 1f else 0.68f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.caption,
            maxLines = 1
        )

        WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)

        Box(
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFAED285))
                    .padding(vertical = 2.dp, horizontal = 6.dp)
            ) {
                Text(
                    modifier = Modifier,
                    text = getBreedName(),
                    color = Color(0xFF221728),
                    fontWeight = FontWeight.SemiBold,
                    style = Material3Theme.typography.labelMedium,
                    maxLines = 1
                )
                val gender = getGender()
                if (gender != null && gender !is PalGender.Named) {
                    WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = when(gender) {
                            PalGender.Female -> painterResource("drawable/gender_female_16px.png")
                            PalGender.Male -> painterResource("drawable/gender_male_16px.png")
                            else -> NoOpPainter
                        },
                        contentDescription = null,
                        tint = Color(10,10,10)
                    )
                }
                if (isLucky) {
                    WidthSpacer(4.dp)
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource("drawable/sparkle1_filled_16px.png"),
                        contentDescription = null,
                        tint = Color(0xFFd1831b)
                    )
                } else if (isAlpha) {
                    WidthSpacer(4.dp)
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource("drawable/devil_simple_16px.png"),
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        }

        WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFA7C8FD))
                .padding(vertical = 2.dp, horizontal = 6.dp)
        ) {
            Text(
                modifier = Modifier,
                text = "Lv.${getLevel()}",
                color = Color(0xFF221728),
                fontWeight = FontWeight.SemiBold,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1
            )
        }

        WidthSpacer(MD3Spec.padding.incrementsDp(1).dp)

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFf5d9ff))
                .padding(vertical = 2.dp, horizontal = 6.dp)
        ) {
            Text(
                modifier = Modifier,
                text = "UID: ${getUid()}",
                color = Color(0xFF221728),
                fontWeight = FontWeight.SemiBold,
                style = Material3Theme.typography.labelMedium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun PalsTopBarActions(
    state: PalsEditPanelState
) {

    Column() {
        val openFilter = remember {
            mutableStateOf(false)
        }
        Row {

            run {

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { openFilter.value = !openFilter.value }
                        .border(
                            width = 1.dp,
                            color = Color(0xFF79747E),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource("drawable/filter_sort_24px.png"),
                        contentDescription = null,
                        tint = if (openFilter.value) Color(0xFFD1BCFD) else Color.White
                    )

                    WidthSpacer(8.dp)

                    Text(
                        text = "Filter",
                        color = Color(252, 252, 252),
                        style = Material3Theme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

        }

        if (openFilter.value) {
        }

        HeightSpacer(8.dp)

        HorizontalDivider(
            modifier = Modifier.width(800.dp),
            color = Color(0xFF978e98)
        )
    }
}

@Composable
private fun PalsContentColumn(
    state: PalsEditPanelState
) {
    Row {
        val heightState = remember {
            mutableStateOf(0.dp)
        }
        val scrollState = rememberLazyListState()
        val density = LocalDensity.current
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .width(800.dp)
                .onSizeChanged { size -> heightState.value = with(density) {
                    size.height.toDp()
                } },
            state = scrollState
        ) {
            val pals = state.filteredPals
            items(
                pals.size,
                key = { i -> pals[i] }
            ) { i ->
                val palUid = pals[i]
                val palData = state
                    .observePalIndividualData(palUid)
                    .collectAsState(state.cachedPalIndividualData(palUid))
                PalsEditPanelPalLazyListItem(
                    Modifier,
                    getName = { palData.value?.attributeDisplayData?.displayName ?: "" },
                    getUid = { palData.value?.attributeDisplayData?.dashSeparatedUid ?: "" },
                    getBreedName = { palData.value?.attributeDisplayData?.breed ?: "" },
                    getGender = { palData.value?.attribute?.gender },
                    getLevel = { (palData.value?.attribute?.level ?: 0).toString() },
                    isNamed = palData.value?.attributeDisplayData?.isNamed == true,
                    isAlpha = palData.value?.attributeDisplayData?.isAlpha == true,
                    isLucky = palData.value?.attributeDisplayData?.isLucky == true,
                    onClick = { state.editPal(palUid) }
                )
                if (i < pals.lastIndex) {
                    HeightSpacer(8.dp)
                }
            }
        }
        WidthSpacer(4.dp)
        VerticalScrollbar(
            modifier = Modifier.height(heightState.value),
            adapter = rememberScrollbarAdapter(scrollState),
            style = remember {
                defaultScrollbarStyle().copy(
                    unhoverColor = Color.White.copy(alpha = 0.12f),
                    hoverColor = Color.White.copy(alpha = 0.50f)
                )
            }
        )
    }
}