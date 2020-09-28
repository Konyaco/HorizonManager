package org.wvt.horizonmgr.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.theme.*

private enum class CheckedColorType {
    LIGHT_PRIMARY, LIGHT_PRIMARY_VARIANT,
    LIGHT_SECONDARY, LIGHT_SECONDARY_VARIANT,
    DARK_PRIMARY, DARK_PRIMARY_VARIANT,
    DARK_SECONDARY
}

data class CustomColor(
    var primary: Pair<String, Int>,
    var primaryVariant: Pair<String, Int>,
    var secondary: Pair<String, Int>,
    var secondaryVariant: Pair<String, Int>
)

@Composable
fun CustomTheme(requestClose: () -> Unit) {
    val emphasis = EmphasisAmbient.current
    val colors = remember { MaterialColors.series }
    var checkedColorType by remember { mutableStateOf(CheckedColorType.LIGHT_PRIMARY) }
    val themeConfig = ThemeConfigAmbient.current
    val themeController = ThemeControllerAmbient.current

    var lightColor by remember {
        mutableStateOf(
            CustomColor(
                primary = MaterialColors.parseColor(themeConfig.lightColor.primary)
                    ?: "cyan" to 500,
                primaryVariant = MaterialColors.parseColor(themeConfig.lightColor.primaryVariant)
                    ?: "cyan" to 700,
                secondary = MaterialColors.parseColor(themeConfig.lightColor.secondary)
                    ?: "teal" to 500,
                secondaryVariant = MaterialColors.parseColor(themeConfig.lightColor.secondaryVariant)
                    ?: "teal" to 700
            )
        )
    }

    var darkColor by remember {
        mutableStateOf(
            CustomColor(
                primary = MaterialColors.parseColor(themeConfig.darkColor.primary) ?: "cyan" to 500,
                primaryVariant = MaterialColors.parseColor(themeConfig.darkColor.primaryVariant)
                    ?: "cyan" to 700,
                secondary = MaterialColors.parseColor(themeConfig.darkColor.secondary)
                    ?: "teal" to 500,
                secondaryVariant = MaterialColors.parseColor(themeConfig.darkColor.secondaryVariant)
                    ?: "teal" to 700
            )
        )
    }

    fun getColor(color: Pair<String, Int>): Color {
        return Color(colors[color.first]!![color.second]!!)
    }

    Stack(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(navigationIcon = {
                IconButton(onClick = requestClose) {
                    Icon(asset = Icons.Filled.ArrowBack)
                }
            }, title = {
                Text("自定义主题")
            }, backgroundColor = MaterialTheme.colors.surface)

            MaterialColorPalette(
                modifier = Modifier.height(256.dp),
                colors = colors,
                onSelect = { s, i ->
                    when (checkedColorType) {
                        CheckedColorType.LIGHT_PRIMARY -> lightColor =
                            lightColor.copy(primary = s to i)
                        CheckedColorType.LIGHT_PRIMARY_VARIANT -> lightColor =
                            lightColor.copy(primaryVariant = s to i)
                        CheckedColorType.LIGHT_SECONDARY -> lightColor =
                            lightColor.copy(secondary = s to i)
                        CheckedColorType.LIGHT_SECONDARY_VARIANT -> lightColor =
                            lightColor.copy(secondaryVariant = s to i)
                        CheckedColorType.DARK_PRIMARY -> darkColor =
                            darkColor.copy(primary = s to i)
                        CheckedColorType.DARK_PRIMARY_VARIANT -> darkColor =
                            darkColor.copy(primaryVariant = s to i)
                        CheckedColorType.DARK_SECONDARY -> darkColor =
                            darkColor.copy(secondary = s to i)
                    }
                },
                selected = when (checkedColorType) {
                    CheckedColorType.LIGHT_PRIMARY -> lightColor.primary
                    CheckedColorType.LIGHT_PRIMARY_VARIANT -> lightColor.primaryVariant
                    CheckedColorType.LIGHT_SECONDARY -> lightColor.secondary
                    CheckedColorType.LIGHT_SECONDARY_VARIANT -> lightColor.secondaryVariant
                    CheckedColorType.DARK_PRIMARY -> darkColor.primary
                    CheckedColorType.DARK_PRIMARY_VARIANT -> darkColor.primaryVariant
                    CheckedColorType.DARK_SECONDARY -> darkColor.secondary
                }
            )
            Divider(Modifier.fillMaxWidth())
            ScrollableColumn {
                Text(
                    modifier = Modifier.padding(top = 16.dp, start = 24.dp),
                    text = "亮色主题",
                    color = MaterialTheme.colors.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "主题色",
                        color = getColor(lightColor.primary),
                        selected = checkedColorType == CheckedColorType.LIGHT_PRIMARY,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_PRIMARY })
                    SelectColorItem(text = "主题色 - 变体",
                        color = getColor(lightColor.primaryVariant),
                        selected = checkedColorType == CheckedColorType.LIGHT_PRIMARY_VARIANT,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_PRIMARY_VARIANT }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "补充色",
                        color = getColor(lightColor.secondary),
                        selected = checkedColorType == CheckedColorType.LIGHT_SECONDARY,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_SECONDARY })
                    SelectColorItem(text = "补充色 - 变体",
                        color = getColor(lightColor.secondaryVariant),
                        selected = checkedColorType == CheckedColorType.LIGHT_SECONDARY_VARIANT,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_SECONDARY_VARIANT }
                    )
                }
                Divider(Modifier.padding(vertical = 16.dp).fillMaxWidth())
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = "暗色主题",
                    color = MaterialTheme.colors.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "主题色",
                        color = getColor(darkColor.primary),
                        selected = checkedColorType == CheckedColorType.DARK_PRIMARY,
                        onSelect = { checkedColorType = CheckedColorType.DARK_PRIMARY })
                    SelectColorItem(text = "主题色 - 变体",
                        color = getColor(darkColor.primaryVariant),
                        selected = checkedColorType == CheckedColorType.DARK_PRIMARY_VARIANT,
                        onSelect = { checkedColorType = CheckedColorType.DARK_PRIMARY_VARIANT }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "补充色",
                        color = getColor(darkColor.secondary),
                        selected = checkedColorType == CheckedColorType.DARK_SECONDARY,
                        onSelect = { checkedColorType = CheckedColorType.DARK_SECONDARY })
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        Button(
            modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
            onClick = {
                themeController.setLightColor(
                    lightColors(
                        primary = getColor(lightColor.primary),
                        primaryVariant = getColor(lightColor.primaryVariant),
                        onPrimary = MaterialColors.contentColorFor(getColor(lightColor.primary)),
                        secondary = getColor(lightColor.secondary),
                        secondaryVariant = getColor(lightColor.secondaryVariant),
                        onSecondary = MaterialColors.contentColorFor(getColor(lightColor.secondary))
                    )
                )
                themeController.setDarkColor(
                    darkColors(
                        primary = getColor(darkColor.primary),
                        primaryVariant = getColor(darkColor.primaryVariant),
                        onPrimary = MaterialColors.contentColorFor(getColor(darkColor.primary)),
                        secondary = getColor(darkColor.secondary),
                        onSecondary = MaterialColors.contentColorFor(getColor(darkColor.secondary))
                    )
                )
            }
        ) {
            Icon(asset = Icons.Filled.Check)
            Text("保存")
        }
        /*Button(modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
            onClick = {
                themeController.setLightColor(LightColorPalette)
                themeController.setDarkColor(DarkColorPalette)
            }) {
            Icon(asset = Icons.Filled.Restore)
            Text("重置")
        }*/
    }
}

