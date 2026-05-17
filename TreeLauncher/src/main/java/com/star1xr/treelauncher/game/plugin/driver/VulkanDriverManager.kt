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

package com.star1xr.treelauncher.game.plugin.driver

import com.star1xr.treelauncher.path.GLOBAL_CLIENT
import com.star1xr.treelauncher.path.PathManager
import com.star1xr.treelauncher.utils.logging.Logger.lError
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String? = null,
    val assets: List<GitHubAsset>
)

@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

object VulkanDriverManager {
    private const val REPO_RELEASES_URL = "https://api.github.com/repos/StevenMXZ/Adreno-Tools-Drivers/releases"

    suspend fun fetchReleases(): List<GitHubRelease> = withContext(Dispatchers.IO) {
        try {
            GLOBAL_CLIENT.get(REPO_RELEASES_URL).body()
        } catch (e: Exception) {
            lError("Failed to fetch Vulkan driver releases", e)
            emptyList()
        }
    }

    fun getDriversHome(): File = PathManager.DIR_DRIVERS

    fun deleteDriver(driverName: String): Boolean {
        val dir = File(getDriversHome(), driverName)
        return if (dir.exists()) {
            dir.deleteRecursively()
        } else false
    }

    fun extractZip(zipFile: File, targetDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val targetFile = File(targetDir, entry.name)
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
}
