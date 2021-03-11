package org.wvt.horizonmgr.ui.onlinemoddetail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import org.wvt.horizonmgr.ui.theme.AndroidHorizonManagerTheme
import org.wvt.horizonmgr.ui.theme.SideEffectStatusBar

class OnlineModDetailActivity : AppCompatActivity() {
    companion object {
        const val INTENT_MOD_ID = "mod_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val modId = intent.getStringExtra(INTENT_MOD_ID) ?: error("Mod id not specified.")

        // TODO: 2020/10/30
        setContent {
            AndroidHorizonManagerTheme {
                SideEffectStatusBar()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    OnlineModDetail()
                }
            }
        }
    }
}