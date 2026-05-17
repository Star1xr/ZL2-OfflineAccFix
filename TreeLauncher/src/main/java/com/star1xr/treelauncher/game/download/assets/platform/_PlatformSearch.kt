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

package com.star1xr.treelauncher.game.download.assets.platform

import android.util.Log
import com.star1xr.treelauncher.game.download.assets.mapExceptionToMessage
import com.star1xr.treelauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.star1xr.treelauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.star1xr.treelauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.star1xr.treelauncher.game.download.assets.platform.modrinth.ModrinthSearcher
import com.star1xr.treelauncher.game.download.assets.utils.localizedModSearchKeywords
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.MirrorSourceType
import com.star1xr.treelauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.star1xr.treelauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.star1xr.treelauncher.utils.isChinaMainland
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import com.star1xr.treelauncher.utils.network.isInterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private val modrinthSearcher = ModrinthSearcher()
private val mirrorModrinthSearcher = ModrinthSearcher(
    api = MCIM_MODRINTH_API,
    source = "MCIM Modrinth"
)

private val curseForgeSearcher = CurseForgeSearcher()
private val mirrorCurseForgeSearcher = CurseForgeSearcher(
    api = MCIM_CURSEFORGE_API,
    apiKey = null, //不向镜像源提供 api key
    source = "MCIM CurseForge"
)

/**
 * 对资源平台搜索启用镜像源机制进行操作
 */
suspend fun <E: AbstractPlatformSearcher, T> mirroredPlatformSearcher(
    searchers: List<E>,
    printLog: Boolean = true,
    block: suspend (E) -> T
): T {
    require(searchers.isNotEmpty()) { "Searcher list must not be empty." }

    val errors = mutableListOf<Exception>()
    var lastException: Exception? = null

    for (searcher in searchers) {
        try {
            if (printLog) {
                lDebug("Starting to attempt to perform the operation on source: {${searcher.source}}")
            }
            return block(searcher)
        } catch (e: Exception) {
            Log.w("PlatformSearcher", "Failed to perform the operation on source: {${searcher.source}}", e)
            lastException = e

            if (e.isInterruptedIOException()) {
                throw e
            } else if (e is FileNotFoundException) {
                errors.add(e)
                break
            } else {
                errors.add(e)
            }
        }
    }

    if (printLog) {
        lWarning(
            msg = "An error occurred during this search.",
            t = IOException("All sources have failed to attempt", lastException).apply {
                errors.forEachIndexed { i, e ->
                    addSuppressed(Exception("Mirror error #${i + 1}: ${e.message}"))
                }
            }
        )
    }
    throw lastException ?: IllegalStateException("Should not have executed to this stage.")
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredCurseForgeSource(
    enabledMirror: Boolean = isChinaMainland()
): List<CurseForgeSearcher> {
    val source = AllSettings.assetSearchSource.getValue()
    val mirrorSource = mirrorCurseForgeSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorSourceType.OFFICIAL_FIRST ->
            listOfNotNull(curseForgeSearcher, mirrorSource)
        MirrorSourceType.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, curseForgeSearcher)
    }
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredModrinthSource(
    enabledMirror: Boolean = isChinaMainland()
): List<ModrinthSearcher> {
    val source = AllSettings.assetSearchSource.getValue()
    val mirrorSource = mirrorModrinthSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorSourceType.OFFICIAL_FIRST ->
            listOfNotNull(modrinthSearcher, mirrorSource)
        MirrorSourceType.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, modrinthSearcher)
    }
}

