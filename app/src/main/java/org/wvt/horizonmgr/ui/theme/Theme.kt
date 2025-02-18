package org.wvt.horizonmgr.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val LocalThemeController =
    staticCompositionLocalOf<ThemeController> { error("No theme controller provided") }
val LocalThemeConfig = staticCompositionLocalOf<ThemeConfig> { error("No theme config provided") }

interface ThemeController {
    fun setFollowSystemDarkTheme(enable: Boolean)
    fun setCustomDarkTheme(enable: Boolean)
    fun setLightColor(color: Colors)
    fun setDarkColor(color: Colors)
    fun setAppbarAccent(enable: Boolean)
}

val AppBarBackgroundColor: Color
    @Composable
    @ReadOnlyComposable
    get() {
        val config = LocalThemeConfig.current
        return if (config.appbarAccent && !config.isDark) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        }
    }

val defaultTypography = Typography(
    defaultFontFamily = FontFamily.Default
)

data class ThemeConfig(
    val followSystemDarkMode: Boolean,
    val isSystemInDark: Boolean,
    val isCustomInDark: Boolean,
    val lightColor: Colors,
    val darkColor: Colors,
    val appbarAccent: Boolean
) {
    val isDark = if (followSystemDarkMode) isSystemInDark else isCustomInDark
    val color = if (isDark) darkColor else lightColor
    val appbarColor = if (appbarAccent && !isDark) color.primary else color.surface
    val statusBarColor = if (appbarAccent && !isDark) color.primaryVariant else color.surface
}

@Composable
fun HorizonManagerTheme(
    controller: ThemeController = DefaultThemeController,
    config: ThemeConfig = DefaultThemeConfig,
    content: @Composable () -> Unit
) {
    val targetColors = if (config.isDark) config.darkColor else config.lightColor
    val colors = Colors(
        primary = animateColorAsState(targetColors.primary).value,
        primaryVariant = animateColorAsState(targetColors.primaryVariant).value,
        secondary = animateColorAsState(targetColors.secondary).value,
        secondaryVariant = animateColorAsState(targetColors.secondaryVariant).value,
        background = animateColorAsState(targetColors.background).value,
        surface = animateColorAsState(targetColors.surface).value,
        error = animateColorAsState(targetColors.error).value,
        onPrimary = animateColorAsState(targetColors.onPrimary).value,
        onSecondary = animateColorAsState(targetColors.onSecondary).value,
        onBackground = animateColorAsState(targetColors.onBackground).value,
        onSurface = animateColorAsState(targetColors.onSurface).value,
        onError = animateColorAsState(targetColors.onError).value,
        isLight = targetColors.isLight
    )
    CompositionLocalProvider(
        LocalThemeController provides controller,
        LocalThemeConfig provides config
    ) {
        MaterialTheme(
            colors = colors,
            content = content,
            typography = defaultTypography
        )
    }
}

@Composable
fun PreviewTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalThemeController provides DefaultThemeController,
        LocalThemeConfig provides DefaultThemeConfig
    ) {
        MaterialTheme(colors = LightColorPalette, content = content, typography = defaultTypography)
    }
}

object DefaultThemeController : ThemeController {
    override fun setFollowSystemDarkTheme(enable: Boolean) {}
    override fun setCustomDarkTheme(enable: Boolean) {}
    override fun setLightColor(color: Colors) {}
    override fun setDarkColor(color: Colors) {}
    override fun setAppbarAccent(enable: Boolean) {}
}

val DefaultThemeConfig = ThemeConfig(
    followSystemDarkMode = true,
    isSystemInDark = false,
    isCustomInDark = false,
    lightColor = LightColorPalette,
    darkColor = DarkColorPalette,
    appbarAccent = false
)