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

package com.movtery.zalithlauncher.ui.screens

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.screens.content.FirstLoginMenu
import kotlinx.serialization.Serializable

/**
 * 普通的屏幕
 */
sealed interface NormalNavKey : TitledNavKey {
    @get:StringRes
    override val title: Int?
        get() = null

    /** 解压依赖内容屏幕（启动屏幕） */
    @Serializable data object UnpackDeps: NormalNavKey
    /** 启动器主页屏幕 */
    @Serializable data object LauncherMain : NormalNavKey
    /** 账号管理屏幕 */
    @Serializable data class AccountManager(
        val loginMenu: FirstLoginMenu = FirstLoginMenu.NONE
    ) : NormalNavKey {
        override val title: Int = R.string.page_title_account_list
    }
    /** Web屏幕 */
    @Serializable data class WebScreen(val url: String) : NormalNavKey
    /** 版本管理屏幕 */
    @Serializable data object VersionsManager : NormalNavKey {
        override val title: Int = R.string.page_title_version_list
    }
    /** 文件选择屏幕 */
    @Serializable data class FileSelector(
        val startPath: String,
        val selectFile: Boolean,
        val saveKey: TitledNavKey,
        val onSelected: (path: String) -> Unit
    ) : NormalNavKey {
        override val title: Int = R.string.page_title_select_files
    }
    /** 多人联机屏幕 */
    @Serializable data object Multiplayer: NormalNavKey {
        override val title: Int = R.string.terracotta_terracotta
    }

    /** 查看日志屏幕 */
    @Serializable data class LogView(
        val logPath: String
    ) : NormalNavKey {
        override val title: Int = R.string.versions_overview_log
    }

    /** 设置嵌套子屏幕 */
    sealed interface Settings : NormalNavKey {
        /** 渲染器设置屏幕 */
        @Serializable data object Renderer : Settings {
            override val title: Int = R.string.settings_tab_renderer
        }
        /** 游戏设置屏幕 */
        @Serializable data object Game : Settings {
            override val title: Int = R.string.settings_tab_game
        }
        /** 控制设置屏幕 */
        @Serializable data object Control : Settings {
            override val title: Int = R.string.settings_tab_control
        }
        /** 手柄设置屏幕 */
        @Serializable data object Gamepad : Settings {
            override val title: Int = R.string.settings_tab_gamepad
        }
        /** 启动器设置屏幕 */
        @Serializable data object Launcher : Settings {
            override val title: Int = R.string.settings_tab_launcher
        }
        /** Java管理屏幕 */
        @Serializable data object JavaManager : Settings {
            override val title: Int = R.string.settings_tab_java_manage
        }
        /** 控制管理屏幕 */
        @Serializable data object ControlManager : Settings {
            override val title: Int = R.string.settings_tab_control_manage
        }
        /** 关于屏幕 */
        @Serializable data object AboutInfo : Settings {
            override val title: Int = R.string.settings_tab_info_about
        }
        /** Vulkan 驱动下载屏幕 */
        @Serializable data object VulkanDriverDownloader : Settings {
            override val title: Int = R.string.vulkan_driver_downloader_title
        }
    }

    /** 版本详细设置嵌套子屏幕 */
    sealed interface Versions : NormalNavKey {
        /** 版本概览屏幕 */
        @Serializable data object OverView : Versions {
            override val title: Int = R.string.versions_settings_overview
        }
        /** 版本配置屏幕 */
        @Serializable data object Config : Versions {
            override val title: Int = R.string.versions_settings_config
        }
        /** 更新版本的模组加载器 */
        @Serializable data object UpdateLoader : Versions {
            override var title: Int = R.string.versions_update_loader
        }
        /** 模组管理屏幕 */
        @Serializable data object ModsManager : Versions {
            override var title: Int = R.string.mods_manage
        }
        /** 存档管理屏幕 */
        @Serializable data object SavesManager : Versions {
            override var title: Int = R.string.saves_manage
        }
        /** 资源包管理屏幕 */
        @Serializable data object ResourcePackManager : Versions {
            override var title: Int = R.string.resource_pack_manage
        }
        /** 光影包管理屏幕 */
        @Serializable data object ShadersManager : Versions {
            override var title: Int = R.string.shader_pack_manage
        }
        /** 截屏管理屏幕 */
        @Serializable data object ScreenshotsManager : Versions {
            override var title: Int = R.string.screenshots_manage
        }
        /** 服务器列表屏幕 */
        @Serializable data object ServerList : Versions {
            override var title: Int = R.string.servers_list
        }
    }

    /** 导出整合包屏幕 */
    sealed interface VersionExports : NormalNavKey {
        /** 选择导出格式 */
        @Serializable data object SelectType : VersionExports
        /** 编辑整合包导出配置 */
        @Serializable data object EditInfo : VersionExports
        /** 选择要导出的文件 */
        @Serializable data object SelectFiles : VersionExports
    }

    /** 下载游戏嵌套子屏幕 */
    sealed interface DownloadGame : NormalNavKey {
        /** 选择游戏版本屏幕 */
        @Serializable data object SelectGameVersion : Versions
        /** 选择附加内容屏幕 */
        @Serializable data class Addons(val gameVersion: String) : Versions
    }

    /** 搜索整合包屏幕 */
    @Serializable data object SearchModPack : NormalNavKey
    /** 搜索模组屏幕 */
    @Serializable data object SearchMod : NormalNavKey
    /** 搜索资源包屏幕 */
    @Serializable data object SearchResourcePack : NormalNavKey
    /** 搜索存档屏幕 */
    @Serializable data object SearchSaves : NormalNavKey
    /** 搜索光影包屏幕 */
    @Serializable data object SearchShaders : NormalNavKey

    /** 下载资源屏幕 */
    @Serializable data class DownloadAssets(
        val platform: Platform,
        val projectId: String,
        val classes: PlatformClasses,
        val iconUrl: String? = null
    ) : NormalNavKey

    /** 协议展示屏幕 */
    @Serializable data class License(
        val raw: Int
    ): NormalNavKey
}