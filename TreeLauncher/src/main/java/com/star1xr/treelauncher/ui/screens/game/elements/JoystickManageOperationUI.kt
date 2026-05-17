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

package com.star1xr.treelauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.observable.ObservableJoystickStyle
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.unit.floatRange
import com.star1xr.treelauncher.ui.components.MarqueeText
import com.star1xr.treelauncher.ui.components.fadeEdge
import com.star1xr.treelauncher.ui.screens.content.elements.DisabledAlpha
import com.star1xr.treelauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.star1xr.treelauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.star1xr.treelauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.star1xr.treelauncher.ui.screens.main.control_editor.edit_joystick.EditJoystickStyleDialog
import com.star1xr.treelauncher.ui.screens.main.control_editor.edit_joystick.EditJoystickStyleMode
import com.star1xr.treelauncher.ui.screens.rememberSwapTween
import com.star1xr.treelauncher.ui.theme.cardColor
import com.star1xr.treelauncher.ui.theme.onCardColor

/** 启动器默认摇杆管理操作状态 */
sealed interface JoystickManageOperation {
    data object None : JoystickManageOperation
    /** 打开摇杆管理页面 */
    data object Manage : JoystickManageOperation
    /** 打开摇杆样式编辑页面 */
    data object Edit : JoystickManageOperation
}

@Composable
fun JoystickManageOperation(
    operation: JoystickManageOperation,
    onChanged: (JoystickManageOperation) -> Unit,
    launcherJoystick: ObservableJoystickStyle,
    onSaveStyle: () -> Unit
) {
    //摇杆管理Dialog
    JoystickManageDialog(
        visible = operation == JoystickManageOperation.Manage,
        onDismissRequest = {
            onChanged(JoystickManageOperation.None)
        },
        onEditStyle = {
            onChanged(JoystickManageOperation.Edit)
        }
    )

    //摇杆样式编辑Dialog
    EditJoystickStyleDialog(
        visible = operation == JoystickManageOperation.Edit,
        style = launcherJoystick,
        mode = EditJoystickStyleMode.Launcher,
        onClose = {
            onChanged(JoystickManageOperation.None)
        },
        onInfoButtonClick = onSaveStyle
    )
}

/**
 * 默认摇杆移动组件属性管理对话框
 * **不再真正使用Dialog，真的会有性能问题！**
 */
