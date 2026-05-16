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

package com.movtery.zalithlauncher.ui.screens.game.elements

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.components.DualMenuSubscreen
import com.movtery.zalithlauncher.ui.components.MenuListLayout
import com.movtery.zalithlauncher.ui.components.MenuSliderLayout
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.MenuSwitchButton
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.control.HotbarRule
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.cardTitleColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel

private data class IconTab(val iconRes: Int, val iconSize: Dp = 18.dp)

private val controlTabs = listOf(
    //概览
    IconTab(R.drawable.ic_dashboard_filled),
    //虚拟鼠标设置
    IconTab(R.drawable.ic_mouse_filled, iconSize = 16.dp),
    //手柄设置
    IconTab(R.drawable.ic_sports_esports_filled),
    //手势控制设置
    IconTab(R.drawable.ic_touch_app_filled),
    //陀螺仪设置
    IconTab(R.drawable.ic_mobile_rotate_filled)
)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import com.movtery.zalithlauncher.ui.components.IntegratedMenuSubscreen

private enum class GameMenuCategory(val iconRes: Int, val titleRes: Int) {
    GENERAL(R.drawable.ic_settings_filled, R.string.generic_all),
    VISUAL(R.drawable.ic_video_settings, R.string.settings_tab_renderer),
    CONTROLS(R.drawable.ic_videogame_asset_outlined, R.string.settings_tab_control),
    LAYOUT(R.drawable.ic_dashboard_filled, R.string.control_manage_info_edit)
}

