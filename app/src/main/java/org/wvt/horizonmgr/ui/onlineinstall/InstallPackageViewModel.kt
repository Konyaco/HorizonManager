package org.wvt.horizonmgr.ui.onlineinstall

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.hzpack.PackageManifest
import org.wvt.horizonmgr.service.hzpack.ZipPackage
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.pack.OfficialCDNPackage

private const val TAG = "InstallPackageVM"

class InstallPackageViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val packRepository = dependencies.packRepository
    private val downloader = dependencies.packageDownloader
    private val mgr = dependencies.manager

    sealed class State {
        object Loading : State()
        object Succeed : State()
        class Error(val message: String) : State()
    }

    val state = MutableStateFlow<State>(State.Loading)
    val packages = MutableStateFlow<List<ChoosePackageItem>>(emptyList())

    var totalProgress = MutableStateFlow<Float>(0f)
    val downloadState = MutableStateFlow<StepState>(StepState.Waiting)
    val installState = MutableStateFlow<StepState>(StepState.Waiting)

    private var packs = emptyList<OfficialCDNPackage>()
    private var selectedPackage: OfficialCDNPackage? = null
    private var customName: String? = null

    fun getPackages() {
        viewModelScope.launch {
            packs = try {
                packRepository.getAllPackages()
            } catch (e: NetworkException) {
                state.emit(State.Error("网络错误，请稍后再试"))
                return@launch
            } catch (e: Exception) {
                Log.d(TAG, "获取分包列表失败", e)
                state.emit(State.Error("未知错误，请稍后再试"))
                return@launch
            }

            val mappedPacks = packs.map {
                val manifestStr = it.getManifest()
                val manifest = PackageManifest.fromJson(manifestStr)
                ChoosePackageItem(
                    it.uuid,
                    manifest.pack,
                    manifest.packVersion,
                    it.isSuggested
                )
            }

            packages.emit(mappedPacks)
            state.emit(State.Succeed)
        }
    }

    fun selectPackage(uuid: String) {
        selectedPackage = packs.find { it.uuid == uuid }
    }

    fun setCustomName(name: String) {
        customName = name
    }

    fun startInstall() {
        viewModelScope.launch {
            selectedPackage?.let { pack ->
                val downloadProgress = mutableStateOf(0f)
                downloadState.emit(StepState.Running(downloadProgress))
                val task = downloader.download(pack)
                task.progressChannel().receiveAsFlow().conflate().collect {
                    downloadProgress.value = it
                    totalProgress.emit(it / 2)
                    delay(200)
                }
                val result = try {
                    task.await()
                } catch (e: Exception) {
                    Log.e(TAG, "下载分包失败", e)
                    // TODO: 2021/2/20 添加显示
                    downloadState.emit(StepState.Error(e))
                    return@launch
                }
                delay(500)
                downloadState.emit(StepState.Complete)
                installState.emit(StepState.Running(mutableStateOf(0f)))
                try {
                    mgr.installPackage(ZipPackage.parse(result.packageZipFile), graphicsZip = result.graphicsFile)
                } catch (e: Exception) {
                    Log.e(TAG, "安装分包失败", e)
                    installState.emit(StepState.Error(e))
                    return@launch
                }

                delay(500)
                totalProgress.emit(1f)
                installState.emit(StepState.Complete)
            }
        }
    }

    fun cancelInstall() {
        // TODO: 2021/2/20 添加取消安装功能
    }
}