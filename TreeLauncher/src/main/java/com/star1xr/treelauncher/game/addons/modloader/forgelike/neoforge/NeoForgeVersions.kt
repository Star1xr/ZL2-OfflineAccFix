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

package com.star1xr.treelauncher.game.addons.modloader.forgelike.neoforge

import com.star1xr.treelauncher.game.addons.mirror.MirrorSource
import com.star1xr.treelauncher.game.addons.mirror.SourceType
import com.star1xr.treelauncher.game.addons.mirror.runMirrorable
import com.star1xr.treelauncher.game.addons.modloader.forgelike.neoforge.models.BMCLAPIMaven
import com.star1xr.treelauncher.game.addons.modloader.forgelike.neoforge.models.NeoForgedMaven
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.MirrorSourceType
import com.star1xr.treelauncher.utils.isChinaMainland
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import com.star1xr.treelauncher.utils.network.httpGetJson
import com.star1xr.treelauncher.utils.network.withRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NeoForgeVersions {
    private const val TAG = "NeoForgeVersions"
    private var cacheResult: List<NeoForgeVersion>? = null

    /**
     * 获取 NeoForge 版本列表
     * [Reference PCL2](https://github.com/Meloong-Git/PCL/blob/28ef67e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L811-L830)
     */
    suspend fun fetchNeoForgeList(
        force: Boolean = false,
        gameVersion: String
    ): List<NeoForgeVersion>? = withContext(Dispatchers.Default) {
        if (!force) cacheResult?.let {
            return@withContext it.outputVersionList(gameVersion)
        }

        if (isChinaMainland()) {
            runMirrorable(
                when (AllSettings.fetchModLoaderSource.getValue()) {
                    MirrorSourceType.OFFICIAL_FIRST -> listOf(
                        fetchListWithOfficial(5),
                        fetchListWithBMCLAPI(5 + 30)
                    )
                    MirrorSourceType.MIRROR_FIRST -> listOf(
                        fetchListWithBMCLAPI(30),
                        fetchListWithOfficial(30 + 60)
                    )
                }
            )
        } else {
            fetchListWithOfficial()
        }?.also {
            cacheResult = it
        }?.outputVersionList(gameVersion)
    }

    private fun List<NeoForgeVersion>.outputVersionList(
        gameVersion: String
    ): List<NeoForgeVersion> {
        return this.filter {
            it.inherit == gameVersion
        }.sortedWith { o1, o2 ->
            o2.forgeBuildVersion.compareTo(o1.forgeBuildVersion)
        }
    }

    /**
     * 在官方源获取版本列表
     */
    private fun fetchListWithOfficial(delayMillis: Long): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.OFFICIAL
    ) {
        fetchListWithOfficial()
    }

    private suspend fun fetchListWithOfficial() = withContext(Dispatchers.IO) {
        processVersionList {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 在BMCL API源获取版本列表
     */
    private fun fetchListWithBMCLAPI(delayMillis: Long): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.BMCLAPI
    ) {
        processVersionList {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGetJson<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 统一处理任务，处理异常、排序
     */
    private suspend fun processVersionList(
        block: suspend () -> List<NeoForgeVersion>
    ): List<NeoForgeVersion>? = withContext(Dispatchers.IO) {
        try {
            block()
                .sortedByDescending { it.forgeBuildVersion }
                .toList()
        } catch (_: CancellationException) {
            lDebug("Client cancelled.")
            null
        } catch (e: Exception) {
            lWarning("Failed to fetch neoforge list!", e)
            throw e
        }
    }

    /**
     * 获取 NeoForge 对应版本的下载链接
     */
    fun getDownloadUrl(version: NeoForgeVersion) = "${version.baseUrl}-installer.jar"
}