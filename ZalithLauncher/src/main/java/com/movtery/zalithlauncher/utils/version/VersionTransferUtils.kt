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

package com.movtery.zalithlauncher.utils.version

import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.FileInputStream
import com.movtery.zalithlauncher.utils.file.ensureDirectorySilently
import java.util.zip.ZipFile

object VersionTransferUtils {
    private fun getExportDir(): File {
        val dir = File("/storage/emulated/0/zalithplus/versionexport")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun exportVersion(version: Version): File? = withContext(Dispatchers.IO) {
        try {
            val versionName = version.getVersionName()
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportFile = File(getExportDir(), "${versionName}_${dateStr}.zip")

            ZipOutputStream(FileOutputStream(exportFile)).use { zipOut ->
                // 1. Export version.json (containing config)
                val config = version.getVersionConfig()
                val json = GSON.toJson(config)
                
                zipOut.putNextEntry(ZipEntry("version.json"))
                zipOut.write(json.toByteArray())
                zipOut.closeEntry()

                // 2. Export version/ directory
                val versionDir = version.getVersionPath()
                if (versionDir.exists() && versionDir.isDirectory) {
                    versionDir.walkTopDown().forEach { file ->
                        val relativePath = file.relativeTo(versionDir).path.replace("\\", "/")
                        if (relativePath.isNotEmpty()) {
                            val entryName = "version/$relativePath"
                            if (file.isDirectory) {
                                zipOut.putNextEntry(ZipEntry("$entryName/"))
                                zipOut.closeEntry()
                            } else {
                                zipOut.putNextEntry(ZipEntry(entryName))
                                file.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            }
                        }
                    }
                }
            }
            lInfo("Version exported to ${exportFile.absolutePath}")
            exportFile
        } catch (e: Exception) {
            lError("Failed to export version", e)
            null
        }
    }

    suspend fun importVersion(zipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipFile(zipFile).use { zip ->
                val configEntry = zip.getEntry("version.json") ?: return@withContext false
                val json = zip.getInputStream(configEntry).bufferedReader().readText()
                val config = GSON.fromJson(json, VersionConfig::class.java)
                
                // We need a name for the version. Let's use the one from the zip if possible or the filename
                var versionName = zipFile.nameWithoutExtension.substringBeforeLast("_")
                if (versionName.isEmpty()) versionName = zipFile.nameWithoutExtension

                // Ensure unique name
                var finalName = versionName
                var i = 1
                while (VersionsManager.getVersion(finalName) != null) {
                    finalName = "${versionName}_$i"
                    i++
                }

                val targetVersionDir = File(getVersionsHome(), finalName)
                targetVersionDir.mkdirs()

                // Extract version/ content
                val prefix = "version/"
                zip.entries().asSequence().forEach { entry ->
                    if (entry.name.startsWith(prefix)) {
                        val relativePath = entry.name.removePrefix(prefix)
                        if (relativePath.isNotEmpty()) {
                            val targetFile = File(targetVersionDir, relativePath)
                            if (entry.isDirectory) {
                                targetFile.mkdirs()
                            } else {
                                targetFile.parentFile?.mkdirs()
                                zip.getInputStream(entry).use { input ->
                                    FileOutputStream(targetFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                        }
                    }
                }

                // Update config path and save
                config.setVersionPath(targetVersionDir)
                config.save()
                
                lInfo("Version imported to ${targetVersionDir.absolutePath}")
                true
            }
        } catch (e: Exception) {
            lError("Failed to import version", e)
            false
        }
    }
}
