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

package com.movtery.zalithlauncher.ui.screens.main

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.ui.activities.CrashType
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.viewmodel.CrashLogsUploadViewModel

@Composable
fun ErrorScreen(
    viewModel: CrashLogsUploadViewModel,
    crashType: CrashType,
    shareLogs: Boolean = true,
    canUpload: Boolean = false,
    canRestart: Boolean = true,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {},
    body: @Composable ColumnScope.() -> Unit
) {
    //获取方向信息，展示两套不同的UI
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ErrorScreenLandscape(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            body = body
        )
    } else {
        ErrorScreenPortrait(
            crashType = crashType,
            shareLogs = shareLogs,
            canUpload = canUpload,
            canRestart = canRestart,
            onShareLogsClick = onShareLogsClick,
            onUploadClick = onUploadClick,
            onRestartClick = onRestartClick,
            onExitClick = onExitClick,
            body = body
        )
    }
}

/**
 * 崩溃页面（横屏页面）
 */
@Composable
private fun ErrorScreenLandscape(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .zIndex(10f),
            crashType = crashType,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                ErrorContent(
                    modifier = Modifier
                        .weight(7f)
                        .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                    body = body
                )

                ActionContext(
                    modifier = Modifier
                        .weight(3f)
                        .padding(all = 12.dp),
                    crashType = crashType,
                    shareLogs = shareLogs,
                    canUpload = canUpload,
                    canRestart = canRestart,
                    onShareLogsClick = onShareLogsClick,
                    onUploadClick = onUploadClick,
                    onRestartClick = onRestartClick,
                    onExitClick = onExitClick
                )
            }
        }
    }
}

/**
 * 崩溃页面（竖屏版本）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreenPortrait(
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit,
    onUploadClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    //控制下拉菜单的显示状态
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.crash_type,
                            stringResource(crashType.textRes)
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.generic_more)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (canUpload) {
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_link_share_button))
                                },
                                onClick = {
                                    showMenu = false
                                    onUploadClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                MarqueeText(text = stringResource(R.string.crash_share_logs))
                            },
                            onClick = {
                                showMenu = false
                                onShareLogsClick()
                            },
                            enabled = shareLogs
                        )
                        if (canRestart) {
                            DropdownMenuItem(
                                text = {
                                    MarqueeText(text = stringResource(R.string.crash_restart))
                                },
                                onClick = {
                                    showMenu = false
                                    onRestartClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                MarqueeText(text = stringResource(R.string.crash_exit))
                            },
                            onClick = {
                                showMenu = false
                                onExitClick()
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (crashType == CrashType.LAUNCHER_CRASH) {
                //仅在启动器崩溃时，才显示这行略显严重的文本
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.crash_launcher_title, InfoDistributor.LAUNCHER_NAME)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = body
                )
            } else {
                body()
            }
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    crashType: CrashType,
    color: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            val text = when (crashType) {
                //在启动器崩溃的时候，显示一个较为严重的标题
                CrashType.LAUNCHER_CRASH -> stringResource(R.string.crash_launcher_title, InfoDistributor.LAUNCHER_NAME)
                //游戏运行崩溃了，大概和启动器关系不大，仅展示应用标题
                CrashType.GAME_CRASH -> InfoDistributor.LAUNCHER_NAME
            }
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = text
            )
        }
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = body
        )
    }
}

@Composable
private fun ActionContext(
    modifier: Modifier = Modifier,
    crashType: CrashType,
    shareLogs: Boolean,
    canUpload: Boolean,
    canRestart: Boolean,
    onShareLogsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
    BackgroundCard(
        modifier = modifier,
        influencedByBackground = false,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(all = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(
                    R.string.crash_type,
                    stringResource(crashType.textRes)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (canUpload) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onUploadClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_link_share_button))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (shareLogs) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShareLogsClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_share_logs))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (canRestart) {
                ScalingActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestartClick
                ) {
                    MarqueeText(text = stringResource(R.string.crash_restart))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onExitClick
            ) {
                MarqueeText(text = stringResource(R.string.crash_exit))
            }
        }
    }
}