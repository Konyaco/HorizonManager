package org.wvt.horizonmgr.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Colors
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.wvt.horizonmgr.ui.donate.alipayColor
import org.wvt.horizonmgr.ui.donate.wechatColor

private val config = mutableStateOf<ThemeConfig>(DefaultThemeConfig)

@Composable
fun AndroidHorizonManagerTheme(
    fullScreen: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeController = remember(context) { AndroidThemeController(context) }

    val controller = rememberSystemUiController()

    DisposableEffect(Unit) {
        WindowCompat.setDecorFitsSystemWindows((context as Activity).window, false)
        onDispose {}
    }
    DisposableEffect(isSystemInDarkTheme()) {
        themeController.update()
        onDispose { }
    }

    var statusBarColor by remember {
        mutableStateOf(Color.Transparent)
    }

    var navigationBarColor by remember {
        mutableStateOf(Color.Transparent)
    }

    DisposableEffect(config.value) {
        val config = config.value
        val color = if (config.isDark) config.darkColor else config.lightColor
        statusBarColor = if (fullScreen) {
            color.background
        } else {
            if (config.isDark) {
                color.surface
            } else {
                if (config.appbarAccent) color.primaryVariant
                else color.surface
            }
        }

        controller.setStatusBarColor(Color.Transparent, MaterialColors.isLightColor(statusBarColor))
        navigationBarColor = color.background
        controller.setNavigationBarColor(
            Color.Transparent,
            MaterialColors.isLightColor(navigationBarColor)
        )
        onDispose { }
    }

    ProvideWindowInsets {
        HorizonManagerTheme(
            controller = themeController,
            config = config.value,
        ) {
            val insets = LocalWindowInsets.current
            val statusBarHeight: Dp
            val navigationBarHeight: Dp
            with(LocalDensity.current) {
                statusBarHeight = insets.statusBars.top.toDp()
                navigationBarHeight = insets.navigationBars.bottom.toDp()
            }
            Column(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarHeight),
                    color = statusBarColor, content = {},
                    elevation = if (fullScreen) 0.dp else 4.dp
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                    content()
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navigationBarHeight),
                    color = navigationBarColor, content = {}
                )
            }
        }
    }
}

@Composable
fun AndroidDonateTheme(content: @Composable() () -> Unit) {
    val context = LocalContext.current.applicationContext
    val themeController = remember(context) { AndroidThemeController(context) }


    DisposableEffect(isSystemInDarkTheme()) {
        themeController.update()
        onDispose { }
    }

    val controller = rememberSystemUiController()
    DisposableEffect(config.value) {
        val config = config.value
        val color = if (config.isDark) config.darkColor else config.lightColor

        if (config.isDark) {
            controller.setSystemBarsColor(color.background)
        } else {
            controller.setStatusBarColor(alipayColor)
            controller.setNavigationBarColor(wechatColor)
        }
        onDispose { }
    }

    ProvideWindowInsets {
        HorizonManagerTheme(
            controller = themeController,
            config = config.value,
            content = content
        )
    }
}

private class LocalConfig(context: Context) {
    private val themePreference =
        context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    private val lightThemePreference =
        context.getSharedPreferences("light_theme", Context.MODE_PRIVATE)
    private val darkThemePreference =
        context.getSharedPreferences("dark_theme", Context.MODE_PRIVATE)

    fun getFollowSystemDarkMode(): Boolean {
        return themePreference.getBoolean("follow_system_dark_theme", true)
    }

    fun setFollowSystemDarkMode(enable: Boolean) {
        themePreference.edit { putBoolean("follow_system_dark_theme", enable) }
    }

    fun getCustomDarkMode(): Boolean {
        return themePreference.getBoolean("custom_dark_theme", false)
    }

    fun setCustomDarkMode(enable: Boolean) {
        themePreference.edit { putBoolean("custom_dark_theme", enable) }
    }

    fun getLightColor(): Colors {
        return with(lightThemePreference) {
            try {
                lightColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    secondaryVariant = getString("secondary_variant", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) {
                LightColorPalette
            }
        }
    }

    fun setLightColor(color: Colors) {
        lightThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("secondary_variant", color.secondaryVariant.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
    }

    fun getDarkColor(): Colors {
        return with(darkThemePreference) {
            try {
                darkColors(
                    primary = getString("primary", null)!!.toColor(),
                    primaryVariant = getString("primary_variant", null)!!.toColor(),
                    onPrimary = getString("on_primary", null)!!.toColor(),
                    secondary = getString("secondary", null)!!.toColor(),
                    onSecondary = getString("on_secondary", null)!!.toColor()
                )
            } catch (e: Exception) {
                DarkColorPalette
            }
        }
    }

    fun setDarkColor(color: Colors) {
        darkThemePreference.edit {
            putString("primary", color.primary.toHexString())
            putString("primary_variant", color.primaryVariant.toHexString())
            putString("on_primary", color.onPrimary.toHexString())
            putString("secondary", color.secondary.toHexString())
            putString("on_secondary", color.onSecondary.toHexString())
        }
    }

    fun isAppbarAccent(): Boolean {
        return themePreference.getBoolean("is_appbar_accent", false)
    }

    fun setAppbarAccent(enable: Boolean) {
        themePreference.edit {
            putBoolean("is_appbar_accent", enable)
        }
    }
}

internal class AndroidThemeController(
    private val context: Context
) : ThemeController {

    private val localConfig = LocalConfig(context)

    init {
        update()
    }

    fun update() {
        val lightColor = localConfig.getLightColor()
        val darkColor = localConfig.getDarkColor()
        val appbarAccent = localConfig.isAppbarAccent()
//        val followSystem = AppCompatDelegate.getDefaultNightMode()

        val configFollowSystem = localConfig.getFollowSystemDarkMode()
        val isConfigCustomInDark = localConfig.getCustomDarkMode()
        val isSystemInDark =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        config.value = config.value.copy(
            followSystemDarkMode = configFollowSystem,
            isSystemInDark = isSystemInDark,
            isCustomInDark = isConfigCustomInDark,
            lightColor = lightColor,
            darkColor = darkColor,
            appbarAccent = appbarAccent
        )
    }

    fun isFollowingSystemDarkTheme(): Boolean {
        return localConfig.getFollowSystemDarkMode()
    }

    override fun setFollowSystemDarkTheme(enable: Boolean) {
        localConfig.setFollowSystemDarkMode(enable)
        update()
    }

    override fun setCustomDarkTheme(enable: Boolean) {
        localConfig.setCustomDarkMode(enable)
        update()
    }

    override fun setLightColor(color: Colors) {
        localConfig.setLightColor(color)
        update()
    }

    override fun setDarkColor(color: Colors) {
        localConfig.setDarkColor(color)
        update()
    }

    override fun setAppbarAccent(enable: Boolean) {
        localConfig.setAppbarAccent(enable)
        update()
    }
}

private fun String.toColor(): Color {
    return Color(toLong(16))
}

private fun Color.toHexString(): String {
    return value.shr(32).toString(16)
}