suspend fun searchAssets(
    searchPlatform: Platform,
    searchFilter: PlatformSearchFilter,
    platformClasses: PlatformClasses,
    onSuccess: suspend (PlatformSearchResult) -> Unit,
    onError: (SearchAssetsState.Error) -> Unit
) {
    runCatching {
        val (containsChinese, englishKeywords) = searchFilter.searchName.localizedModSearchKeywords(platformClasses)
        val query = englishKeywords?.joinToString(" ") ?: searchFilter.searchName
        val result = when (searchPlatform) {
            Platform.CURSEFORGE -> mirroredPlatformSearcher(
                searchers = mirroredCurseForgeSource()
            ) { searcher ->
                searcher.searchAssets(
                    query = query,
                    searchFilter = searchFilter,
                    platformClasses = platformClasses
                )
            }
            Platform.MODRINTH -> mirroredPlatformSearcher(
                searchers = mirroredModrinthSource()
            ) { searcher ->
                searcher.searchAssets(
                    query = query,
                    searchFilter = searchFilter,
                    platformClasses = platformClasses
                )
            }
        }
        onSuccess(
            if (containsChinese) result.processChineseSearchResults(searchFilter.searchName, platformClasses)
            else result
        )
    }.onFailure { e ->
        if (e !is CancellationException) {
            lError("An exception occurred while searching for assets.", e)
            val pair = mapExceptionToMessage(e)
            val state = SearchAssetsState.Error(pair.first, pair.second)
            onError(state)
        } else {
            lWarning("The search task has been cancelled.")
        }
    }
}

suspend fun getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
) = when (platform) {
    Platform.CURSEFORGE -> mirroredPlatformSearcher(
        searchers = mirroredCurseForgeSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
    Platform.MODRINTH -> mirroredPlatformSearcher(
        searchers = mirroredModrinthSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
}

suspend fun <E> getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
    onSuccess: suspend (List<PlatformVersion>) -> Unit,
    onError: (DownloadAssetsState<List<E>>) -> Unit
) {
    runCatching {
        val result = getVersions(projectID, platform, pageCallback)
        onSuccess(result)
    }.onFailure { e ->
        if (e !is CancellationException) {
            lError("An exception occurred while retrieving the project version.", e)
            val pair = mapExceptionToMessage(e)
            val state = DownloadAssetsState.Error<List<E>>(pair.first, pair.second)
            onError(state)
        } else {
            lWarning("The version retrieval task has been cancelled.")
        }
    }
}

suspend fun <E> getProject(
    projectID: String,
    platform: Platform,
    onSuccess: (PlatformProject) -> Unit,
    onError: (DownloadAssetsState<E>, Throwable) -> Unit
) {
    runCatching {
        when (platform) {
            Platform.CURSEFORGE -> mirroredPlatformSearcher(
                searchers = mirroredCurseForgeSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
            Platform.MODRINTH -> mirroredPlatformSearcher(
                searchers = mirroredModrinthSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
        }
    }.fold(
        onSuccess = onSuccess,
        onFailure = { e ->
            if (e !is CancellationException) {
                lError("An exception occurred while retrieving project information.", e)
                val pair = mapExceptionToMessage(e)
                val state = DownloadAssetsState.Error<E>(pair.first, pair.second)
                onError(state, e)
            } else {
                lWarning("The project retrieval task has been cancelled.")
            }
        }
    )
}

suspend fun getProjectByVersion(
    projectId: String,
    platform: Platform,
    printLog: Boolean = true
): PlatformProject = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> mirroredPlatformSearcher(
            searchers = mirroredModrinthSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
        Platform.CURSEFORGE -> mirroredPlatformSearcher(
            searchers = mirroredCurseForgeSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = coroutineScope {
    val modrinthDeferred = async(Dispatchers.IO) {
        runCatching {
            mirroredPlatformSearcher(
                searchers = mirroredModrinthSource(),
                printLog = false
            ) { searcher ->
                searcher.getVersionByLocalFile(file, sha1)
            }
        }.getOrNull()
    }

    val curseForgeDeferred = async(Dispatchers.IO) {
        runCatching {
            mirroredPlatformSearcher(
                searchers = mirroredCurseForgeSource(),
                printLog = false
            ) { searcher ->
                searcher.getVersionByLocalFile(file, sha1)
            }
        }.getOrNull()
    }

    val result = select {
        modrinthDeferred.onAwait { result ->
            if (result != null) {
                curseForgeDeferred.cancel()
                result
            } else {
                null
            }
        }
        curseForgeDeferred.onAwait { result ->
            if (result != null) {
                modrinthDeferred.cancel()
                result
            } else {
                null
            }
        }
    }

    result ?: run {
        if (!modrinthDeferred.isCompleted) modrinthDeferred.await()
        else if (!curseForgeDeferred.isCompleted) curseForgeDeferred.await()
        else null
    }
}