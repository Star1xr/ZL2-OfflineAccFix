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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.plugin.driver.GitHubAsset
import com.movtery.zalithlauncher.game.plugin.driver.GitHubRelease
import com.movtery.zalithlauncher.game.plugin.driver.VulkanDriverManager
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.PathManager
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed interface VulkanDriverDownloadState {
    data object Idle : VulkanDriverDownloadState
    data object Fetching : VulkanDriverDownloadState
    data class Success(val releases: List<GitHubRelease>) : VulkanDriverDownloadState
    data class Error(val message: String) : VulkanDriverDownloadState
}

class VulkanDriverViewModel : ViewModel() {
    private val _state = MutableStateFlow<VulkanDriverDownloadState>(VulkanDriverDownloadState.Idle)
    val state = _state.asStateFlow()

    private val _installedDrivers = MutableStateFlow<Set<String>>(emptySet())
    val installedDrivers = _installedDrivers.asStateFlow()

    init {
        refreshInstalledDrivers()
    }

    fun refreshInstalledDrivers() {
        viewModelScope.launch(Dispatchers.IO) {
            val drivers = VulkanDriverManager.getDriversHome().listFiles { file -> file.isDirectory }
                ?.map { it.name }?.toSet() ?: emptySet()
            _installedDrivers.value = drivers
        }
    }

    fun fetchReleases() {
        viewModelScope.launch {
            _state.value = VulkanDriverDownloadState.Fetching
            val releases = VulkanDriverManager.fetchReleases()
            _state.value = if (releases.isNotEmpty()) {
                VulkanDriverDownloadState.Success(releases)
            } else {
                VulkanDriverDownloadState.Error("Failed to fetch releases")
            }
        }
    }

    fun downloadDriver(asset: GitHubAsset, driverName: String, onFinished: () -> Unit) {
        TaskSystem.submitTask(
            Task.runTask(
                dispatcher = Dispatchers.IO,
                task = { task ->
                    val tempFile = File(PathManager.DIR_CACHE, asset.name)
                    
                    // 1. Download
                    task.updateMessage(R.string.vulkan_driver_downloader_downloading, asset.name)
                    GLOBAL_CLIENT.get(asset.browser_download_url).bodyAsChannel().copyTo(FileOutputStream(tempFile))

                    // 2. Extract
                    task.updateMessage(R.string.vulkan_driver_downloader_extracting, asset.name)
                    val targetDir = File(VulkanDriverManager.getDriversHome(), driverName)
                    targetDir.mkdirs()
                    VulkanDriverManager.extractZip(tempFile, targetDir)

                    tempFile.delete()

                    refreshInstalledDrivers()

                    withContext(Dispatchers.Main) {
                        onFinished()
                    }
                }
            )
        )
    }

    fun deleteDriver(driverName: String, onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            VulkanDriverManager.deleteDriver(driverName)
            refreshInstalledDrivers()
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}
