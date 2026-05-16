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

package com.movtery.zalithlauncher.ui.screens.content.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.VulkanDriverDownloadState
import com.movtery.zalithlauncher.viewmodel.VulkanDriverViewModel

@Composable
fun VulkanDriverDownloaderScreen(
    key: NormalNavKey.Settings.VulkanDriverDownloader,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    viewModel: VulkanDriverViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val installedDrivers by viewModel.installedDrivers.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchReleases()
    }

    var deleteConfirmRelease by remember { mutableStateOf<com.movtery.zalithlauncher.game.plugin.driver.GitHubRelease?>(null) }

    if (deleteConfirmRelease != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteConfirmRelease = null },
            title = { Text(stringResource(R.string.generic_tip)) },
            text = { Text(stringResource(R.string.vulkan_driver_downloader_delete_confirm, deleteConfirmRelease!!.name)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val release = deleteConfirmRelease!!
                        viewModel.deleteDriver(release.name) {
                            DriverPluginManager.initDriver(context)
                        }
                        deleteConfirmRelease = null
                    }
                ) {
                    Text(stringResource(R.string.generic_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { deleteConfirmRelease = null }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.VulkanDriverDownloader, settingsScreenKey, false)
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.vulkan_driver_downloader_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (val currentState = state) {
                is VulkanDriverDownloadState.Fetching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VulkanDriverDownloadState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.releases) { release ->
                            ReleaseItem(
                                release = release,
                                isInstalled = installedDrivers.contains(release.name),
                                onDownload = { asset ->
                                    viewModel.downloadDriver(asset, release.name) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.vulkan_driver_downloader_success, release.name),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        DriverPluginManager.initDriver(context)
                                    }
                                },
                                onDelete = {
                                    deleteConfirmRelease = release
                                }
                            )
                        }
                    }
                }
                is VulkanDriverDownloadState.Error -> {
                    Text(
                        text = stringResource(R.string.vulkan_driver_downloader_fetch_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ReleaseItem(
    release: com.movtery.zalithlauncher.game.plugin.driver.GitHubRelease,
    isInstalled: Boolean,
    onDownload: (com.movtery.zalithlauncher.game.plugin.driver.GitHubAsset) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = release.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (isInstalled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.download_state_finished),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = stringResource(R.string.generic_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            release.body?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            
            release.assets.filter { it.name.endsWith(".zip") }.forEach { asset ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = asset.name, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDownload(asset) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download_2_filled),
                            contentDescription = stringResource(R.string.generic_download)
                        )
                    }
                }
            }
        }
    }
}
