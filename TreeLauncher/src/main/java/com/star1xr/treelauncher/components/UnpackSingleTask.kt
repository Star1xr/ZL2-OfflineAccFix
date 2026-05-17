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

package com.star1xr.treelauncher.components

import android.content.Context
import android.content.res.AssetManager
import com.star1xr.treelauncher.context.copyAssetFile
import com.star1xr.treelauncher.utils.file.readString
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.logging.Logger.lInfo
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

abstract class UnpackSingleTask(
    val context: Context,
    val rootDir: File,
    val assetsDirName: String,
    val fileDirName: String,
) : AbstractUnpackTask() {
    private lateinit var am: AssetManager
    private lateinit var versionFile: File
    private lateinit var input: InputStream
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            am = context.assets
            versionFile = File("$rootDir/$fileDirName/version")
            input = am.open("$assetsDirName/$fileDirName/version")
        }.onFailure { e ->
            lWarning("Failed to init asset version. assetsPath=$assetsDirName/$fileDirName/version", e)
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun checkState(): InstallableItem.State {
        if (isCheckFailed) return InstallableItem.State.NOT_EXISTS

        return if (!versionFile.exists()) {
            requestEmptyParentDir(versionFile)
            lInfo("$fileDirName: Pack was installed manually, or does not exist...")
            InstallableItem.State.NOT_STARTED
        } else {
            runCatching {
                val fis = FileInputStream(versionFile)
                val release1 = input.readString()
                val release2 = fis.readString()
                if (release1 != release2) {
                    requestEmptyParentDir(versionFile)
                    InstallableItem.State.PENDING
                } else {
                    lInfo("$fileDirName: Pack is up-to-date with the launcher, continuing...")
                    InstallableItem.State.FINISHED
                }
            }.onFailure { e ->
                lError("An exception occurred while detecting the assets resource.", e)
            }.getOrElse {
                //检查失败，要求重新进行安装
                InstallableItem.State.NOT_STARTED
            }
        }
    }

    override suspend fun run() {
        val dir = File(rootDir, fileDirName)
        FileUtils.deleteDirectory(dir)

        val fileList = am.list("$assetsDirName/$fileDirName")!!
            .map { fileName -> File(dir, fileName) }
        val versionFile = fileList.find { it.name == "version" }!!

        for (file in fileList) {
            context.copyAssetFile(
                fileName = "$assetsDirName/$fileDirName/${file.name}",
                output = file,
                overwrite = true
            )
            moreProgress(file)
        }

        //最后才写入version
        context.copyAssetFile(
            fileName = "$assetsDirName/$fileDirName/version",
            output = versionFile,
            overwrite = true
        )
    }

    /**
     * 执行更多操作
     */
    open suspend fun moreProgress(file: File) {}

    private fun requestEmptyParentDir(file: File) {
        file.parentFile!!.apply {
            if (exists() and isDirectory) {
                FileUtils.deleteDirectory(this)
            }
            mkdirs()
        }
    }
}