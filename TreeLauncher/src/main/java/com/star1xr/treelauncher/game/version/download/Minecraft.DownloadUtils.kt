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

package com.star1xr.treelauncher.game.version.download

import com.star1xr.treelauncher.game.addons.mirror.mapBMCLMirrorUrls
import com.star1xr.treelauncher.game.versioninfo.models.GameManifest
import com.star1xr.treelauncher.game.versioninfo.models.OperatingSystem
import com.star1xr.treelauncher.utils.GSON
import com.star1xr.treelauncher.utils.file.compareSHA1
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import com.star1xr.treelauncher.utils.network.fetchStringFromUrls
import com.star1xr.treelauncher.utils.network.withRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

private const val UTILS_LOG_TAG = "Minecraft.DownloaderUtils"

fun <T> String.parseTo(classOfT: Class<T>): T {
    return runCatching {
        GSON.fromJson(this, classOfT)
    }.getOrElse { e ->
        lError("Failed to parse JSON", e)
        throw e
    }
}

suspend fun <T> downloadAndParseJson(
    targetFile: File,
    url: String,
    expectedSHA: String?,
    verifyIntegrity: Boolean,
    classOfT: Class<T>
): T {
    suspend fun downloadAndParse(): T {
        val json = withContext(Dispatchers.IO) {
            val string = withRetry(UTILS_LOG_TAG, maxRetries = 2) {
                fetchStringFromUrls(
                    url.mapBMCLMirrorUrls()
                )
            }
            if (string.isBlank()) {
                lError("Downloaded string is empty, aborting.")
                throw IllegalStateException("Downloaded string is empty.")
            }
            targetFile.writeText(string)
            string
        }
        return json.parseTo(classOfT)
    }

    if (targetFile.exists()) {
        if (!verifyIntegrity || compareSHA1(targetFile, expectedSHA)) {
            return runCatching {
                targetFile.readText().parseTo(classOfT)
            }.getOrElse {
                lWarning("Failed to parse existing JSON, re-downloading...")
                downloadAndParse()
            }
        } else {
            FileUtils.deleteQuietly(targetFile)
        }
    }

    return downloadAndParse()
}

fun artifactToPath(library: GameManifest.Library): String? {
    library.downloads?.artifact?.path?.let { return it }

    val libInfos = library.name.split(":")
    if (libInfos.size < 3) {
        lError("Invalid library name format: ${library.name}")
        return null
    }

    val groupId = libInfos[0].replace('.', '/')
    val artifactId = libInfos[1]
    val version = libInfos[2]

    var classifier = if (libInfos.size > 3) "-${libInfos[3]}" else ""
    if (library.isNative) run {
        val natives = library.natives ?: return@run
        if (natives.isNotEmpty()) {
            //Android，在这里使用Linux
            val native = natives[OperatingSystem.Linux] ?: return@run
            classifier = "-$native"
        }
    }

    return "$groupId/$artifactId/$version/$artifactId-$version$classifier.jar"
}

fun processLibraries(libraries: () -> List<GameManifest.Library>) {
    libraries().forEach { library ->
        if (library.filterLibrary()) return@forEach

        val versionSegment = library.name.split(":").getOrNull(2) ?: return
        val versionParts = versionSegment.split(".")

        getLibraryReplacement(library.name, versionParts)?.let { replacement ->
            lDebug("Library ${library.name} has been changed to version ${replacement.newName.split(":").last()}")
            updateLibrary(library, replacement)
        }
    }
}

private fun updateLibrary(
    library: GameManifest.Library,
    replacement: LibraryReplacement
) {
    createLibraryInfo(library)
    library.name = replacement.newName
    library.downloads.artifact.apply {
        path = replacement.newPath
        sha1 = replacement.newSha1
        url = replacement.newUrl
    }
}

private fun createLibraryInfo(library: GameManifest.Library) {
    if (library.downloads?.artifact == null) {
        library.downloads = GameManifest.DownloadsX().apply {
            this.artifact = GameManifest.Artifact()
        }
    }
}
