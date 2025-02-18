package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun FileItem(name: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        icon = { Icon(Icons.Filled.InsertDriveFile, contentDescription = "文件") },
        text = { Text(name) }
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Column {
            FileItem(name = "Example File", onClick = { /*TODO*/ })
        }
    }
}