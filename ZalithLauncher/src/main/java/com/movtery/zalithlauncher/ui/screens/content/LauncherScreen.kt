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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionsOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.CopyVersionDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ChangeGroupDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.ToggleableIntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.getMessageOrToString
import com.movtery.zalithlauncher.utils.logging.lError
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: () -> Unit,
    onOpenLink: (String) -> Unit,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    onModsClick: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    var showQuickRamDialog by remember { mutableStateOf<Boolean>(false) }
    var showQuickFpsDialog by remember { mutableStateOf<Boolean>(false) }

    if (showQuickRamDialog) {
        QuickRamDialog(
            onDismissRequest = { showQuickRamDialog = false }
        )
    }

    if (showQuickFpsDialog) {
        QuickFpsDialog(
            onDismissRequest = { showQuickFpsDialog = false }
        )
    }

    var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }

    when (val operation = versionsOperation) {
        is VersionsOperation.Copy -> {
            CopyVersionDialog(
                onConfirm = { name, copyAll ->
                    versionsOperation = VersionsOperation.RunTask(
                        title = R.string.versions_manage_copy_version,
                        task = { VersionsManager.copyVersion(operation.version, name, copyAll) }
                    )
                },
                onDismissRequest = { versionsOperation = VersionsOperation.None }
            )
        }

        is VersionsOperation.ChangeGroup -> {
            ChangeGroupDialog(
                version = operation.version,
                onDismissRequest = { versionsOperation = VersionsOperation.None },
                onConfirm = { group ->
                    versionsOperation = VersionsOperation.RunTask(
                        title = R.string.generic_setting,
                        task = {
                            operation.version.getVersionConfig().apply {
                                this.group = group
                                saveWithThrowable()
                            }
                        }
                    )
                }
            )
        }

        is VersionsOperation.RunTask -> {
            val errorMessage = stringResource(R.string.versions_manage_task_error)
            SimpleTaskDialog(
                title = stringResource(operation.title),
                task = operation.task,
                context = Dispatchers.IO,
                onDismiss = {
                    VersionsManager.refresh("LauncherScreen.RunTask")
                    versionsOperation = VersionsOperation.None
                },
                onError = { e ->
                    lError("Failed to run task.", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = errorMessage,
                            message = e.getMessageOrToString()
                        )
                    )
                }
            )
        }

        else -> {}
    }

    val versions by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main Content: Categorized Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                VersionGrid(
                    modifier = Modifier.fillMaxSize(),
                    versions = VersionsManager.versions,
                    currentVersion = currentVersion,
                    onVersionClick = { version ->
                        VersionsManager.saveCurrentVersion(version.getVersionName())
                    }
                )
            }

            // Right Sidebar: Action Panel
            RightActionSidebar(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                version = currentVersion,
                onLaunch = onLaunchGame,
                onEdit = {
                    currentVersion?.let { navigateToVersions(it) }
                },
                onDelete = {
                    currentVersion?.let { VersionsManager.deleteVersion(it) }
                },
                onCopyClick = {
                    currentVersion?.let { versionsOperation = VersionsOperation.Copy(it) }
                },
                onChangeGroupClick = {
                    currentVersion?.let { versionsOperation = VersionsOperation.ChangeGroup(it) }
                },
                onModsClick = onModsClick
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f, label = ""
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(14.dp)
                .rotate(rotation),
            painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall, // Larger (was labelMedium)
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp)) // More gap
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun VersionGrid(
    versions: List<Version>,
    currentVersion: Version?,
    onVersionClick: (Version) -> Unit,
    modifier: Modifier = Modifier
) {
    val pinned = versions.filter { it.pinnedState }
    val unpinned = versions.filter { !it.pinnedState }

    val grouped = unpinned.groupBy { it.getVersionConfig().group }
    val sortedGroupNames = grouped.keys.filter { it.isNotEmpty() }.sorted()

    var collapsedGroups by rememberSaveable { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pinned.isNotEmpty()) {
            val isExpanded = "pinned" !in collapsedGroups
            item {
                CategoryHeader(
                    title = stringResource(R.string.category_pinned),
                    isExpanded = isExpanded,
                    onExpandClick = {
                        collapsedGroups = if (isExpanded) collapsedGroups + "pinned" else collapsedGroups - "pinned"
                    }
                )
            }
            if (isExpanded) {
                item {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pinned.forEach { version ->
                            VersionGridItem(
                                version = version,
                                isSelected = version == currentVersion,
                                onClick = { onVersionClick(version) }
                            )
                        }
                    }
                }
            }
        }

        // Ungrouped
        val ungrouped = grouped[""] ?: emptyList()
        if (ungrouped.isNotEmpty()) {
            val isExpanded = "ungrouped" !in collapsedGroups
            item {
                CategoryHeader(
                    title = stringResource(R.string.category_ungrouped),
                    isExpanded = isExpanded,
                    onExpandClick = {
                        collapsedGroups = if (isExpanded) collapsedGroups + "ungrouped" else collapsedGroups - "ungrouped"
                    }
                )
            }
            if (isExpanded) {
                item {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ungrouped.forEach { version ->
                            VersionGridItem(
                                version = version,
                                isSelected = version == currentVersion,
                                onClick = { onVersionClick(version) }
                            )
                        }
                    }
                }
            }
        }

        // Groups
        sortedGroupNames.forEach { groupName ->
            val groupVersions = grouped[groupName] ?: emptyList()
            if (groupVersions.isNotEmpty()) {
                val isExpanded = groupName !in collapsedGroups
                item(key = groupName) {
                    CategoryHeader(
                        title = groupName,
                        isExpanded = isExpanded,
                        onExpandClick = {
                            collapsedGroups = if (isExpanded) collapsedGroups + groupName else collapsedGroups - groupName
                        }
                    )
                }
                if (isExpanded) {
                    item {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            groupVersions.forEach { version ->
                                VersionGridItem(
                                    version = version,
                                    isSelected = version == currentVersion,
                                    onClick = { onVersionClick(version) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionGridItem(
    version: Version,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFF3DAEE9) else Color.Transparent)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VersionIconImage(
            version = version,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = version.getVersionName(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RightActionSidebar(
    version: Version?,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopyClick: () -> Unit,
    onChangeGroupClick: () -> Unit,
    onModsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = version != null

    Column(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(140.dp), // One-click larger (was 120)
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            VersionIconImage(
                version = version,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = version?.getVersionName() ?: stringResource(R.string.sidebar_no_instance_selected),
            style = MaterialTheme.typography.titleMedium, // Larger font
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )

        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp) // More spacing
        ) {
            SidebarActionItem(
                icon = R.drawable.ic_play_arrow_filled,
                label = stringResource(R.string.main_launch_game),
                onClick = onLaunch,
                isPrimary = true,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_close,
                label = stringResource(R.string.sidebar_action_kill),
                onClick = { },
                enabled = false
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            SidebarActionItem(
                icon = R.drawable.ic_edit_filled,
                label = stringResource(R.string.versions_manage_settings),
                onClick = onEdit,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_sort,
                label = stringResource(R.string.sidebar_action_change_group),
                onClick = onChangeGroupClick,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_extension_outlined,
                label = stringResource(R.string.topbar_mods),
                onClick = onModsClick,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_share_filled,
                label = stringResource(R.string.sidebar_action_export),
                onClick = { },
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_copy_all_filled,
                label = stringResource(R.string.sidebar_action_copy),
                onClick = onCopyClick,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_delete_filled,
                label = stringResource(R.string.generic_delete),
                onClick = onDelete,
                contentColor = MaterialTheme.colorScheme.error,
                enabled = isSelected
            )
            SidebarActionItem(
                icon = R.drawable.ic_add,
                label = stringResource(R.string.sidebar_action_create_shortcut),
                onClick = { },
                enabled = isSelected
            )
        }
    }
}

@Composable
private fun SidebarActionItem(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val alpha = if (enabled) 1f else 0.3f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp), // Larger (was 32)
        onClick = if (enabled) onClick else ({}),
        color = Color.Transparent,
        contentColor = (if (isPrimary) MaterialTheme.colorScheme.primary else contentColor).copy(alpha = alpha),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp), // Larger
                painter = painterResource(icon),
                contentDescription = label
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge, // Larger
                maxLines = 1
            )
            if (isPrimary) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun QuickFpsDialog(
    onDismissRequest: () -> Unit
) {
    val showFps = AllSettings.showFPS.state
    val resolutionRatio = AllSettings.resolutionRatio.state
    var tempResolutionRatio by remember { mutableIntStateOf(resolutionRatio) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_menu_option_switch_fps),
                    style = MaterialTheme.typography.titleMedium
                )

                SettingsCardColumn {
                    SwitchSettingsCard(
                        position = CardPosition.Top,
                        title = stringResource(R.string.game_menu_option_switch_fps),
                        checked = showFps,
                        onCheckedChange = { AllSettings.showFPS.save(it) }
                    )
                    SwitchSettingsCard(
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_renderer_force_big_core_title),
                        summary = stringResource(R.string.settings_renderer_force_big_core_summary),
                        checked = AllSettings.bigCoreAffinity.state,
                        onCheckedChange = { AllSettings.bigCoreAffinity.save(it) }
                    )
                    SwitchSettingsCard(
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_renderer_sustained_performance_title),
                        summary = stringResource(R.string.settings_renderer_sustained_performance_summary),
                        checked = AllSettings.sustainedPerformance.state,
                        onCheckedChange = { AllSettings.sustainedPerformance.save(it) }
                    )
                    ToggleableIntSliderSettingsCard(
                        position = CardPosition.Bottom,
                        currentValue = tempResolutionRatio,
                        valueRange = AllSettings.resolutionRatio.floatRange,
                        defaultValue = AllSettings.resolutionRatio.defaultValue,
                        title = stringResource(R.string.settings_renderer_resolution_scale_title),
                        summary = stringResource(R.string.settings_renderer_resolution_scale_summary),
                        suffix = "%",
                        onValueChange = {
                            tempResolutionRatio = it
                        },
                        onValueChangeFinished = {
                            AllSettings.resolutionRatio.save(tempResolutionRatio)
                        }
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    Text(text = stringResource(R.string.generic_confirm))
                }
            }
        }
    }
}

