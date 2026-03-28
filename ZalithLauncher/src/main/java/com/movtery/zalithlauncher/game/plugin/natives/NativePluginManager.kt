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

package com.movtery.zalithlauncher.game.plugin.natives

import android.content.Context
import android.content.pm.ApplicationInfo
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.ApkPluginManager
import com.movtery.zalithlauncher.game.plugin.cacheAppIcon

object NativePluginManager: ApkPluginManager() {
    private val nativePlugins = mutableListOf<NativePlugin>()

    fun getPlugins(): List<NativePlugin> = nativePlugins.toList()

    fun getJVMEnv(): List<String> {
        return buildList {
            nativePlugins.forEach { plugin ->
                addAll(plugin.envList)
            }
        }
    }

    fun clearPlugin() {
        nativePlugins.clear()
    }

    override fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (
                metaData.getBoolean("FCLNativePlugin", false)
            ) {
                val nativeLibraryDir = info.nativeLibraryDir
                val packageManager = context.packageManager
                val packageName = info.packageName
                val appName = info.loadLabel(packageManager).toString()

                val environment = metaData.getString("environment") ?: return
                val des = metaData.getString("des") ?: ""

                val envList = if (environment.isNotEmpty()) {
                    val entries = environment.split(" ")
                    buildList {
                        entries.forEach { entry ->
                            add(parseEntry(entry, nativeLibraryDir))
                        }
                    }
                } else {
                    emptyList()
                }

                val plugin = NativePlugin(
                    packageName = packageName,
                    displayName = des,
                    summary = context.getString(R.string.settings_renderer_from_plugins, appName),
                    minMCVer = metaData.getVersionString("minMCVer"),
                    maxMCVer = metaData.getVersionString("maxMCVer"),
                    path = nativeLibraryDir,
                    envList = envList
                )
                nativePlugins.add(plugin)

                runCatching {
                    cacheAppIcon(context, info)
                    ApkPlugin(
                        packageName = packageName,
                        appName = appName,
                        appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: ""
                    )
                }.getOrNull()?.let { loaded(it) }
            }
        }
    }

    private fun parseEntry(
        entry: String,
        nativeLibraryDir: String
    ): String {
        var (key, value) = entry.split("=")

        if (value == "{nativeLibraryDir}") {
            value = nativeLibraryDir
        }

        return "$key=$value"
    }
}