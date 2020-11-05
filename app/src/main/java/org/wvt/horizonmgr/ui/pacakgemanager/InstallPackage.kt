package org.wvt.horizonmgr.ui.pacakgemanager

import androidx.compose.animation.*
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.ui.HorizonManagerAmbient
import org.wvt.horizonmgr.ui.WebAPIAmbient

private data class Step(
    val icon: VectorAsset,
    val label: String,
    val progressable: Boolean
)

// TODO 反正是要改的 屎山就屎山吧
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InstallPackage(packInfo: WebAPI.ICPackage, name: String, onFinished: () -> Unit) {
    var totalProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val horizonMgr = HorizonManagerAmbient.current
    val webApi = WebAPIAmbient.current

    val steps = remember {
        listOf(
            Step(Icons.Filled.CloudDownload, "正在下载", true),
            Step(Icons.Filled.Inbox, "正在安装", false),
        )
    }

    var currentStep by remember { mutableStateOf(0) }
    var currentProgress by remember { mutableStateOf(0f) }
    var currentStepState by remember { mutableStateOf(0) } // 0: todoit, 1: doing, 2: failed

    onActive {
        scope.launch {
            val task = webApi.downloadPackage(packInfo)
            task.progressChannel().receiveAsFlow().conflate().collect {
                delay(200)
                currentProgress = it
            }
            val result = try {
                task.await()
            } catch (e: Exception) {
                currentStepState = 2
                e.printStackTrace()
                return@launch
            }
            totalProgress = 0.5f
            currentStep++
            delay(500)

            try {
                horizonMgr.installPackage(name, result.first, result.second, packInfo.uuid)
            } catch (e: Exception) {
                currentStepState = 2
                e.printStackTrace()
                return@launch
            }

            delay(500)
            totalProgress = 1f
            currentStep++
        }
    }

    Stack(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = {
                Crossfade(if (totalProgress >= 1f) "安装完成" else "正在安装") {
                    Text(it)
                }
            }, navigationIcon = {
                // 安装目前不可被取消
            }, backgroundColor = MaterialTheme.colors.surface)
            Row(Modifier.height(2.dp).fillMaxWidth()) {
                AnimatedVisibility(
                    visible = totalProgress < 1f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = animate(totalProgress),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            ScrollableColumn(Modifier.fillMaxSize()) {
                steps.forEachIndexed { index, step ->
                    val state =
                        when {
                            index == currentStep && currentStepState == 2 -> -1
                            index < currentStep -> 0 // finished
                            index == currentStep -> 1 // doing
                            index > currentStep -> 2 // todoit
                            else -> -1
                        }
                    val contentColor = animate(
                        if (state == 2) contentColor().copy(alpha = 0.5f)
                        else MaterialTheme.colors.onSurface
                    )
                    ListItem(
                        modifier = Modifier.height(72.dp),
                        icon = {
                            Icon(step.icon, Modifier, contentColor)
                        }, text = {
                            Text(
                                text = step.label,
                                color = contentColor
                            )
                        }, trailing = {
                            Stack(Modifier.width(48.dp)) {
                                Crossfade(
                                    current = state,
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    if (state == 1) {
                                        // Doing
                                        if (step.progressable)
                                            CircularProgressIndicator(animate(currentProgress))
                                        else CircularProgressIndicator()
                                    } else if (state == 0) {
                                        // Finished
                                        Icon(Icons.Filled.Check)
                                    } else if (state == -1) {
                                        Icon(Icons.Filled.Error)
                                    }
                                    // Todoit - Nothing
                                }
                            }
                        }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = totalProgress >= 1f, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Button(onClick = onFinished) {
                Text("完成")
            }
        }
    }
}