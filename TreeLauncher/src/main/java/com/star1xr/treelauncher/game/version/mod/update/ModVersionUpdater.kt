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

import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.coroutine.Task
import com.star1xr.treelauncher.game.download.assets.platform.PlatformVersion
import com.star1xr.treelauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.star1xr.treelauncher.game.version.download.DownloadFailedException
import com.star1xr.treelauncher.utils.file.formatFileSize
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.network.downloadFromMirrorListSuspend
import com.star1xr.treelauncher.utils.network.withSpeedReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class ModVersionUpdater(
    val mods: List<PlatformVersion>,
    private val targetDir: File,
    private val maxDownloadThreads: Int = 64
) {
    //文件下载进度计数
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private val downloadFailedTasks = mutableSetOf<PlatformVersion>()

    /** 用于速率监测的已写入大小记录 */
    private val mSpeedReport = AtomicLong(0L)

    suspend fun startDownload(task: Task) {
        downloadAll(
            task = task,
            taskMessageRes = R.string.mods_update_updating
        )
        if (downloadFailedTasks.isNotEmpty()) {
            downloadedFileCount.set(0)
            downloadedFileSize.set(0L)
            downloadAll(
                task = task,
                tasks = downloadFailedTasks.toList(),
                taskMessageRes = R.string.mods_update_updating_retry
            )
        }
        if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
        //清除任务信息
        task.updateProgress(1f, null)
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<PlatformVersion> = mods,
        taskMessageRes: Int,
        totalFileCount: Int = tasks.size
    ) = withContext(Dispatchers.IO) {
        coroutineScope {
            downloadFailedTasks.clear()

            val semaphore = Semaphore(maxDownloadThreads)

            val downloadJobs = tasks.map { newVersion ->
                launch {
                    semaphore.withPermit {
                        val urls = newVersion
                            .platformDownloadUrl()
                            .mapMCIMMirrorUrls()
                        val outputFile = File(targetDir, newVersion.platformFileName())

                        runCatching {
                            downloadFromMirrorListSuspend(
                                urls = urls,
                                sha1 = newVersion.platformSha1(),
                                outputFile = outputFile
                            ) { size ->
                                downloadedFileSize.addAndGet(size)
                                mSpeedReport.addAndGet(size)
                            }
                            //下载成功
                            downloadedFileCount.incrementAndGet()
                        }.onFailure { e ->
                            if (e is CancellationException) return@onFailure
                            lError("Download failed: ${outputFile.absolutePath}, urls: ${urls.joinToString(", ")}", e)
                            downloadFailedTasks.add(newVersion)
                        }
                    }
                }
            }

            val progressJob = launch(Dispatchers.Main) {
                while (isActive) {
                    ensureActive()
                    val currentFileCount = downloadedFileCount.get()
                    task.updateProgress(
                        (currentFileCount.toFloat() / totalFileCount.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount,
                        formatFileSize(downloadedFileSize.get())
                    )
                    delay(100)
                }
            }

            try {
                withSpeedReport(
                    onTimeReport = {
                        val currentBytes = mSpeedReport.getAndSet(0L)
                        task.updateSpeed(currentBytes)
                    },
                    onClear = {
                        mSpeedReport.set(0L)
                        task.clearSpeed()
                    }
                ) {
                    downloadJobs.joinAll()
                }
            } catch (e: CancellationException) {
                downloadJobs.forEach { it.cancel("Parent cancelled", e) }
                throw e
            } finally {
                progressJob.cancel()
            }
        }
    }
}