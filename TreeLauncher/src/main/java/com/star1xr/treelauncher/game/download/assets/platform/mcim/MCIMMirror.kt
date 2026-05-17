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

package com.star1xr.treelauncher.game.download.assets.platform.mcim

import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.MirrorSourceType
import com.star1xr.treelauncher.utils.isChinaMainland

private const val ROOT = "https://mod.mcimirror.top"

private val REPLACE_MIRROR_HOLDERS = listOf(
    //CurseForge
    "https://edge.forgecdn.net",
    //Modrinth
    "https://cdn.modrinth.com"
)

/**
 * 根据是否为中国地区决定，是否启用 MCIM 镜像源，若启用，则会根据优先级，生成链接集合
 */
fun String.mapMCIMMirrorUrls(): List<String> {
    return if (isChinaMainland()) {
        val mirroredUrl = REPLACE_MIRROR_HOLDERS.find { key ->
            this.startsWith(key)
        }?.let { origin ->
            this.replaceFirst(origin, ROOT)
        }

        val source = AllSettings.assetDownloadSource.getValue()
        when (source) {
            MirrorSourceType.OFFICIAL_FIRST ->
                listOfNotNull(this, mirroredUrl)
            MirrorSourceType.MIRROR_FIRST ->
                listOfNotNull(mirroredUrl, this)
        }
    } else {
        listOf(this)
    }
}

/**
 * 根据是否为中国地区决定，是否启用 MCIM 镜像源，若启用，则会根据优先级，生成链接集合
 *
 * 特殊：本身就已经是多链接的形式了，这个函数要做的事情：
 * 将检查数组内是否有可以被替换的链接，如果有，则生成镜像链接，根据优先级，穿插到列表前/后
 */
fun Array<String>.mapMCIMMirrorUrls(): List<String> {
    if (isChinaMainland()) {
        val sources = mapNotNull { url ->
            REPLACE_MIRROR_HOLDERS.find { key ->
                url.startsWith(key)
            }?.let { origin ->
                url.replaceFirst(origin, ROOT)
            }
        }
        if (sources.isNotEmpty()) {
            val source = AllSettings.assetDownloadSource.getValue()
            return buildList {
                when (source) {
                    MirrorSourceType.OFFICIAL_FIRST -> {
                        addAll(this@mapMCIMMirrorUrls)
                        addAll(sources)
                    }
                    MirrorSourceType.MIRROR_FIRST -> {
                        addAll(sources)
                        addAll(this@mapMCIMMirrorUrls)
                    }
                }
            }
        }
    }
    return toList()
}