@Composable
fun QuickRamDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val globalRamAllocation = AllSettings.ramAllocation.state
    var tempRamAllocation by remember { mutableStateOf(globalRamAllocation) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_game_java_memory_title),
                    style = MaterialTheme.typography.titleMedium
                )

                ToggleableIntSliderSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Single,
                    currentValue = tempRamAllocation ?: 0,
                    valueRange = AllSettings.ramAllocation.floatRange.start..getMaxMemoryForSettings(context).toFloat(),
                    defaultValue = AllSettings.ramAllocation.getOrMin(),
                    title = stringResource(R.string.settings_game_java_memory_title),
                    summary = stringResource(R.string.settings_game_java_memory_summary),
                    suffix = "MB",
                    onValueChange = {
                        tempRamAllocation = it
                    },
                    onValueChangeFinished = {
                        AllSettings.ramAllocation.save(tempRamAllocation)
                    },
                    previewContent = {
                        MemoryPreview(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp, end = 8.dp),
                            preview = tempRamAllocation?.toDouble(),
                            usedText = { usedMemory: Double, totalMemory: Double ->
                                stringResource(R.string.settings_game_java_memory_used_text, usedMemory.toInt(), totalMemory.toInt())
                            },
                            previewText = { preview: Double ->
                                stringResource(R.string.settings_game_java_memory_allocation_text, preview.toInt())
                            }
                        )
                    }
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    Text(text = stringResource(R.string.generic_confirm))
                }
            }
        }
    }
}
