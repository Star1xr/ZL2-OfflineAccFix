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

package com.star1xr.treelauncher.game.version.mod.update

import android.content.Context
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.coroutine.Task
import com.star1xr.treelauncher.coroutine.TaskFlowExecutor
import com.star1xr.treelauncher.coroutine.TitledTask
import com.star1xr.treelauncher.coroutine.addTask
import com.star1xr.treelauncher.coroutine.buildPhase
import com.star1xr.treelauncher.game.addons.modloader.ModLoader
import com.star1xr.treelauncher.game.download.assets.platform.PlatformVersion
import com.star1xr.treelauncher.game.download.assets.platform.getProjectByVersion
import com.star1xr.treelauncher.game.version.mod.ModProject
import com.star1xr.treelauncher.game.version.mod.RemoteMod
import com.star1xr.treelauncher.path.PathManager
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lInfo
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * 全自动模组检查更新，自动检查传入的模组列表，检查并获取模组最新版本，匹配现有MC版本、现有模组加载器
 * @param mods                  需要检查并更新的模组列表
 * @param modsDir               当前模组文件夹
 * @param minecraft             MC主版本号，用于版本匹配
 * @param modLoader             模组加载器信息，用于版本匹配
 * @param waitForUserConfirm    等待用户确认更新模组的信息
 *                              如果用户觉得没有问题，须返回`true`；否则返回`false`，安装会取消
 */
