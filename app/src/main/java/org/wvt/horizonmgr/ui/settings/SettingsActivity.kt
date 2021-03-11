package org.wvt.horizonmgr.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import org.wvt.horizonmgr.BuildConfig
import org.wvt.horizonmgr.ui.startActivity
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.SideEffectStatusBar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = "Version " + BuildConfig.VERSION_NAME

        setContent {
            AndroidHorizonManagerTheme {
                SideEffectStatusBar()
                Surface {
                    Settings(
                        versionName = version,
                        onNavClick = { finish() },
                        requestCustomTheme = { startActivity<CustomThemeActivity>() }
                    )
                }
            }
        }
    }
}