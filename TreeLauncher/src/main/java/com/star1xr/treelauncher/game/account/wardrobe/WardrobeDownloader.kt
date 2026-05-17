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

package com.star1xr.treelauncher.game.account.wardrobe

import com.google.gson.JsonObject
import com.star1xr.treelauncher.path.createOkHttpClient
import com.star1xr.treelauncher.utils.GSON
import com.star1xr.treelauncher.utils.logging.Logger
import com.star1xr.treelauncher.utils.network.fetchStringFromUrl
import com.star1xr.treelauncher.utils.string.decodeBase64
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

abstract class WardrobeDownloader {
    protected val mClient = createOkHttpClient()

    protected suspend fun yggdrasil(
        url: String,
        uuid: String
    ): JsonObject {
        val profileJson = fetchStringFromUrl("${url.removeSuffix("/")}/session/minecraft/profile/$uuid")
        val profileObject = GSON.fromJson(profileJson, JsonObject::class.java)
        val properties = profileObject.get("properties").asJsonArray
        val rawValue = properties.get(0).asJsonObject.get("value").asString

        val value = decodeBase64(rawValue)

        return GSON.fromJson(value, JsonObject::class.java)
    }

    protected fun download(url: String, file: File) {
        file.parentFile?.apply {
            if (!exists()) mkdirs()
        }

        val request = Request.Builder()
            .url(url)
            .build()

        mClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Unexpected code $response")
            }

            try {
                response.body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.lError("Failed to download skin file", e)
            }
        }
    }
}