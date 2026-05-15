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

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.defaultRichTextStyle
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountAvatar
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.navigateToLogView
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.ToggleableIntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import com.movtery.zalithlauncher.ui.screens.main.custom_home.customHomePage
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.viewmodel.HomePageState
import com.movtery.zalithlauncher.viewmodel.LocalHomePageViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import java.io.File

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: () -> Unit,
    onOpenLink: (String) -> Unit,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
) {
    var showQuickRamDialog by remember { mutableStateOf(false) }
    var showQuickFpsDialog by remember { mutableStateOf(false) }

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
                onFolders = {
                    backStackViewModel.mainScreen.navigateTo(NormalNavKey.Versions.ModsManager)
                }
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
                .androidx.compose.ui.draw.rotate(rotation),
            painter = painterResource(R.drawable.ic_arrow_drop_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
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
    
    var pinnedExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var unpinnedExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pinned.isNotEmpty()) {
            item { 
                CategoryHeader(
                    title = "Pinned", 
                    isExpanded = pinnedExpanded,
                    onExpandClick = { pinnedExpanded = !pinnedExpanded }
                ) 
            }
            if (pinnedExpanded) {
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

        if (unpinned.isNotEmpty()) {
            item { 
                CategoryHeader(
                    title = "Ungrouped", 
                    isExpanded = unpinnedExpanded,
                    onExpandClick = { unpinnedExpanded = !unpinnedExpanded }
                ) 
            }
            if (unpinnedExpanded) {
                item {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        unpinned.forEach { version ->
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

@Composable
private fun VersionGridItem(
    version: Version,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(4.dp)) // Desktop-class angular approach
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFF3DAEE9) else Color.Transparent) // Solid blue for selected state
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
            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
        )
    }
}

@Composable
private fun RightActionSidebar(
    version: Version?,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFolders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview Image (Larger and centered)
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            VersionIconImage(
                version = version,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = version?.getVersionName() ?: "No Instance Selected",
            style = MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )

        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Action List - Very Compact
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SidebarActionItem(
                icon = R.drawable.ic_play_arrow_filled,
                label = "Launch",
                onClick = onLaunch,
                isPrimary = true
            )
            SidebarActionItem(
                icon = R.drawable.ic_close,
                label = "Kill",
                onClick = { /* Kill logic if running */ },
                enabled = false // Greyed out like in ref
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            SidebarActionItem(
                icon = R.drawable.ic_edit_filled,
                label = "Edit",
                onClick = onEdit
            )
            SidebarActionItem(
                icon = R.drawable.ic_sort,
                label = "Change Group",
                onClick = { /* Change group logic */ }
            )
            SidebarActionItem(
                icon = R.drawable.ic_folder_filled,
                label = "Folder",
                onClick = onFolders
            )
            SidebarActionItem(
                icon = R.drawable.ic_share,
                label = "Export",
                onClick = { /* Export logic */ }
            )
            SidebarActionItem(
                icon = R.drawable.ic_content_copy,
                label = "Copy",
                onClick = { /* Copy logic */ }
            )
            SidebarActionItem(
                icon = R.drawable.ic_delete_filled,
                label = "Delete",
                onClick = onDelete,
                contentColor = MaterialTheme.colorScheme.error
            )
            SidebarActionItem(
                icon = R.drawable.ic_add,
                label = "Create Shortcut",
                onClick = { /* Shortcut logic */ }
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
    val alpha = if (enabled) 1f else 0.4f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        onClick = if (enabled) onClick else ({}),
        color = if (isPrimary) Color.Transparent else Color.Transparent,
        contentColor = (if (isPrimary) MaterialTheme.colorScheme.primary else contentColor).copy(alpha = alpha),
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(icon),
                contentDescription = label
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            if (isPrimary) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun QuickFpsDialog(
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
private fun QuickRamDialog(
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

@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val homePageViewModel = LocalHomePageViewModel.current
    val pageState by homePageViewModel.pageState.collectAsStateWithLifecycle()
    val richTextStyle = defaultRichTextStyle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        if (BuildConfig.DEBUG) {
            item {
                //debug版本关不掉的警告，防止有人把测试版当正式版用 XD
                BackgroundCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.generic_warning),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.launcher_version_debug_warning, InfoDistributor.LAUNCHER_NAME),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            modifier = Modifier
                                .alpha(0.8f)
                                .align(Alignment.End),
                            text = stringResource(R.string.launcher_version_debug_warning_cant_close),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        when (val state = pageState) {
            is HomePageState.Blank -> {}
            is HomePageState.Loading -> {
                item(key = "homepage_loading_box") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LoadingIndicator()
                            Text(
                                text = stringResource(R.string.settings_launcher_home_page_loading),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
            is HomePageState.None -> {
                customHomePage(
                    blocks = state.page,
                    richTextStyle = richTextStyle,
                    onEvent = onHomePageEvent
                )
            }
        }
    }
}

@Composable
private fun RightMenuContent(
    modifier: Modifier = Modifier,
    onLaunchGame: () -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    onQuickRamClick: () -> Unit,
    onQuickFpsClick: () -> Unit,
    onLogViewerClick: () -> Unit,
    onModsFolderClick: () -> Unit,
    launchButton: @Composable (
        innerModifier: Modifier,
        onClick: () -> Unit,
        text: @Composable RowScope.() -> Unit
    ) -> Unit,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (accountAvatar, shortcutsGrid, versionManagerLayout, launchButton) = createRefs()

        AccountAvatar(
            modifier = Modifier
                .constrainAs(accountAvatar) {
                    top.linkTo(parent.top)
                    bottom.linkTo(shortcutsGrid.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            account = account,
            onClick = toAccountManageScreen
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .constrainAs(shortcutsGrid) {
                    bottom.linkTo(versionManagerLayout.top, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortcutButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_sort,
                onClick = toVersionManageScreen,
                contentDescription = stringResource(R.string.page_title_version_list)
            )
            ShortcutButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_build_filled,
                onClick = onQuickRamClick,
                contentDescription = stringResource(R.string.settings_game_java_memory_title)
            )
            ShortcutButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_video_settings,
                onClick = onQuickFpsClick,
                contentDescription = stringResource(R.string.game_menu_option_switch_fps)
            )
            ShortcutButton(
                modifier = Modifier.weight(1f),
                icon = R.drawable.ic_terminal_outlined,
                onClick = onLogViewerClick,
                contentDescription = stringResource(R.string.versions_overview_log)
            )
        }

        Row(
            modifier = Modifier.constrainAs(versionManagerLayout) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(launchButton.top)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionManagerLayout(
                isRefreshing = isRefreshing,
                version = version,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                swapToVersionManage = toVersionManageScreen
            )
            version?.takeIf { !isRefreshing && it.isValid() }?.let {
                IconButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = toVersionSettingsScreen
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_filled),
                        contentDescription = stringResource(R.string.versions_manage_settings)
                    )
                }
            }
        }

        launchButton(
            Modifier
                .fillMaxWidth()
                .constrainAs(launchButton) {
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
                .padding(PaddingValues(horizontal = 12.dp)),
            {
                onLaunchGame()
            },
            {
                MarqueeText(text = stringResource(R.string.main_launch_game))
            }
        )
    }
}

@Composable
private fun ShortcutButton(
    icon: Int,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(icon),
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
private fun RightMenu(
    isVisible: Boolean,
    onLaunchGame: () -> Unit,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {},
    onQuickRamClick: () -> Unit = {},
    onQuickFpsClick: () -> Unit = {},
    onLogViewerClick: () -> Unit = {},
    onModsFolderClick: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        RightMenuContent(
            modifier = Modifier.fillMaxSize(),
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen,
            onQuickRamClick = onQuickRamClick,
            onQuickFpsClick = onQuickFpsClick,
            onLogViewerClick = onLogViewerClick,
            onModsFolderClick = onModsFolderClick
        ) { innerModifier, onClick, text ->
            ScalingActionButton(
                modifier = innerModifier,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                onClick = onClick,
                content = text
            )
        }
    }
}

@Composable
private fun VersionManagerLayout(
    isRefreshing: Boolean,
    version: Version?,
    modifier: Modifier = Modifier,
    swapToVersionManage: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .clickable(onClick = swapToVersionManage)
            .padding(PaddingValues(all = 8.dp))
    ) {
        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (version == null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    if (version.isValid()) {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}