package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.service.level.LevelInfo
import org.wvt.horizonmgr.service.level.MCLevel
import org.wvt.horizonmgr.service.level.ZipMCLevel
import org.wvt.horizonmgr.ui.components.InputDialogHostState
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File

private const val TAG = "MCLevelTabVM"

class MCLevelTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val levelManager = dependencies.mcLevelManager
    private val levelTransporter = dependencies.levelTransporter
    private var manager = dependencies.manager
    private val localCache = dependencies.localCache
    private var currentPackage: InstalledPackage? = null
    val inputDialogState = InputDialogHostState()

    val levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    val errors = MutableStateFlow<List<String>>(emptyList())

    private var cachedLevels = emptyMap<LevelInfo, MCLevel>()

    val state = MutableStateFlow<State>(State.Loading)

    sealed class State {
        object Loading : State()
        object Done : State()
        class Error(val message: String) : State()
    }

    val progressState = MutableStateFlow<ProgressDialogState?>(null)

    fun load() {
        viewModelScope.launch(Dispatchers.Default) {
            state.emit(State.Loading)
            launch(Dispatchers.IO) level@{
                val result = try {
                    levelManager.getLevels()
                } catch (e: Exception) {
                    Log.e(TAG, "获取存档失败", e)
                    state.emit(State.Error("获取存档失败"))
                    return@level
                }
                val mappedErrors = result.errors.map {
                    "${it.file.absolutePath}: ${it.error.message ?: "未知错误"}"
                }
                val mapped = mutableMapOf<LevelInfo, MCLevel>().apply {
                    try {
                        result.levels.forEach {
                            put(it.getInfo(), it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "获取存档信息失败", e)
                        state.emit(State.Error("获取存档信息失败"))
                        return@level
                    }
                }.toMap()
                cachedLevels = mapped
                levels.emit(mapped.keys.toList())
                errors.emit(mappedErrors)
            }
            launch(Dispatchers.IO) pack@{
                val uuid = localCache.getSelectedPackageUUID() ?: return@pack
                currentPackage = manager.getInstalledPackage(uuid) ?: return@pack
            }
            joinAll()
            state.emit(State.Done)
        }
    }

    private suspend fun mDeleteLevel(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.delete()
        }
    }

    private suspend fun mRenameLevel(level: LevelInfo, newName: String) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.rename(newName)
        }
    }

    private suspend fun moveToHZ(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.let { mcLevel ->
                currentPackage?.let { pack ->
                    levelTransporter.moveToHZ(mcLevel, pack)
                }
            }
        }
    }

    private suspend fun copyToHZ(level: LevelInfo) {
        withContext(Dispatchers.IO) {
            cachedLevels[level]?.let { mcLevel ->
                currentPackage?.let { pack ->
                    levelTransporter.copyToHZ(mcLevel, pack)
                }
            }
        }
    }

    fun rename(item: LevelInfo) {
        viewModelScope.launch {
            val result: InputDialogHostState.DialogResult =
                inputDialogState.showDialog(item.name, "请输入新名称", "新名称")
            if (result is InputDialogHostState.DialogResult.Confirm) {
                progressState.emit(ProgressDialogState.Loading("正在重命名"))
                try {
                    mRenameLevel(item, result.input)
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressState.emit(
                        ProgressDialogState.Failed(
                            "重命名失败",
                            e.localizedMessage ?: "未知错误"
                        )
                    )
                    return@launch
                }
                progressState.emit(ProgressDialogState.Finished("重命名成功"))
                load()
            }
        }
    }

    fun delete(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在删除"))
            try {
                mDeleteLevel(item)
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("删除失败", e.localizedMessage ?: "未知错误"))
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("删除完成"))
            load()
        }
    }

    fun move(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在移动存档动到 Horizon"))
            try {
                moveToHZ(item)
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("移动失败", e.localizedMessage ?: "未知错误"))
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("移动完成"))
            load()
        }
    }

    fun copy(item: LevelInfo) {
        viewModelScope.launch {
            progressState.emit(ProgressDialogState.Loading("正在复制存档动到 Horizon"))
            try {
                copyToHZ(item)
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("复制失败", e.localizedMessage ?: "未知错误"))
                return@launch
            }
            progressState.emit(ProgressDialogState.Finished("复制完成"))
        }
    }

    fun selectedFileToInstall(path: String) {
        viewModelScope.launch {
            try {
                progressState.emit(ProgressDialogState.Loading("正在解析"))
                val file = File(path)
                val level = try {
                    ZipMCLevel.parse(file)
                } catch (e: ZipMCLevel.NotZipMCLevelException) {
                    progressState.emit(ProgressDialogState.Failed("解析失败", "您选择的文件可能不是一个正确的存档"))
                    return@launch
                }
                progressState.emit(ProgressDialogState.Loading("正在安装"))
                val task = levelManager.installLevel(level)
                // TODO: 2021/3/21 安装进度
                try {
                    task.await()
                } catch (e: Exception) {
                    progressState.emit(ProgressDialogState.Failed("安装失败", "安装时出现错误", e.message))
                }
            } catch (e: Exception) {
                progressState.emit(ProgressDialogState.Failed("安装失败", "出现未知错误", e.message))
            }
        }
    }

    fun dismissProgressDialog() {
        viewModelScope.launch {
            progressState.emit(null)
        }
    }
}