@Composable
private fun SelectColorItem(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scale = animate(if (selected) 1.1f else 1f)
    Column(modifier) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Text(text = text)
        }
        Surface(
            modifier = Modifier.size(128.dp, 96.dp).padding(top = 8.dp)
                .clickable(onClick = onSelect, indication = null)
                .drawLayer(scaleX = scale, scaleY = scale),
            color = animate(color),
            elevation = animate(if (selected) 8.dp else 0.dp),
            shape = RoundedCornerShape(size = 4.dp)
        ) {}
    }
}

@Composable
private fun MaterialColorPalette(
    modifier: Modifier = Modifier,
    colors: Map<String, Map<Int, Long>>,
    onSelect: (series: String, bright: Int) -> Unit,
    selected: Pair<String, Int>
) {
    val tags = remember {
        listOf<String>(
            "50", "100", "200", "300", "400", "500", "600", "700", "800", "900",
            "A100", "A200", "A400", "A700"
        )
    }
    ScrollableColumn(modifier) {
        ScrollableRow {
            Column {
                Spacer(Modifier.padding(1.dp).height(42.dp))
                ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                    for (i in colors) {
                        Box(
                            modifier = Modifier.padding(1.dp).height(42.dp).width(72.dp),
                            gravity = ContentGravity.Center
                        ) {
                            Text(
                                text = i.key,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Column {
                Row {
                    ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                        tags.forEach {
                            Box(
                                modifier = Modifier.padding(1.dp).size(42.dp),
                                gravity = Alignment.Center
                            ) { Text(text = it) }
                        }
                    }
                }
                for ((series, map) in colors) {
                    Row {
                        for ((bright, color) in map) {
                            ColorItem(
                                modifier = Modifier.padding(1.dp),
                                color = Color(color),
                                selected = selected.first == series && selected.second == bright,
                                onSelect = { onSelect(series, bright) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColorItem(
    modifier: Modifier = Modifier,
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val roundPercent = animate(if (selected) 50 else 0)
    val contentColor = remember(color) {
        MaterialColors.contentColorFor(color)
    }
    Surface(
        modifier = modifier.clickable(
            onClick = onSelect,
            indication = null
        ).size(42.dp),
        color = color,
        shape = RoundedCornerShape(percent = roundPercent)
    ) {
        AnimatedVisibility(
            visible = selected, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), gravity = ContentGravity.Center) {
                Text(text = "T", color = contentColor)
            }
        }
    }
}

@Preview
@Composable
private fun ColorItemPreview() {
    HorizonManagerTheme {
        var selected by remember { mutableStateOf(true) }
        Surface(Modifier.padding(32.dp)) {
            ColorItem(
                modifier = Modifier.padding(1.dp),
                color = Color(MaterialColors.purple[500]!!),
                selected = selected,
                onSelect = { selected = true })
        }
    }
}

