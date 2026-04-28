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

package com.movtery.zalithlauncher.game.download.game

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.download.DownloadTask
import com.movtery.zalithlauncher.game.version.download.parseTo
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.network.withSpeedReport
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
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * 游戏支持库下载器
 */
class GameLibDownloader(
    private val downloader: BaseMinecraftDownloader,
    private val gameJson: String,
    private val maxDownloadThreads: Int = 64
) {
    //已下载文件计数器
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private var allDownloadTasks = ConcurrentLinkedQueue<DownloadTask>()
    private var downloadFailedTasks = mutableListOf<DownloadTask>()

    //判断是否已经开始下载
    private var isDownloadStarted: Boolean = false

    /** 用于速率监测的已写入大小记录 */
    private val mSpeedReport = AtomicLong(0L)

    /**
     * 计划下载所有支持库
     */
    suspend fun schedule(
        task: Task,
        targetDir: File = downloader.librariesTarget,
        updateProgress: Boolean = true
    ) {
        val gameManifest = gameJson.parseTo(GameManifest::class.java)

        if (updateProgress) {
            task.updateProgress(-1f, R.string.minecraft_download_stat_download_task)
        }

        //仅加载处理支持库
        downloader.loadLibraryDownloads(gameManifest, targetDir) { urls, hash, targetFile, size, isDownloadable ->
            scheduleDownload(urls, hash, targetFile, size, isDownloadable)
        }
    }

    /**
     * 多线程下载支持库
     */
    suspend fun download(task: Task) {
        isDownloadStarted = true
        val tasks = allDownloadTasks.toList()
        if (tasks.isNotEmpty()) {
            //使用线程池进行下载
            downloadAll(task, tasks, R.string.minecraft_download_downloading_game_files)
            if (downloadFailedTasks.isNotEmpty()) {
                downloadedFileCount.set(0)
                totalFileCount.set(downloadFailedTasks.size.toLong())
                downloadAll(task, downloadFailedTasks.toList(), R.string.minecraft_download_progress_retry_downloading_files)
            }
            if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
        }

        //清除任务信息
        task.updateProgress(1f, null)
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<DownloadTask>,
        taskMessageRes: Int
    ) = withContext(Dispatchers.IO) {
        coroutineScope {
            downloadFailedTasks.clear()

            val semaphore = Semaphore(maxDownloadThreads)

            val downloadJobs = tasks.map { downloadTask ->
                launch {
                    semaphore.withPermit {
                        downloadTask.download()
                    }
                }
            }

            val progressJob = launch(Dispatchers.Main) {
                while (isActive) {
                    ensureActive()
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(
                        (currentFileSize.toFloat() / totalFileSize.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(), //文件个数
                        formatFileSize(currentFileSize), formatFileSize(totalFileSize) //文件大小
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

    /**
     * 提交计划下载
     */
    fun scheduleDownload(urls: List<String>, sha1: String?, targetFile: File, size: Long, isDownloadable: Boolean = true) {
        if (isDownloadStarted) throw IllegalStateException("The download has already started; adding more download tasks is no longer meaningful.")

        if (allDownloadTasks.any { it.targetFile.absolutePath == targetFile.absolutePath }) return

        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(
            DownloadTask(
                urls = urls,
                verifyIntegrity = true,
                targetFile = targetFile,
                sha1 = sha1,
                isDownloadable = isDownloadable,
                onDownloadFailed = { task ->
                    downloadFailedTasks.add(task)
                },
                onFileDownloadedSize = { downloadedSize ->
                    downloadedFileSize.addAndGet(downloadedSize)
                    mSpeedReport.addAndGet(downloadedSize)
                },
                onFileDownloaded = {
                    downloadedFileCount.incrementAndGet()
                }
            )
        )
    }

    /**
     * 删除某一项下载任务
     * 在下载前可以使用
     */
    fun removeDownload(predicate: (DownloadTask) -> Boolean) {
        if (isDownloadStarted) throw IllegalStateException("The download has already started; removing download tasks is no longer meaningful.")
        allDownloadTasks.removeAll(predicate)
    }
}