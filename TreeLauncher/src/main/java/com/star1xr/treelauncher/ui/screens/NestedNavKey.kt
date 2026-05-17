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

package com.star1xr.treelauncher.ui.screens

import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.game.version.installed.Version
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 嵌套NavDisplay的屏幕
 */
sealed interface NestedNavKey {
    /** 启动屏幕 */
    @Serializable class Splash : BackStackNavKey<TitledNavKey>()
    /** 主屏幕 */
    @Serializable class Main : BackStackNavKey<TitledNavKey>()
    /** 设置屏幕 */
    @Serializable class Settings : BackStackNavKey<TitledNavKey>(R.string.generic_setting)
    /** 版本详细设置屏幕 */
    @Serializable
    class VersionSettings(@Contextual val version: Version) : BackStackNavKey<TitledNavKey>(
        R.string.page_title_version_manage
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.Versions.OverView)
        }
    }
    /** 导出整合包屏幕 */
    @Serializable
    class VersionExport(@Contextual val version: Version) : BackStackNavKey<TitledNavKey>(
        R.string.versions_export
    ) {
        init {
            backStack.addIfEmpty(NormalNavKey.VersionExports.SelectType)
        }
    }
    /** 下载屏幕 */
    @Serializable class Download : BackStackNavKey<TitledNavKey>(
        R.string.generic_download
    )

    //下载嵌套子屏幕
    /** 下载游戏屏幕 */
    @Serializable class DownloadGame : BackStackNavKey<TitledNavKey>(
        R.string.download_category_game
    )
    /** 下载整合包屏幕 */
    @Serializable class DownloadModPack : BackStackNavKey<TitledNavKey>(
        R.string.download_category_modpack
    )
    /** 下载模组屏幕 */
    @Serializable class DownloadMod : BackStackNavKey<TitledNavKey>(
        R.string.download_category_mod
    )
    /** 下载资源包屏幕 */
    @Serializable class DownloadResourcePack : BackStackNavKey<TitledNavKey>(
        R.string.download_category_resource_pack
    )
    /** 下载存档屏幕 */
    @Serializable class DownloadSaves : BackStackNavKey<TitledNavKey>(
        R.string.download_category_saves
    )
    /** 下载光影包屏幕 */
    @Serializable class DownloadShaders : BackStackNavKey<TitledNavKey>(
        R.string.download_category_shaders
    )
}