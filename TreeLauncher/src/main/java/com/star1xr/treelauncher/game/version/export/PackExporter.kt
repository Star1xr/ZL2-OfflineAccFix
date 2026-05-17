/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.star1xr.treelauncher.game.version.export

import android.content.Context
import android.net.Uri
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.context.writeLocalFile
import com.star1xr.treelauncher.coroutine.TaskFlowExecutor
import com.star1xr.treelauncher.coroutine.TitledTask
import com.star1xr.treelauncher.coroutine.addTask
import com.star1xr.treelauncher.coroutine.buildPhase
import com.star1xr.treelauncher.game.version.export.platform.CurseForgePackExporter
import com.star1xr.treelauncher.game.version.export.platform.MCBBSPackExporter
import com.star1xr.treelauncher.game.version.export.platform.ModrinthPackExporter
import com.star1xr.treelauncher.game.version.export.platform.MultiMCPackExporter
import com.star1xr.treelauncher.game.version.installed.Version
import com.star1xr.treelauncher.path.PathManager
import com.star1xr.treelauncher.utils.file.zipDirectory
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 整合包导出器
 * @param exportInfo 要导出的整合包的必要信息
 * @param scope 在有生命周期管理的scope中执行安装任务
 */
class PackExporter(
    val context: Context,
    val exportInfo: ExportInfo,
    private val scope: CoroutineScope,
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val taskFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    private val exporter: AbstractExporter = when (exportInfo.packType) {
        PackType.MCBBS -> MCBBSPackExporter()
        PackType.Modrinth -> ModrinthPackExporter()
        PackType.CurseForge -> CurseForgePackExporter()
        PackType.MultiMC -> MultiMCPackExporter()
    }

    /**
     * 开始导出整合包
     */
    fun startExport(
        outputUri: Uri,
        version: Version,
        isRunning: () -> Unit = {},
        onFinished: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            isRunning()
            return //正在运行中，拒绝导出
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases(outputUri, version)
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                onFinished()
            },
            onError = { e ->
                onError(e)
            }
        )
    }

    private suspend fun getTaskPhases(
        outputUri: Uri,
        version: Version
    ) = withContext(Dispatchers.IO) {
        val exportCachePath = PathManager.DIR_CACHE_MODPACK_EXPORTER
        val tempPath = File(exportCachePath, "temp")
        val pack = File(exportCachePath, "${exportInfo.name}.${exporter.fileSuffix}")

        listOf(
            buildPhase {
                //清除上一次导出的缓存
                addTask(
                    id = "ExportModpack.Cleanup",
                    title = context.getString(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModPackDir()
                    tempPath.createDirAndLog()
                }

                with(exporter) {
                    buildTasks(
                        context = context,
                        version = version,
                        info = exportInfo,
                        tempPath = tempPath
                    )
                }

                addTask(
                    id = "ExportModpack.Pack",
                    title = context.getString(R.string.versions_export_task_generate_pack),
                    icon = R.drawable.ic_build_outlined
                ) {
                    zipDirectory(
                        sourceDir = tempPath,
                        outputZipFile = pack,
                        preserveFileTime = false
                    )

                    context.writeLocalFile(
                        inputFile = pack,
                        outputUri = outputUri,
                        mimeType = "application/*"
                    )
                }

                addTask(
                    id = "ExportModpack.Cleanup_Finished",
                    title = context.getString(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModPackDir()
                }
            }
        )
    }

    /**
     * 清理临时整合包导出目录
     */
    private suspend fun clearTempModPackDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_MODPACK_EXPORTER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            lInfo("Temporary modpack export directory cleared.")
        }
    }

    /**
     * 取消整合包导入
     */
    fun cancel() {
        taskExecutor.cancel()
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        lDebug("Created directory: $this")
        return this
    }
}