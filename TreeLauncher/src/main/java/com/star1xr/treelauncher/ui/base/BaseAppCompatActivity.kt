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

package com.star1xr.treelauncher.ui.base

import android.os.Bundle
import androidx.annotation.CallSuper
import com.star1xr.treelauncher.context.refreshContext
import com.star1xr.treelauncher.game.account.AccountsManager
import com.star1xr.treelauncher.game.path.GamePathManager
import com.star1xr.treelauncher.game.plugin.PluginLoader
import com.star1xr.treelauncher.game.renderer.Renderers
import com.star1xr.treelauncher.setting.loadAllSettings
import com.star1xr.treelauncher.utils.checkStoragePermissionsForInit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseAppCompatActivity(
    /** 是否刷新数据 */
    private val refreshData: Boolean = true
) : FullScreenAppCompatActivity() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshContext(this)
        checkStoragePermissions()

        if (refreshData) {
            //加载渲染器
            Renderers.init()
            //加载插件
            PluginLoader.loadAllPlugins(this, false)
            //刷新其他内容
            refreshData()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        loadAllSettings(this, true)
        checkStoragePermissions()
    }

    private fun refreshData() {
        AccountsManager.reloadAccounts()
        AccountsManager.reloadAuthServers()
        GamePathManager.reloadPath()
    }

    private fun checkStoragePermissions() {
        //检查所有文件管理权限
        checkStoragePermissionsForInit(this)
    }

    protected fun runFinish() = run { finish() }
}