@Composable
private fun JoystickManageDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onEditStyle: () -> Unit,
) {
    val tween = rememberSwapTween()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visible,
        enter = fadeIn(animationSpec = tween),
        exit = fadeOut(animationSpec = tween)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //作为背景层，被点击时关闭Dialog
            if (visible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismissRequest
                        )
                )
            }

            /** 设置属性时降低对话框透明度进行预览 */
            var enableAlpha by remember { mutableStateOf(false) }
            val alpha by animateFloatAsState(
                targetValue = if (enableAlpha) DisabledAlpha else 1.0f
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.65f)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 3.dp),
                    shadowElevation = 3.dp,
                    color = cardColor(false),
                    contentColor = onCardColor(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(all = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MarqueeText(
                            text = stringResource(R.string.game_styles_joystick),
                            style = MaterialTheme.typography.titleMedium
                        )

                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fadeEdge(state = scrollState)
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .verticalScroll(state = scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            //启用摇杆
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_enable),
                                value = AllSettings.enableJoystickControl.state,
                                onValueChange = { AllSettings.enableJoystickControl.save(it) }
                            )

                            //x
                            InfoLayoutSliderItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_x),
                                value = AllSettings.joystickControlX.state / 100f,
                                onValueChange = {
                                    AllSettings.joystickControlX.updateState((it * 100).toInt())
                                    enableAlpha = true
                                },
                                valueRange = 0f..100f,
                                onValueChangeFinished = {
                                    AllSettings.joystickControlX.save()
                                    enableAlpha = false
                                },
                                decimalFormat = "#0.00",
                                suffix = "%",
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //y
                            InfoLayoutSliderItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_y),
                                value = AllSettings.joystickControlY.state / 100f,
                                onValueChange = {
                                    AllSettings.joystickControlY.updateState((it * 100).toInt())
                                    enableAlpha = true
                                },
                                valueRange = 0f..100f,
                                onValueChangeFinished = {
                                    AllSettings.joystickControlY.save()
                                    enableAlpha = false
                                },
                                decimalFormat = "#0.00",
                                suffix = "%",
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //大小
                            InfoLayoutSliderItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_size),
                                value = AllSettings.joystickControlSize.state.toFloat(),
                                onValueChange = {
                                    AllSettings.joystickControlSize.updateState(it.toInt())
                                    enableAlpha = true
                                },
                                valueRange = AllSettings.joystickControlSize.floatRange,
                                onValueChangeFinished = {
                                    AllSettings.joystickControlSize.save()
                                    enableAlpha = false
                                },
                                decimalFormat = "#0",
                                suffix = "dp",
                                fineTuningStep = 1.0f,
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            Spacer(Modifier)

                            //编辑摇杆样式
                            InfoLayoutTextItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_edit),
                                onClick = onEditStyle
                            )

                            //使用控制布局提供的样式
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_edit_use_control_layout),
                                value = AllSettings.joystickUseStyleByLayout.state,
                                onValueChange = {
                                    AllSettings.joystickUseStyleByLayout.save(it)
                                },
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            Spacer(Modifier)

                            //在实体鼠标操作时隐藏
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_hide_when_mouse),
                                value = AllSettings.joystickHideWhenMouse.state,
                                onValueChange = {
                                    AllSettings.joystickHideWhenMouse.save(it)
                                },
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //在手柄操作时隐藏
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_hide_when_gamepad),
                                value = AllSettings.joystickHideWhenGamepad.state,
                                onValueChange = {
                                    AllSettings.joystickHideWhenGamepad.save(it)
                                },
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            Spacer(Modifier)

                            //摇杆死区范围
                            InfoLayoutSliderItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_deadzone),
                                value = AllSettings.joystickDeadZoneRatio.state.toFloat(),
                                onValueChange = { AllSettings.joystickDeadZoneRatio.updateState(it.toInt()) },
                                onValueChangeFinished = { AllSettings.joystickDeadZoneRatio.save() },
                                valueRange = AllSettings.joystickDeadZoneRatio.floatRange,
                                decimalFormat = "#0",
                                suffix = "%",
                                fineTuningStep = 1.0f,
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //摇杆前进锁判定范围
                            InfoLayoutSliderItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_lock_threshold),
                                value = AllSettings.joystickLockThreshold.state.toFloat(),
                                onValueChange = { AllSettings.joystickLockThreshold.updateState(it.toInt()) },
                                onValueChangeFinished = { AllSettings.joystickLockThreshold.save() },
                                valueRange = AllSettings.joystickLockThreshold.floatRange,
                                decimalFormat = "#0",
                                suffix = "%",
                                fineTuningStep = 1.0f,
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //前进锁定
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_can_lock),
                                value = AllSettings.joystickControlCanLock.state,
                                onValueChange = {
                                    AllSettings.joystickControlCanLock.save(it)
                                },
                                enabled = AllSettings.enableJoystickControl.state
                            )

                            //锁定时强制疾跑
                            InfoLayoutSwitchItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = stringResource(R.string.game_styles_joystick_lock_sprint),
                                value = AllSettings.joystickControlLockSpring.state,
                                onValueChange = {
                                    AllSettings.joystickControlLockSpring.save(it)
                                },
                                enabled = AllSettings.enableJoystickControl.state && AllSettings.joystickControlCanLock.state
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onDismissRequest
                            ) {
                                MarqueeText(text = stringResource(R.string.generic_close))
                            }
                        }
                    }
                }
            }
        }
    }
}