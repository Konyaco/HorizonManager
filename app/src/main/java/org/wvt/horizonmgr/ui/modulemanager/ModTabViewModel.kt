package org.wvt.horizonmgr.ui.modulemanager

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.InstalledPackage
import org.wvt.horizonmgr.service.mod.InstalledMod
import org.wvt.horizonmgr.service.mod.ZipMod
import org.wvt.horizonmgr.ui.components.ProgressDialogState
import java.io.File
import java.util.*

private const val TAG = "ModTabVM"

class ModTabViewModel(dependencies: DependenciesContainer) : ViewModel() {
    private val manager = dependencies.manager
    private val localCache = dependencies.localCache

    private var selectedPackage: InstalledPackage? = null

    private val _progressState = MutableStateFlow<ProgressDialogState?>(null)
    val progressState: StateFlow<ProgressDialogState?> = _progressState

    data class ModEntry(
        val id: String,
        val name: String,
        val description: String,
        val iconPath: String?
    )

    private val _mods = MutableStateFlow(emptyList<ModEntry>())
    val mods: StateFlow<List<ModEntry>> = _mods

    private var map: Map<ModEntry, InstalledMod> = emptyMap()
    val newEnabledMods = MutableStateFlow<Set<ModEntry>>(emptySet())

    // TODO: 2021/3/12 考虑细分错误类型
    val errors = MutableStateFlow<List<String>>(emptyList())

    sealed class State {
        object Loading : State()
        object PackageNotSelected : State()
        object OK : State()
        class Error(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.emit(State.Loading)
            val pkg = localCache.getSelectedPackageUUID()?.let { uuid ->
                manager.getInstalledPackage(uuid)
            }
            if (pkg != null) {
                val mods = try {
                    pkg.getMods()
                } catch (e: Exception) {
                    _state.emit(State.Error("获取模组列表出错"))
                    return@launch
                }

                val enabled = mutableSetOf<ModEntry>()
                val result = mutableListOf<ModEntry>()
                val mMap = mutableMapOf<ModEntry, InstalledMod>()

                val exceptions = mutableListOf<String>()

                mods.forEach { mod ->
                    try {
                        val modInfo = mod.getModInfo()
                        val entry = ModEntry(
                            mod.modDir.absolutePath,
                            modInfo.name,
                            modInfo.description,
                            mod.iconFile?.absolutePath
                        )
                        result.add(entry)
                        mMap[entry] = mod
                        if (mod.isEnabled()) {
                            enabled.add(entry)
                        }
                    } catch (e: Exception) {
                        exceptions.add("${mod.modDir.path}: ${e.message ?: "未知错误"}")
                        Log.e(TAG, "Mod 解析错误", e)
                    }
                }
                selectedPackage = pkg
                _mods.emit(result)
                newEnabledMods.emit(enabled)
                map = mMap
                _state.emit(State.OK)
                errors.emit(exceptions)
            } else {
                _state.emit(State.PackageNotSelected)
            }
        }
    }

    fun enableMod(mod: ModEntry) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { map[mod]?.enable() }
            } catch (e: Exception) {
                Log.e(TAG, "enableMod: Error", e)
                return@launch
            }
            newEnabledMods.emit(newEnabledMods.value.toMutableSet().also { it.add(mod) })
        }
    }

    fun disableMod(mod: ModEntry) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { map[mod]?.disable() }
            } catch (e: Exception) {
                Log.e(TAG, "disableMod: Error", e)
                return@launch
            }
            newEnabledMods.emit(newEnabledMods.value.toMutableSet().also { it.remove(mod) })
        }
    }

    fun deleteMod(mod: ModEntry) {
        viewModelScope.launch {
            _progressState.value = ProgressDialogState.Loading("正在删除")
            try {
                withContext(Dispatchers.IO) { map[mod]?.delete() }
            } catch (e: Exception) {
                Log.e(TAG, "deleteMod: Error", e)
                return@launch
            }
            load()
            _progressState.value = ProgressDialogState.Finished("删除成功")
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            _progressState.emit(null)
        }
    }

    fun fileSelected(path: String) {
        val pkg = selectedPackage
        viewModelScope.launch {
            if (pkg != null) {
                _progressState.emit(ProgressDialogState.Loading("正在安装"))
                val zipMod = try {
                    withContext(Dispatchers.IO) { ZipMod.fromFile(File(path)) }
                } catch (e: Exception) {
                    _progressState.emit(ProgressDialogState.Failed("安装失败", "您选择的文件可能不是一个正确的模组"))
                    return@launch
                }
                try {
                    withContext(Dispatchers.IO) { pkg.installMod(zipMod) }
                } catch (e: Exception) {
                    // TODO: 2021/3/9 更详细的错误分类
                    _progressState.emit(ProgressDialogState.Failed("安装失败", "安装过程中出现错误"))
                    return@launch
                }
                _progressState.value = ProgressDialogState.Finished("安装完成")
            } else {
                _progressState.emit(ProgressDialogState.Failed("您还没有选择分包", "您还没有安装分包"))
            }
        }
    }
}