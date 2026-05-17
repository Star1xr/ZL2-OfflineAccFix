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

package com.star1xr.treelauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.observable.DefaultObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableSpecial
import com.movtery.layer_controller.utils.widgetPosition
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.setting.enums.isLauncherInDarkTheme
import com.star1xr.treelauncher.ui.components.rememberBoxSize
import com.star1xr.treelauncher.ui.control.joystick.StyleableJoystick
import com.star1xr.treelauncher.ui.control.mouse.SwitchableMouseLayout

/**
 * 预览控制布局层
 * @param observableLayout 被预览的控制布局
 * @param previewScenario 控制布局预览的场景
 * @param previewHideLayerWhen  控制布局预览时，模拟当前使用的设备
 *                              控件层会根据该值决定是否隐藏
 */
@Composable
fun BoxWithConstraintsScope.PreviewControlBox(
    observableLayout: ObservableControlLayout,
    previewScenario: PreviewScenario,
    previewHideLayerWhen: HideLayerWhen,
    enableJoystick: Boolean,
    modifier: Modifier = Modifier,
) {
    val occupiedPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }
    val moveOnlyPointers = remember(observableLayout) { mutableStateSetOf<PointerId>() }

    val screenSize = rememberBoxSize()

    ControlBoxLayout(
        modifier = modifier.fillMaxSize(),
        observedLayout = observableLayout,
        checkOccupiedPointers = { occupiedPointers.contains(it) },
        markPointerAsMoveOnly = { moveOnlyPointers.add(it) },
        isUsingJoystick = previewScenario.isCursorGrabbing && enableJoystick,
        isCursorGrabbing = previewScenario.isCursorGrabbing,
        hideLayerWhen = previewHideLayerWhen,
        isDark = isLauncherInDarkTheme()
    ) {
        PreviewMouseLayout(
            modifier = Modifier.fillMaxSize(),
            screenSize = screenSize,
            isMoveOnlyPointer = { moveOnlyPointers.contains(it) },
            onOccupiedPointer = { occupiedPointers.add(it) },
            onReleasePointer = {
                occupiedPointers.remove(it)
                moveOnlyPointers.remove(it)
            },
            previewScenario = previewScenario
        )
    }

    //预览摇杆
    val special by observableLayout.special.collectAsStateWithLifecycle()
    PreviewJoystickControlLayout(
        special = special,
        screenSize = screenSize,
        enableJoystick = enableJoystick,
        previewHideLayerWhen = previewHideLayerWhen,
        previewScenario = previewScenario
    )
}

/**
 * 预览鼠标控制层
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 * @param previewScenario 控制布局预览的场景
 */
@Composable
private fun PreviewMouseLayout(
    modifier: Modifier = Modifier,
    screenSize: IntSize,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    previewScenario: PreviewScenario
) {
    Box(
        modifier = modifier
    ) {
        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            screenSize = screenSize,
            cursorMode = previewScenario.cursorMode,
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}

/**
 * 预览摇杆控制层
 * @param special 由控制布局提供的特殊设定，摇杆会根据这里的配置应用样式
 * @param previewScenario 控制布局预览的场景
 */
@Composable
private fun PreviewJoystickControlLayout(
    screenSize: IntSize,
    special: ObservableSpecial,
    enableJoystick: Boolean,
    previewHideLayerWhen: HideLayerWhen,
    previewScenario: PreviewScenario
) {
    val joystickStyle by special.joystickStyle.collectAsStateWithLifecycle()

    val density = LocalDensity.current

    val hideState = when (previewHideLayerWhen) {
        HideLayerWhen.WhenMouse -> AllSettings.joystickHideWhenMouse.state
        HideLayerWhen.WhenGamepad -> AllSettings.joystickHideWhenGamepad.state
        HideLayerWhen.None -> false
    }

    if (enableJoystick && previewScenario.isCursorGrabbing && !hideState) {
        val size = AllSettings.joystickControlSize.state.dp
        val x = AllSettings.joystickControlX.state
        val y = AllSettings.joystickControlY.state

        val position = remember(screenSize, size, x, y) {
            val widgetSize = with(density) {
                val pixelSize = size.roundToPx()
                IntSize(
                    width = pixelSize,
                    height = pixelSize
                )
            }

            widgetPosition(
                xPercentage = x / 10000f,
                yPercentage = y / 10000f,
                widgetSize = widgetSize,
                screenSize = screenSize
            )
        }

        StyleableJoystick(
            modifier = Modifier
                .absoluteOffset {
                    IntOffset(x = position.x.toInt(), y = position.y.toInt())
                },
            style = joystickStyle ?: DefaultObservableJoystickStyle, //预览模式不显示启动器默认样式
            size = size,
            deadZoneRatio = AllSettings.joystickDeadZoneRatio.state / 100f,
            canLock = AllSettings.joystickControlCanLock.state
        )
    }
}