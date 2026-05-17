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

package com.star1xr.treelauncher.ui.screens.content.settings

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.colorpicker.rememberColorPickerController
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.contract.MediaPickerContract
import com.star1xr.treelauncher.coroutine.Task
import com.star1xr.treelauncher.coroutine.TaskSystem
import com.star1xr.treelauncher.path.PathManager
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.AppLanguage
import com.star1xr.treelauncher.setting.enums.DarkMode
import com.star1xr.treelauncher.setting.enums.MirrorSourceType

import com.star1xr.treelauncher.setting.enums.applyLanguage
import com.star1xr.treelauncher.setting.unit.floatRange
import com.star1xr.treelauncher.ui.base.BaseScreen
import com.star1xr.treelauncher.ui.components.AnimatedColumn
import com.star1xr.treelauncher.ui.components.ColorPickerDialog
import com.star1xr.treelauncher.ui.components.IconTextButton
import com.star1xr.treelauncher.ui.components.OwnOutlinedTextField
import com.star1xr.treelauncher.ui.components.SimpleAlertDialog
import com.star1xr.treelauncher.ui.components.TitleAndSummary
import com.star1xr.treelauncher.ui.components.WarningCard
import com.star1xr.treelauncher.ui.screens.NestedNavKey
import com.star1xr.treelauncher.ui.screens.NormalNavKey
import com.star1xr.treelauncher.ui.screens.TitledNavKey
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.CardPosition
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.EnumSettingsCard
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.SettingsCard
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.star1xr.treelauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.star1xr.treelauncher.ui.theme.ColorThemeType
import com.star1xr.treelauncher.utils.animation.TransitionAnimationType
import com.star1xr.treelauncher.utils.file.shareFile
import com.star1xr.treelauncher.utils.checkStoragePermissions
import com.star1xr.treelauncher.utils.isChinaMainland
import com.star1xr.treelauncher.utils.logging.Logger
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.settings.SettingsTransferUtils
import com.star1xr.treelauncher.utils.string.getMessageOrToString
import com.star1xr.treelauncher.utils.version.VersionTransferUtils
import com.star1xr.treelauncher.game.version.installed.VersionsManager
import com.star1xr.treelauncher.viewmodel.BackgroundViewModel
import com.star1xr.treelauncher.viewmodel.ErrorViewModel
import com.star1xr.treelauncher.viewmodel.EventViewModel
import com.star1xr.treelauncher.viewmodel.LocalBackgroundViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private sealed interface CustomColorOperation {
    data object None : CustomColorOperation
    /** 展示自定义主题颜色 Dialog */
    data object Dialog: CustomColorOperation
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LauncherSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Launcher, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                val importLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            val success = SettingsTransferUtils.importData(context, it)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    if (success) R.string.settings_import_success else R.string.settings_import_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                val importVersionLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            val file = withContext(Dispatchers.IO) {
                                val tempFile = File(context.cacheDir, "import_version.zip")
                                context.contentResolver.openInputStream(it)?.use { input ->
                                    tempFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                tempFile
                            }
                            val success = VersionTransferUtils.importVersion(file)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    Toast.makeText(context, R.string.versions_import_task_finished, Toast.LENGTH_SHORT).show()
                                    VersionsManager.refresh("LauncherSettingsScreen.importVersion")
                                } else {
                                    Toast.makeText(context, R.string.settings_import_failed, Toast.LENGTH_SHORT).show()
                                }
                            }
                            file.delete()
                        }
                    }
                }

                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SettingsCard(
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_export),
                        onClick = {
                            val activity = context as? android.app.Activity ?: return@SettingsCard
                            checkStoragePermissions(
                                activity = activity,
                                title = R.string.storage_permission_request_title,
                                message = context.getString(R.string.storage_permission_request_message),
                                hasPermission = {
                                    coroutineScope.launch {
                                        val file = SettingsTransferUtils.exportSettings(context)
                                        withContext(Dispatchers.Main) {
                                            if (file != null) {
                                                Toast.makeText(context, context.getString(R.string.settings_export_success, file.absolutePath), Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, R.string.settings_export_failed, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    )
                    SettingsCard(
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_import),
                        onClick = {
                            importLauncher.launch("application/json")
                        }
                    )
                    SettingsCard(
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.versions_import_version),
                        summary = stringResource(R.string.versions_import_version_summary),
                        onClick = {
                            importVersionLauncher.launch("application/zip")
                        }
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    var customColorOperation by remember { mutableStateOf<CustomColorOperation>(CustomColorOperation.None) }
                    CustomColorOperation(
                        customColorOperation = customColorOperation,
                        updateOperation = { customColorOperation = it }
                    )

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.launcherColorTheme,
                        title = stringResource(R.string.settings_launcher_color_theme_title),
                        summary = stringResource(R.string.settings_launcher_color_theme_summary),
                        entries = ColorThemeType.entries,
                        getRadioEnable = { enum ->
                            if (enum == ColorThemeType.DYNAMIC) Build.VERSION.SDK_INT >= Build.VERSION_CODES.S else true
                        },
                        getRadioText = { enum ->
                            when (enum) {
                                ColorThemeType.DYNAMIC -> stringResource(R.string.theme_color_dynamic)
                                ColorThemeType.EMBERMIRE -> stringResource(R.string.theme_color_embermire)
                                ColorThemeType.VELVET_ROSE -> stringResource(R.string.theme_color_velvet_rose)
                                ColorThemeType.MISTWAVE -> stringResource(R.string.theme_color_mistwave)
                                ColorThemeType.GLACIER -> stringResource(R.string.theme_color_glacier)
                                ColorThemeType.VERDANTFIELD -> stringResource(R.string.theme_color_verdant_field)
                                ColorThemeType.URBAN_ASH -> stringResource(R.string.theme_color_urban_ash)
                                ColorThemeType.VERDANT_DAWN -> stringResource(R.string.theme_color_verdant_dawn)
                                ColorThemeType.CUSTOM -> stringResource(R.string.generic_custom)
                            }
                        },
                        maxItemsInEachRow = 5,
                        onRadioClick = { enum ->
                            if (enum == ColorThemeType.CUSTOM) customColorOperation = CustomColorOperation.Dialog
                        }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherDarkMode,
                        items = DarkMode.entries,
                        title = stringResource(R.string.settings_launcher_dark_mode_title),
                        getItemText = { stringResource(it.textRes) }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherLanguage,
                        items = AppLanguage.entries,
                        title = stringResource(R.string.settings_launcher_language),
                        getItemText = { stringResource(it.textRes) },
                        onValueChange = {
                            applyLanguage(it)
                        }
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherFestivalEffects,
                        title = stringResource(R.string.settings_launcher_festivals_effects_title),
                        summary = stringResource(R.string.settings_launcher_festivals_effects_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.launcherFullScreen,
                        title = stringResource(R.string.settings_launcher_full_screen_title),
                        summary = stringResource(R.string.settings_launcher_full_screen_summary)
                    )
                }
            }

            //启动器背景设置板块
            LocalBackgroundViewModel.current?.let { backgroundViewModel ->
                AnimatedItem(scope) { yOffset ->
                    SettingsCardColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                    ) {
                        SettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top
                        ) {
                            CustomBackground(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundViewModel = backgroundViewModel,
                                submitError = submitError
                            )
                        }

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.launcherBackgroundOpacity,
                            title = stringResource(R.string.settings_launcher_background_opacity_title),
                            summary = stringResource(R.string.settings_launcher_background_opacity_summary),
                            valueRange = AllSettings.launcherBackgroundOpacity.floatRange,
                            suffix = "%",
                            enabled = backgroundViewModel.isValid,
                            fineTuningControl = true
                        )

                        IntSliderSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Bottom,
                            unit = AllSettings.videoBackgroundVolume,
                            title = stringResource(R.string.settings_launcher_background_video_volume_title),
                            summary = stringResource(R.string.settings_launcher_background_video_volume_summary),
                            valueRange = AllSettings.videoBackgroundVolume.floatRange,
                            suffix = "%",
                            enabled = backgroundViewModel.isValid && backgroundViewModel.isVideo,
                            fineTuningControl = true
                        )
                    }
                }
            }

            //动画设置板块
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.launcherAnimateSpeed,
                        title = stringResource(R.string.settings_launcher_animate_speed_title),
                        summary = stringResource(R.string.settings_launcher_animate_speed_summary),
                        valueRange = AllSettings.launcherAnimateSpeed.floatRange,
                        suffix = "x"
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.launcherAnimateExtent,
                        title = stringResource(R.string.settings_launcher_animate_extent_title),
                        summary = stringResource(R.string.settings_launcher_animate_extent_summary),
                        valueRange = AllSettings.launcherAnimateExtent.floatRange,
                        suffix = "x"
                    )

                    EnumSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.launcherSwapAnimateType,
                        title = stringResource(R.string.settings_launcher_swap_animate_type_title),
                        summary = stringResource(R.string.settings_launcher_swap_animate_type_summary),
                        entries = TransitionAnimationType.entries,
                        getRadioEnable = { true },
                        getRadioText = { enum ->
                            stringResource(enum.textRes)
                        }
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    //这些镜像源都是为了改善中国大陆内陆的网络环境而存在的
                    //境外不需要这些镜像源，反而可能拖慢境外的下载速度
                    //所以不应该向中国境外开放这些选项
                    val isChinaMainland = remember { isChinaMainland() }
                    if (isChinaMainland) {
                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Top,
                            unit = AllSettings.fetchModLoaderSource,
                            items = MirrorSourceType.entries,
                            title = stringResource(R.string.settings_launcher_mirror_modloader_title),
                            getItemText = { stringResource(it.textRes) }
                        )

                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.fileDownloadSource,
                            items = MirrorSourceType.entries,
                            title = stringResource(R.string.settings_launcher_mirror_file_download_title),
                            getItemText = { stringResource(it.textRes) }
                        )

                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.assetSearchSource,
                            items = MirrorSourceType.entries,
                            title = stringResource(R.string.settings_launcher_mirror_assets_search_title),
                            getItemText = { stringResource(it.textRes) }
                        )

                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.assetDownloadSource,
                            items = MirrorSourceType.entries,
                            title = stringResource(R.string.settings_launcher_mirror_assets_download_title),
                            getItemText = { stringResource(it.textRes) }
                        )
                    }

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = if (isChinaMainland) {
                            CardPosition.Middle
                        } else {
                            CardPosition.Top
                        },
                        unit = AllSettings.launcherLogRetentionDays,
                        title = stringResource(R.string.settings_launcher_log_retention_days_title),
                        summary = stringResource(R.string.settings_launcher_log_retention_days_summary),
                        valueRange = AllSettings.launcherLogRetentionDays.floatRange,
                        suffix = stringResource(R.string.unit_day)
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_launcher_log_share_title),
                        summary = stringResource(R.string.settings_launcher_log_share_summary),
                        onClick = {
                            TaskSystem.submitTask(
                                Task.runTask(
                                    id = "ZIP_LOGS",
                                    task = { task ->
                                        task.updateProgress(-1f, R.string.settings_launcher_log_share_packing)
                                        val logsFile = File(PathManager.DIR_CACHE, "logs.zip")
                                        Logger.pack(logsFile)
                                        task.updateProgress(1f, null)
                                        //分享压缩包
                                        shareFile(
                                            context = context,
                                            file = logsFile
                                        )
                                    },
                                    onError = { e ->
                                        lError("Failed to package log files.", e)
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomColorOperation(
    customColorOperation: CustomColorOperation,
    updateOperation: (CustomColorOperation) -> Unit
) {
    when (customColorOperation) {
        is CustomColorOperation.None -> {}
        is CustomColorOperation.Dialog -> {
            var tempColor by remember {
                mutableStateOf(Color(AllSettings.launcherCustomColor.getValue()))
            }
            val colorController = rememberColorPickerController(initialColor = tempColor)

            val currentColor by remember(colorController) { colorController.color }

            ColorPickerDialog(
                colorController = colorController,
                onChangeFinished = {
                    AllSettings.launcherCustomColor.updateState(currentColor.toArgb())
                },
                onCancel = {
                    //还原颜色
                    AllSettings.launcherCustomColor.updateState(colorController.getOriginalColor().toArgb())
                    updateOperation(CustomColorOperation.None)
                },
                onConfirm = { selectedColor ->
                    AllSettings.launcherCustomColor.save(selectedColor.toArgb())
                    updateOperation(CustomColorOperation.None)
                },
                showAlpha = false
            )
        }
    }
}

private sealed interface BackgroundOperation {
    data object None : BackgroundOperation
    data object PreReset : BackgroundOperation
    data object Reset : BackgroundOperation
}

@Composable
private fun CustomBackground(
    backgroundViewModel: BackgroundViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var operation by remember { mutableStateOf<BackgroundOperation>(BackgroundOperation.None) }

    BackgroundOperation(
        operation = operation,
        changeOperation = { operation = it },
        backgroundViewModel = backgroundViewModel
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = MediaPickerContract(
            allowImages = true,
            allowVideos = true,
            allowMultiple = false
        )
    ) { result ->
        if (result != null) {
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateMessage(R.string.settings_launcher_background_importing)
                        backgroundViewModel.import(context, result[0] /* 取决于上面的allowMultiple，此处一定会是单个元素的列表 */)
                    },
                    onError = { th ->
                        backgroundViewModel.delete()
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.error_import_image),
                                message = th.getMessageOrToString()
                            )
                        )
                    }
                )
            )
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { filePicker.launch(Unit) }
                .padding(all = 16.dp),
        ) {
            TitleAndSummary(
                title = stringResource(R.string.settings_launcher_background_title),
                summary = stringResource(R.string.settings_launcher_background_summary),
            )
        }

        AnimatedVisibility(
            modifier = Modifier.padding(horizontal = 16.dp),
            visible = backgroundViewModel.isValid
        ) {
            IconTextButton(
                painter = painterResource(R.drawable.ic_restart_alt),
                text = stringResource(R.string.generic_reset),
                onClick = {
                    if (operation == BackgroundOperation.None) {
                        operation = BackgroundOperation.PreReset
                    }
                }
            )
        }
    }
}

@Composable
private fun BackgroundOperation(
    operation: BackgroundOperation,
    changeOperation: (BackgroundOperation) -> Unit,
    backgroundViewModel: BackgroundViewModel
) {
    when (operation) {
        is BackgroundOperation.None -> {}
        is BackgroundOperation.PreReset -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.settings_launcher_background_reset_message),
                onConfirm = {
                    changeOperation(BackgroundOperation.Reset)
                },
                onDismiss = {
                    changeOperation(BackgroundOperation.None)
                }
            )
        }
        is BackgroundOperation.Reset -> {
            LaunchedEffect(Unit) {
                backgroundViewModel.delete()
                changeOperation(BackgroundOperation.None)
            }
        }
    }
}