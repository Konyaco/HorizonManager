package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.wvt.horizonmgr.ui.components.EmptyPage
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.ProgressDialog

private val repositories = listOf(
    "官方镜像源" to NewOnlineViewModel.Repository.OfficialMirror,
    "汉化组源" to NewOnlineViewModel.Repository.Chinese,
)

private val repositoryNames = repositories.map { it.first }
private val repositoryEnums = repositories.map { it.second }

private val mirrorSortModes = listOf(
    "推荐排序" to NewOnlineViewModel.MirrorSortMode.DEFAULT,
    "最热门" to NewOnlineViewModel.MirrorSortMode.FAVORITE_ASC,
    "最冷门" to NewOnlineViewModel.MirrorSortMode.FAVORITE_DSC,
    "近日发布" to NewOnlineViewModel.MirrorSortMode.TIME_ASC,
    "最早发布" to NewOnlineViewModel.MirrorSortMode.TIME_DSC,
    "近日更新" to NewOnlineViewModel.MirrorSortMode.UPDATE_TIME_ASC,
    "最早更新" to NewOnlineViewModel.MirrorSortMode.UPDATE_TIME_DSC,
    "名称排序" to NewOnlineViewModel.MirrorSortMode.NAME_ASC,
    "名称倒序" to NewOnlineViewModel.MirrorSortMode.NAME_DSC,
)

private val mirrorSortModeNames = mirrorSortModes.map { it.first }
private val mirrorSortModeEnums = mirrorSortModes.map { it.second }

private val chineseSortModes = listOf(
    "推荐排序" to NewOnlineViewModel.ChineseSortMode.DEFAULT,
    "最新发布" to NewOnlineViewModel.ChineseSortMode.TIME_ASC,
    "最早发布" to NewOnlineViewModel.ChineseSortMode.TIME_DSC,
    "名称正序" to NewOnlineViewModel.ChineseSortMode.NAME_ASC,
    "名称倒序" to NewOnlineViewModel.ChineseSortMode.NAME_DSC,
)

private val chineseSortModeNames = chineseSortModes.map { it.first }
private val chineseSortModeEnums = chineseSortModes.map { it.second }


@Composable
fun NewOnline(
    viewModel: NewOnlineViewModel,
    isLogon: Boolean,
    onNavClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val selectedRepository by viewModel.selectedRepository.collectAsState()
    val selectedChineseSortMode by viewModel.selectedCNSortMode.collectAsState()
    val selectedMirrorSortMode by viewModel.selectedMirrorSortMode.collectAsState()
    val mirrorMods by viewModel.cdnMods.collectAsState()
    val chineseMods by viewModel.chineseMods.collectAsState()
    val filterText by viewModel.filterText.collectAsState()
    val installState by viewModel.installState.collectAsState()

    DisposableEffect(isLogon) {
        if (isLogon) {
            viewModel.load()
        }
        onDispose { }
    }

    installState?.let { ProgressDialog(onCloseRequest = { viewModel.installFinish() }, state = it) }

    Column(Modifier.fillMaxSize()) {
        // Top App Bar
        NewTuneAppBar(
            enable = isLogon,
            onNavClicked = onNavClick,
            filterText = filterText,
            onFilterValueConfirm = { viewModel.setFilterText(it) },
            repositories = repositoryNames,
            selectedRepository = repositoryEnums.indexOf(selectedRepository),
            onRepositorySelect = { viewModel.setSelectedRepository(repositoryEnums[it]) },
            sortModes = if (selectedRepository == NewOnlineViewModel.Repository.OfficialMirror) {
                mirrorSortModeNames
            } else {
                chineseSortModeNames
            },
            selectedSortMode = if (selectedRepository == NewOnlineViewModel.Repository.OfficialMirror) {
                mirrorSortModeEnums.indexOf(selectedMirrorSortMode)
            } else {
                chineseSortModeEnums.indexOf(selectedChineseSortMode)
            },
            onSortModeSelect = {
                if (selectedRepository == NewOnlineViewModel.Repository.OfficialMirror) {
                    viewModel.setSelectedMirrorSortMode(mirrorSortModeEnums[it])
                } else {
                    viewModel.setSelectedCNSortMode(chineseSortModeEnums[it])
                }
            }
        )
        // Content
        if (isLogon) {
            Crossfade((state to selectedRepository)) { (state, selectedRepository) ->
                when (state) {
                    NewOnlineViewModel.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is NewOnlineViewModel.State.Error -> {
                        ErrorPage(message = {
                            Text(state.message)
                        }, onRetryClick = {
                            viewModel.load()
                        })
                    }
                    NewOnlineViewModel.State.Succeed -> when (selectedRepository) {
                        NewOnlineViewModel.Repository.OfficialMirror -> {
                            if (mirrorMods.isEmpty()) {
                                EmptyPage(Modifier.fillMaxSize()) {
                                    Text("什么模组都没有")
                                }
                            } else {
                                OfficialMirrorModList(
                                    modifier = Modifier.fillMaxSize(),
                                    mods = mirrorMods,
                                    onItemClick = { /*TODO*/ },
                                    onInstallClick = {
                                        viewModel.installMirrorMod(mirrorMods[it].id)
                                    }
                                )
                            }
                        }
                        NewOnlineViewModel.Repository.Chinese -> {
                            if (chineseMods.isEmpty()) {
                                EmptyPage(Modifier.fillMaxSize()) {
                                    Text("什么模组都没有")
                                }
                            } else {
                                ChineseModList(
                                    modifier = Modifier.fillMaxSize(),
                                    mods = chineseMods,
                                    onItemClick = { /*TODO*/ },
                                    onInstallClick = {
                                        viewModel.installChineseMod(chineseMods[it].id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            NotLoginTip()
        }
    }
}


@Composable
private fun NotLoginTip() {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "此功能仅在登录后可用",
            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
        )
    }
}
