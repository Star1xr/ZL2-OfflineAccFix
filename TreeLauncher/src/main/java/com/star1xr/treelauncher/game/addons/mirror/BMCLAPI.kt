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

package com.star1xr.treelauncher.game.addons.mirror

import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.MirrorSourceType
import com.star1xr.treelauncher.utils.isChinaMainland

private const val ROOT = "https://bmclapi2.bangbang93.com"

enum class BMCLAPI(val url: String) {
    BASE_URL(ROOT),
    MAVEN("$ROOT/maven"),
    ASSETS("$ROOT/assets"),
    LIBRARIES("$ROOT/libraries")
}

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/9aa1367/HMCLCore/src/main/java/org/jackhuang/hmcl/download/BMCLAPIDownloadProvider.java#L64-L83)
 */
private val REPLACE_MIRROR_HOLDERS = mapOf(
    Pair(BMCLAPI.BASE_URL.url, BMCLAPI.BASE_URL.url),
    Pair("https://launchermeta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-meta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-data.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://launcher.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://libraries.minecraft.net", BMCLAPI.LIBRARIES.url),
    Pair("https://resources.download.minecraft.net", BMCLAPI.ASSETS.url),
    Pair("http://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://maven.minecraftforge.net", BMCLAPI.MAVEN.url),
    Pair("https://maven.neoforged.net/releases/net/neoforged/forge", BMCLAPI.MAVEN.url + "/net/neoforged/forge"),
    Pair("https://maven.neoforged.net/releases/net/neoforged/neoforge", BMCLAPI.MAVEN.url + "/net/neoforged/neoforge"),
    Pair("http://dl.liteloader.com/versions/versions.json", BMCLAPI.MAVEN.url + "/com/mumfrey/liteloader/versions.json"),
    Pair("http://dl.liteloader.com/versions", BMCLAPI.MAVEN.url),
    Pair("https://meta.fabricmc.net", BMCLAPI.BASE_URL.url + "/fabric-meta"),
    Pair("https://maven.fabricmc.net", BMCLAPI.MAVEN.url),
    Pair("https://authlib-injector.yushi.moe", BMCLAPI.BASE_URL.url + "/mirrors/authlib-injector"),
    Pair("https://repo1.maven.org/maven2", "https://mirrors.cloud.tencent.com/nexus/repository/maven-public"),
    Pair("https://repo.maven.apache.org/maven2", "https://mirrors.cloud.tencent.com/nexus/repository/maven-public"),
    Pair("https://hmcl-dev.github.io/metadata/cleanroom", "https://alist.8mi.tech/d/mirror/HMCL-Metadata/Auto/cleanroom")
)

/**
 * 替换为 BMCL API 镜像源链接，若如匹配的链接，则仅返回官方链接集合
 */
fun String.mapBMCLMirrorUrls(): List<String> {
    var isAssetsFile = false

    val mirrorUrl = REPLACE_MIRROR_HOLDERS.entries.find { (key, mirror) ->
        isAssetsFile = mirror == BMCLAPI.ASSETS.url
        this.startsWith(key)
    }?.let { (origin, mirror) ->
        this.replaceFirst(origin, mirror)
    }

    return if (isChinaMainland()) {
        val type = if (!isAssetsFile) {
            AllSettings.fileDownloadSource.getValue()
        } else {
            //资源文件数量过多，请求量大，应先尝试官方源，减轻 BMCL API 源压力
            MirrorSourceType.OFFICIAL_FIRST
        }
        when (type) {
            MirrorSourceType.OFFICIAL_FIRST -> listOfNotNull(this, mirrorUrl)
            MirrorSourceType.MIRROR_FIRST -> listOfNotNull(mirrorUrl, this)
        }
    } else {
        listOf(this)
    }
}