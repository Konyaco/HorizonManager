package org.wvt.horizonmgr.service.hzpack

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import org.wvt.horizonmgr.service.CoroutineZip
import org.wvt.horizonmgr.service.utils.translateToValidFile
import java.io.File
import java.util.*

class HorizonManager constructor(val horizonDir: File) {
    private val packDir = horizonDir.resolve("packs")

    /**
     * 从本机中获取已安装的 Package
     * 该方法仅把文件夹包装成 [InstalledPackage]，不加以验证，不会抛出 IO 以外的异常
     */
    suspend fun getInstalledPackages(): List<InstalledPackage> = withContext(Dispatchers.IO) {
        val subDirs = packDir.listFiles() ?: emptyArray()
        val packages = mutableListOf<InstalledPackage>()

        for (dir in subDirs) {
            val pkg = try {
                InstalledPackage(dir)
            } catch (e: IllegalStateException) {
                continue
            }
            packages.add(pkg)
        }

        return@withContext packages
    }

    /**
     * 从本机中获取指定的分包
     * 由于要使用 [InstalledPackage::getInstallationInfo()] 方法，该方法会对分包进行格式验证
     * 如果格式不正确则返回 null，只会抛出 IO 导致的异常
     */
    suspend fun getInstalledPackage(installUUID: String): InstalledPackage? =
        withContext(Dispatchers.IO) {
            getInstalledPackages().find {
                try {
                    return@find it.getInstallationInfo().internalId == installUUID
                } catch (e: SerializationException) {
                    return@find false
                }
            }
        }

    /**
     * 将 ZipPackage 安装到本机
     * 安装步骤：
     * 在 packDir 中创建文件夹
     *  创建 .installation_started
     *  将分包压缩包解压到文件夹
     *  创建 .installation_info
     *  将 graphics zip 改名为 .cached_graphics 并复制到文件夹
     *  创建 .installation_complete
     */
    suspend fun installPackage(
        zipPackage: ZipPackage,
        graphicsZip: File?
    ): InstalledPackage = withContext(Dispatchers.IO) {
        val manifest = zipPackage.getManifest()
        if (packDir.exists().not()) packDir.mkdirs()
        // 根据指定分包名称生成目录
        val outDir =
            packDir.resolve(manifest.pack).translateToValidFile().also { it.mkdirs() }
        // 创建开始安装的文件标志
        outDir.resolve(".installation_started").createNewFile()
        // 直接解压到 packs 文件夹
        CoroutineZip.unzip(zipPackage.zipFile, outDir).await()
        // TODO: 2021/2/20 测试如果没有 graphics 是否能运行
        // 预览图压缩包直接复制改名
        graphicsZip?.copyTo(outDir.resolve(".cached_graphics"))
        val uuid = UUID.randomUUID().toString().toLowerCase(Locale.ROOT)
        outDir.resolve(".installation_info").outputStream().writer().use {
            val jsonStr = InstallationInfo(
                packageId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT),
                internalId = uuid,
                timeStamp = Date().time,
                customName = manifest.pack
            ).toJson()
            it.write(jsonStr)
        }
        outDir.resolve(".installation_complete").createNewFile()
        return@withContext InstalledPackage(outDir)
    }
}