@Composable
fun GameMenuSubscreen(
    state: MenuState,
    controlMenuTabIndex: Int,
    onControlMenuTabChange: (Int) -> Unit,
    gamepadViewModel: GamepadViewModel,
    closeScreen: () -> Unit,
    onForceClose: () -> Unit,
    onSwitchLog: () -> Unit,
    enableTerracotta: Boolean,
    onOpenTerracottaMenu: () -> Unit,
    onRefreshWindowSize: () -> Unit,
    onInputMethod: () -> Unit,
    onSendKeycode: () -> Unit,
    onReplacementControl: () -> Unit,
    onManageJoystick: () -> Unit,
    onEditLayout: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(GameMenuCategory.GENERAL) }

    IntegratedMenuSubscreen(
        state = state,
        closeScreen = closeScreen,
        sidebarContent = {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(R.string.game_menu_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            GameMenuCategory.entries.forEach { category ->
                NavigationRailItem(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    icon = {
                        Icon(
                            painter = painterResource(category.iconRes),
                            contentDescription = stringResource(category.titleRes)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        },
        content = {
            Text(
                text = stringResource(selectedCategory.titleRes),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                when (selectedCategory) {
                    GameMenuCategory.GENERAL -> {
                        GeneralContent(
                            onForceClose = onForceClose,
                            onSwitchLog = onSwitchLog,
                            enableTerracotta = enableTerracotta,
                            onOpenTerracottaMenu = onOpenTerracottaMenu
                        )
                    }
                    GameMenuCategory.VISUAL -> {
                        VisualContent(
                            onRefreshWindowSize = onRefreshWindowSize
                        )
                    }
                    GameMenuCategory.CONTROLS -> {
                        val pagerState = rememberPagerState(pageCount = { controlTabs.size - 1 }) // Exclude overview

                        LaunchedEffect(controlMenuTabIndex) {
                            if (controlMenuTabIndex > 0) {
                                pagerState.animateScrollToPage(controlMenuTabIndex - 1)
                            }
                        }

                        Column {
                            SecondaryScrollableTabRow(
                                selectedTabIndex = (controlMenuTabIndex - 1).coerceAtLeast(0),
                                edgePadding = 0.dp,
                                minTabWidth = 58.dp,
                                containerColor = Color.Transparent,
                            ) {
                                controlTabs.drop(1).forEachIndexed { index, iconTab ->
                                    Tab(
                                        selected = index == (controlMenuTabIndex - 1),
                                        onClick = {
                                            onControlMenuTabChange(index + 1)
                                        },
                                        icon = {
                                            Icon(
                                                modifier = Modifier.size(iconTab.iconSize),
                                                painter = painterResource(iconTab.iconRes),
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                when (page) {
                                    0 -> ControlMouse(modifier = Modifier.fillMaxSize())
                                    1 -> ControlGamepad(
                                        modifier = Modifier.fillMaxSize(),
                                        gamepadViewModel = gamepadViewModel
                                    )
                                    2 -> ControlGesture(modifier = Modifier.fillMaxSize())
                                    3 -> ControlGyroscope(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                    GameMenuCategory.LAYOUT -> {
                        ControlOverview(
                            modifier = Modifier.fillMaxSize(),
                            closeScreen = closeScreen,
                            onInputMethod = onInputMethod,
                            onSendKeycode = onSendKeycode,
                            onReplacementControl = onReplacementControl,
                            onManageJoystick = onManageJoystick,
                            onEditLayout = onEditLayout
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun GeneralContent(
    onForceClose: () -> Unit,
    onSwitchLog: () -> Unit,
    enableTerracotta: Boolean,
    onOpenTerracottaMenu: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_button_force_close),
                onClick = onForceClose,
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_log),
                onClick = onSwitchLog,
                color = color,
                contentColor = contentColor,
            )
        }

        if (enableTerracotta) {
            item {
                MenuTextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.terracotta_menu),
                    onClick = onOpenTerracottaMenu,
                    color = color,
                    contentColor = contentColor,
                )
            }
        }
    }
}

@Composable
private fun VisualContent(
    onRefreshWindowSize: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_show_menu),
                switch = AllSettings.showMenuBall.state,
                onSwitch = { value -> AllSettings.showMenuBall.save(value) },
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_menu_ball_opacity),
                value = AllSettings.menuBallOpacity.state,
                valueRange = AllSettings.menuBallOpacity.floatRange,
                onValueChange = { AllSettings.menuBallOpacity.updateState(it) },
                onValueChangeFinished = { AllSettings.menuBallOpacity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
                enabled = AllSettings.showMenuBall.state
            )
        }
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_fps),
                switch = AllSettings.showFPS.state,
                onSwitch = { AllSettings.showFPS.save(it) },
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_switch_memory),
                switch = AllSettings.showMemory.state,
                onSwitch = { AllSettings.showMemory.save(it) },
                color = color,
                contentColor = contentColor,
            )
        }
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_renderer_resolution_scale_title),
                value = AllSettings.resolutionRatio.state,
                valueRange = AllSettings.resolutionRatio.floatRange,
                onValueChange = { AllSettings.resolutionRatio.updateState(it) },
                onValueChangeFinished = { 
                    AllSettings.resolutionRatio.save(it)
                    onRefreshWindowSize()
                },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun ControlOverview(
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
    closeScreen: () -> Unit,
    onInputMethod: () -> Unit,
    onSendKeycode: () -> Unit,
    onReplacementControl: () -> Unit,
    onManageJoystick: () -> Unit,
    onEditLayout: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //切换输入法
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_input_method),
                onClick = {
                    onInputMethod()
                    closeScreen()
                },
                color = color,
                contentColor = contentColor,
            )
        }
        //发送键值
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_send_keycode),
                onClick = {
                    onSendKeycode()
                    closeScreen()
                },
                color = color,
                contentColor = contentColor,
            )
        }
        //更换控制布局
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_menu_option_replacement_control),
                onClick = {
                    onReplacementControl()
                    closeScreen()
                },
                color = color,
                contentColor = contentColor,
            )
        }
        //编辑布局
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.control_manage_info_edit),
                onClick = {
                    onEditLayout()
                    closeScreen()
                },
                color = color,
                contentColor = contentColor,
            )
        }
        //控制布局不透明度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_controls_opacity),
                value = AllSettings.controlsOpacity.state,
                valueRange = AllSettings.controlsOpacity.floatRange,
                onValueChange = { AllSettings.controlsOpacity.updateState(it) },
                onValueChangeFinished = { AllSettings.controlsOpacity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //管理摇杆
        item {
            MenuTextButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.game_styles_joystick),
                onClick = {
                    onManageJoystick()
                    closeScreen()
                },
                color = color,
                contentColor = contentColor,
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        //快捷栏定位规则
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_hotbar_rule),
                items = HotbarRule.entries,
                currentItem = AllSettings.hotbarRule.state,
                onItemChange = { AllSettings.hotbarRule.save(it) },
                getItemText = { stringResource(it.nameRes) },
                color = color,
                contentColor = contentColor,
            )
        }

        //快捷栏宽度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_hotbar_width),
                value = AllSettings.hotbarWidth.state / 10f,
                valueRange = 0f..100f,
                enabled = AllSettings.hotbarRule.state == HotbarRule.Custom,
                onValueChange = { value ->
                    AllSettings.hotbarWidth.updateState((value * 10f).toInt())
                },
                onValueChangeFinished = { value ->
                    AllSettings.hotbarWidth.save((value * 10f).toInt())
                },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //快捷栏高度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.game_menu_option_hotbar_height),
                value = AllSettings.hotbarHeight.state / 10f,
                valueRange = 0f..100f,
                enabled = AllSettings.hotbarRule.state == HotbarRule.Custom,
                onValueChange = { value ->
                    AllSettings.hotbarHeight.updateState((value * 10f).toInt())
                },
                onValueChangeFinished = { value ->
                    AllSettings.hotbarHeight.save((value * 10f).toInt())
                },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun ControlMouse(
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //隐藏虚拟鼠标
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_mouse_hide_title),
                switch = AllSettings.hideMouse.state,
                onSwitch = { AllSettings.hideMouse.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = AllSettings.mouseControlMode.state == MouseControlMode.CLICK
            )
        }
        //触控板式操作
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_mouse_enable_click_title),
                switch = AllSettings.enableMouseClick.state,
                onSwitch = { AllSettings.enableMouseClick.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = AllSettings.mouseControlMode.state == MouseControlMode.SLIDE
            )
        }
        //鼠标控制模式
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_mouse_control_mode_title),
                items = MouseControlMode.entries,
                currentItem = AllSettings.mouseControlMode.state,
                onItemChange = { AllSettings.mouseControlMode.save(it) },
                getItemText = { stringResource(it.nameRes) },
                color = color,
                contentColor = contentColor,
            )
        }
        //虚拟鼠标大小
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_mouse_size_title),
                value = AllSettings.mouseSize.state,
                valueRange = AllSettings.mouseSize.floatRange,
                onValueChange = { AllSettings.mouseSize.updateState(it) },
                onValueChangeFinished = { AllSettings.mouseSize.save(it) },
                suffix = "Dp",
                color = color,
                contentColor = contentColor,
            )
        }
        //虚拟鼠标灵敏度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_mouse_sensitivity_title),
                value = AllSettings.cursorSensitivity.state,
                valueRange = AllSettings.cursorSensitivity.floatRange,
                onValueChange = { AllSettings.cursorSensitivity.updateState(it) },
                onValueChangeFinished = { AllSettings.cursorSensitivity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }
        //抓获鼠标滑动灵敏度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_mouse_capture_sensitivity_title),
                value = AllSettings.mouseCaptureSensitivity.state,
                valueRange = AllSettings.mouseCaptureSensitivity.floatRange,
                onValueChange = { AllSettings.mouseCaptureSensitivity.updateState(it) },
                onValueChangeFinished = { AllSettings.mouseCaptureSensitivity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }
        //虚拟鼠标长按触发的延迟
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_mouse_long_press_delay_title),
                value = AllSettings.mouseLongPressDelay.state,
                valueRange = AllSettings.mouseLongPressDelay.floatRange,
                onValueChange = { AllSettings.mouseLongPressDelay.updateState(it) },
                onValueChangeFinished = { AllSettings.mouseLongPressDelay.save(it) },
                suffix = "ms",
                color = color,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun ControlGamepad(
    gamepadViewModel: GamepadViewModel,
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //手柄控制总开关
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_gamepad_title),
                switch = AllSettings.gamepadControl.state,
                onSwitch = { AllSettings.gamepadControl.save(it) },
                color = color,
                contentColor = contentColor,
            )
        }

        //手柄死区缩放
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_gamepad_deadzone_title),
                value = AllSettings.gamepadDeadZoneScale.state,
                valueRange = AllSettings.gamepadDeadZoneScale.floatRange,
                enabled = AllSettings.gamepadControl.state,
                onValueChange = { AllSettings.gamepadDeadZoneScale.updateState(it) },
                onValueChangeFinished = { AllSettings.gamepadDeadZoneScale.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //摇杆指针灵敏度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_gamepad_cursor_sensitivity_title),
                value = AllSettings.gamepadCursorSensitivity.state,
                valueRange = AllSettings.gamepadCursorSensitivity.floatRange,
                enabled = AllSettings.gamepadControl.state,
                onValueChange = { AllSettings.gamepadCursorSensitivity.updateState(it) },
                onValueChangeFinished = { AllSettings.gamepadCursorSensitivity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //摇杆视角灵敏度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_gamepad_camera_sensitivity_title),
                value = AllSettings.gamepadCameraSensitivity.state,
                valueRange = AllSettings.gamepadCameraSensitivity.floatRange,
                enabled = AllSettings.gamepadControl.state,
                onValueChange = { AllSettings.gamepadCameraSensitivity.updateState(it) },
                onValueChangeFinished = { AllSettings.gamepadCameraSensitivity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //手柄映射配置切换
        item {
            val list = remember(gamepadViewModel) {
                gamepadViewModel.getAllConfigKeys()
            }

            if (list.isNotEmpty()) {
                MenuListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_gamepad_config_title),
                    items = list,
                    currentItem = AllSettings.gamepadMappingConfig.state,
                    onItemChange = { name ->
                        AllSettings.gamepadMappingConfig.save(name)
                        gamepadViewModel.reloadAllMappings()
                    },
                    getItemText = { it },
                    color = color,
                    contentColor = contentColor,
                    enabled = AllSettings.gamepadControl.state,
                )
            } else {
                MenuTextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.settings_gamepad_config_no_items),
                    onClick = {},
                    color = color,
                    contentColor = contentColor,
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun ControlGesture(
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //手势控制
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_gesture_control_title),
                switch = AllSettings.gestureControl.state,
                onSwitch = { AllSettings.gestureControl.save(it) },
                color = color,
                contentColor = contentColor,
            )
        }

        //点击触发的操作类型
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gesture_tap_action_title),
                items = GestureActionType.entries,
                currentItem = AllSettings.gestureTapMouseAction.state,
                onItemChange = { AllSettings.gestureTapMouseAction.save(it) },
                getItemText = { stringResource(it.nameRes) },
                color = color,
                contentColor = contentColor,
                enabled = AllSettings.gestureControl.state
            )
        }

        //长按触发的操作类型
        item {
            MenuListLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gesture_long_press_action_title),
                items = GestureActionType.entries,
                currentItem = AllSettings.gestureLongPressMouseAction.state,
                onItemChange = { AllSettings.gestureLongPressMouseAction.save(it) },
                getItemText = { stringResource(it.nameRes) },
                color = color,
                contentColor = contentColor,
                enabled = AllSettings.gestureControl.state
            )
        }

        //手势长按触发的延迟
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gesture_long_press_delay_title),
                value = AllSettings.gestureLongPressDelay.state,
                valueRange = AllSettings.gestureLongPressDelay.floatRange,
                enabled = AllSettings.gestureControl.state,
                onValueChange = { AllSettings.gestureLongPressDelay.updateState(it) },
                onValueChangeFinished = { AllSettings.gestureLongPressDelay.save(it) },
                suffix = "ms",
                color = color,
                contentColor = contentColor,
            )
        }
    }
}