class ModUpdater(
    private val context: Context,
    private val mods: List<RemoteMod>,
    private val modsDir: File,
    private val minecraft: String,
    private val modLoader: ModLoader,
    scope: CoroutineScope,
    private val waitForUserConfirm: suspend (Map<ModData, PlatformVersion>) -> Boolean
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 需要检查新版本的模组列表
     */
    val dataList: MutableList<ModData> = mutableListOf()

    /**
     * 需要更新的模组列表
     */
    val allModsUpdate: MutableMap<ModData, PlatformVersion> = mutableMapOf()

    /**
     * 开始更新所有已选择的模组
     * @param isRunning 正在运行中，拒绝此次更新请求时
     * @param onUpdated 已成功更新所有模组
     * @param onNoModUpdates 没有模组需要被更新时（所有选择的模组都是最新版）
     * @param onCancelled 更新任务被取消时
     * @param onError 更新模组时遇到错误
     */
    fun updateAll(
        isRunning: () -> Unit = {},
        onUpdated: () -> Unit,
        onNoModUpdates: () -> Unit,
        onCancelled: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在更新中，阻止这次更新请求
            isRunning()
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases()
                taskExecutor.addPhases(tasks)
            },
            onComplete = onUpdated,
            onError = { th ->
                if (th is ModUpdateCancelledException) {
                    //用户已取消本次更新
                    onCancelled()
                    return@executePhasesAsync
                }
                if (th is NoModUpdatesAvailableException) {
                    //所有模组都是最新版本，不需要更新
                    onNoModUpdates()
                    return@executePhasesAsync
                }
                onError(th)
            }
        )
    }

    private suspend fun getTaskPhases() = withContext(Dispatchers.IO) {
        dataList.clear()
        allModsUpdate.clear()
        val tempModUpdaterDir = PathManager.DIR_CACHE_MOD_UPDATER

        listOf(
            buildPhase {
                //清理缓存
                addTask(
                    id = "ModUpdater.ClearTemp",
                    title = context.getString(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModUpdaterDir()
                    //清理后，重新创建缓存目录
                    tempModUpdaterDir.createDirAndLog()
                }

                //过滤模组数据
                addTask(
                    id = "ModUpdater.Filter",
                    title = context.getString(R.string.mods_update_task_filter),
                    icon = R.drawable.ic_filter_alt_outlined
                ) { task ->
                    val totalSize = mods.size
                    val needLoad = mutableListOf<RemoteMod>()
                    val readyData = mutableListOf<ModData>()

                    mods.forEachIndexed { index, mod ->
                        val file = mod.localMod.file

                        task.updateProgress(
                            percentage = (index + 1f) / totalSize,
                            message = R.string.empty_holder,
                            file.nameWithoutExtension
                        )

                        val modFile = mod.remoteFile
                        val project = mod.projectInfo

                        if (modFile != null && project != null) {
                            readyData += ModData(
                                file = file,
                                modFile = modFile,
                                project = project,
                                mcMod = mod.mcMod
                            )
                        } else {
                            needLoad += mod
                        }
                    }

                    val loadedData = if (needLoad.isNotEmpty()) {
                        needLoad.map { mod ->
                            async(Dispatchers.IO) {
                                loadModData(task, mod)
                            }
                        }.awaitAll()
                            .filterNotNull()
                            .also {
                                task.updateMessage(null)
                            }
                    } else {
                        emptyList()
                    }

                    dataList.addAll(readyData)
                    dataList.addAll(loadedData)
                }

                // 检查更新
                addTask(
                    id = "ModUpdater.CheckUpdate",
                    title = context.getString(R.string.mods_update_task_check_update),
                    icon = R.drawable.ic_list_alt_check_outlined
                ) { task ->
                    // 最大并发数为 5
                    val semaphore = Semaphore(5)
                    val completedCount = AtomicInteger(0)
                    val totalSize = dataList.size

                    val updateResults = dataList.map { data ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                // 检查更新
                                val version = data.checkUpdate(minecraft, modLoader)

                                // 线程安全地更新进度条：以完成的数量来计算进度
                                val currentCompleted = completedCount.incrementAndGet()
                                task.updateProgress(
                                    percentage = currentCompleted.toFloat() / totalSize,
                                    message = R.string.empty_holder,
                                    data.project.title
                                )

                                // 如果有新版本，返回键值对；否则返回 null
                                if (version != null) data to version else null
                            }
                        }
                    }.awaitAll().filterNotNull() // 等待所有任务完成，并过滤掉不需要更新的 null 结果

                    updateResults.forEach { (data, version) ->
                        allModsUpdate[data] = version
                    }

                    if (allModsUpdate.isEmpty()) {
                        //所有模组都是最新版本，无需更新
                        throw NoModUpdatesAvailableException()
                    }
                }

                //等待用户确认模组更新
                addTask(
                    id = "ModUpdater.WaitForUser",
                    title = context.getString(R.string.mods_update_task_wait_for_user),
                    icon = R.drawable.ic_schedule_outlined
                ) {
                    if (!waitForUserConfirm(allModsUpdate.toMap())) {
                        //用户取消了更新，这里抛出取消异常，结束全部任务
                        throw ModUpdateCancelledException()
                    }
                }

                //下载新版本模组
                addTask(
                    id = "ModUpdater.UpdateMod",
                    title = context.getString(R.string.mods_update_task_download)
                ) { task ->
                    val mods = allModsUpdate.values.toList()
                    val updater = ModVersionUpdater(mods, tempModUpdaterDir)
                    updater.startDownload(task)
                }

                //替换模组文件
                addTask(
                    id = " ModUpdater.ReplaceMod",
                    title = context.getString(R.string.mods_update_task_replace),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    val totalCount = allModsUpdate.entries.size
                    allModsUpdate.entries.forEachIndexed { index, entry ->
                        val oldMod = entry.key
                        val newVersion = entry.value

                        val oldFile = oldMod.file
                        val newFileName = newVersion.platformFileName()
                        val cacheFile = File(tempModUpdaterDir, newFileName)

                        task.updateProgress(
                            percentage = (index + 1).toFloat() / totalCount,
                            message = R.string.empty_holder,
                            oldFile.name
                        )

                        //确保所有文件都有效
                        if (modsDir.exists() && oldFile.exists() && cacheFile.exists()) {
                            FileUtils.deleteQuietly(oldFile)
                            val newFile = File(modsDir, newFileName)
                            cacheFile.copyTo(target = newFile, overwrite = true)
                        }
                    }
                }

                //清理缓存
                addTask(
                    id = "ModUpdater.ClearTempEnds",
                    title = context.getString(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) {
                    clearTempModUpdaterDir()
                }
            }
        )
    }

    private suspend fun loadModData(
        task: Task,
        mod: RemoteMod
    ): ModData? {
        val file = mod.localMod.file

        val modFile = mod.remoteFile ?: runCatching {
            task.updateMessage(R.string.mods_update_task_loading, file.name)
            mod.loadRemoteFile()
        }.onFailure {
            lWarning("Failed to load remote mod version", it)
        }.getOrNull() ?: return null

        val project = mod.projectInfo ?: runCatching {
            task.updateMessage(R.string.mods_update_task_loading, file.name)

            val project = getProjectByVersion(modFile.projectId, modFile.platform)
            ModProject(
                id = project.platformId(),
                platform = project.platform(),
                iconUrl = project.platformIconUrl(),
                title = project.platformTitle(),
                slug = project.platformSlug()
            )
        }.onFailure {
            lWarning("Failed to load remote project", it)
        }.getOrNull() ?: return null

        return ModData(
            file = file,
            modFile = modFile,
            project = project,
            mcMod = mod.mcMod
        )
    }

    fun cancel() {
        taskExecutor.cancel()
    }

    /**
     * 清理临时模组更新缓存目录
     */
    private suspend fun clearTempModUpdaterDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_MOD_UPDATER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            lInfo("Temporary mod updater directory cleared.")
        }
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        lDebug("Created directory: $this")
        return this
    }
}