@Composable
private fun ControlGyroscope(
    modifier: Modifier = Modifier,
    color: Color = cardColor(false),
    contentColor: Color = onCardColor(),
) {
    val context = LocalContext.current
    val isGyroscopeAvailable = remember(context) {
        isGyroscopeAvailable(context = context)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //陀螺仪控制
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_gyroscope_title),
                switch = AllSettings.gyroscopeControl.state,
                onSwitch = { AllSettings.gyroscopeControl.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = isGyroscopeAvailable
            )
        }

        //陀螺仪控制灵敏度
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gyroscope_sensitivity_title),
                value = AllSettings.gyroscopeSensitivity.state,
                valueRange = AllSettings.gyroscopeSensitivity.floatRange,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                onValueChange = { AllSettings.gyroscopeSensitivity.updateState(it) },
                onValueChangeFinished = { AllSettings.gyroscopeSensitivity.save(it) },
                suffix = "%",
                color = color,
                contentColor = contentColor,
            )
        }

        //陀螺仪采样率
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gyroscope_sample_rate_title),
                value = AllSettings.gyroscopeSampleRate.state,
                valueRange = AllSettings.gyroscopeSampleRate.floatRange,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state,
                onValueChange = { AllSettings.gyroscopeSampleRate.updateState(it) },
                onValueChangeFinished = { AllSettings.gyroscopeSampleRate.save(it) },
                suffix = "ms",
                color = color,
                contentColor = contentColor,
            )
        }

        //陀螺仪数值平滑
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_gyroscope_smoothing_title),
                switch = AllSettings.gyroscopeSmoothing.state,
                onSwitch = { AllSettings.gyroscopeSmoothing.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
            )
        }

        //陀螺仪平滑处理的窗口大小
        item {
            MenuSliderLayout(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.settings_control_gyroscope_smoothing_window_title),
                value = AllSettings.gyroscopeSmoothingWindow.state,
                valueRange = AllSettings.gyroscopeSmoothingWindow.floatRange,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state && AllSettings.gyroscopeSmoothing.state,
                onValueChange = { AllSettings.gyroscopeSmoothingWindow.updateState(it) },
                onValueChangeFinished = { AllSettings.gyroscopeSmoothingWindow.save(it) },
                color = color,
                contentColor = contentColor,
            )
        }

        //反转 X 轴
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_gyroscope_invert_x_title),
                switch = AllSettings.gyroscopeInvertX.state,
                onSwitch = { AllSettings.gyroscopeInvertX.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
            )
        }

        //反转 Y 轴
        item {
            MenuSwitchButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_control_gyroscope_invert_y_title),
                switch = AllSettings.gyroscopeInvertY.state,
                onSwitch = { AllSettings.gyroscopeInvertY.save(it) },
                color = color,
                contentColor = contentColor,
                enabled = isGyroscopeAvailable && AllSettings.gyroscopeControl.state
            )
        }
    }
}