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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.DualMenuSubscreen
import com.movtery.zalithlauncher.ui.components.FloatingBall
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.MenuListLayout
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.MenuSwitchButton
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.components.itemLayoutShadowElevation
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 控制布局编辑器操作状态
 */
sealed interface EditorOperation {
    data object None : EditorOperation
    /** 选择了一个控件进行编辑 */
    data object SelectButton : EditorOperation
    /** 编辑控件层属性 */
    data class EditLayer(val layer: ObservableControlLayer) : EditorOperation
    /** 删除控件层 */
    data class DeleteLayer(val layer: ObservableControlLayer) : EditorOperation
    /** 打开控件外观列表 */
    data object OpenStyleList : EditorOperation
    /** 创建控件外观 */
    data object CreateStyle : EditorOperation
    /** 编辑控件外观 */
    data object EditButtonStyle : EditorOperation
    /** 删除控件外观 */
    data class DeleteButtonStyle(val style: ObservableButtonStyle) : EditorOperation
    /** 创建摇杆样式独立设定 */
    data object CreateJoystickStyle : EditorOperation
    /** 关于摇杆的提醒 */
    data object TipJoystick : EditorOperation
    /** 打开摇杆样式独立设定页面 */
    data object EditJoystickStyle : EditorOperation
    /** 删除摇杆样式独立设定 */
    data object DeleteJoystickStyle : EditorOperation
    /** 控制布局正在保存中 */
    data object Saving : EditorOperation
    /** 控制布局保存失败 */
    data class SaveFailed(val error: Throwable) : EditorOperation
}

/**
 * 控制布局编辑器对控件的操作状态
 */
sealed interface EditorWidgetOperation {
    data object None : EditorWidgetOperation
    /** 选择了一个控件, 并询问用户将其复制到哪些控制层 */
    data class CloneButton(val data: ObservableWidget, val layer: ObservableControlLayer) : EditorWidgetOperation
    /** 删除一个控件 */
    data class DeleteButton(val data: ObservableWidget, val layer: ObservableControlLayer) : EditorWidgetOperation
    /** 编辑控件的显示文本 */
    data class EditWidgetText(val string: ObservableTranslatableString) : EditorWidgetOperation
    /** 编辑切换控件层可见性事件 */
    data class SwitchLayersVisibility(val data: ObservableNormalData, val type: ClickEvent.Type) : EditorWidgetOperation
    /** 编辑发送的文本 */
    data class SendText(val data: ObservableNormalData) : EditorWidgetOperation
}

/**
 * 控制布局编辑器的一些警告的操作状态
 */
sealed interface EditorWarningOperation {
    data object None : EditorWarningOperation
    /** 没有控件层，提醒用户添加 */
    data object WarningNoLayers : EditorWarningOperation
    /** 没有选择控件层，提醒用户选择 */
    data object WarningNoSelectLayer : EditorWarningOperation
}

/**
 * 预览控制布局的场景
 */
enum class PreviewScenario(
    val textRes: Int,
    val cursorMode: Int,
    val isCursorGrabbing: Boolean = cursorMode == CURSOR_DISABLED
) {
    InGame(R.string.control_editor_menu_preview_mode_in_game, cursorMode = CURSOR_DISABLED),
    InMenu(R.string.control_editor_menu_preview_mode_in_menu, cursorMode = CURSOR_ENABLED)
}

@Composable
fun VisibilityType.getVisibilityText(): String {
    val textRes = when (this) {
        VisibilityType.ALWAYS -> R.string.control_editor_edit_visibility_always
        VisibilityType.IN_GAME -> R.string.control_editor_edit_visibility_in_game
        VisibilityType.IN_MENU -> R.string.control_editor_edit_visibility_in_menu
    }
    return stringResource(textRes)
}

@Composable
fun MenuBox(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onClick: () -> Unit
) {
    FloatingBall(
        position = position,
        onPositionChanged = onPositionChanged,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(all = 2.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
        }
    }
}

@Composable
fun EditorMenu(
    state: MenuState,
    closeScreen: () -> Unit,
    layers: List<ObservableControlLayer>,
    onReorder: (from: Int, to: Int) -> Unit,
    selectedLayer: ObservableControlLayer?,
    onLayerSelected: (ObservableControlLayer?) -> Unit,
    createLayer: () -> Unit,
    onAttribute: (ObservableControlLayer) -> Unit,
    addNewButton: () -> Unit,
    addNewText: () -> Unit,
    openStyleList: () -> Unit,
    onEditJoystickStyle: () -> Unit,
    isLayerFocus: Boolean,
    onLayerFocusChanged: (Boolean) -> Unit,
    isPreviewMode: Boolean,
    onPreviewChanged: (Boolean) -> Unit,
    previewScenario: PreviewScenario,
    onPreviewScenarioChanged: (PreviewScenario) -> Unit,
    previewHideLayerWhen: HideLayerWhen,
    onPreviewHideLayerChanged: (HideLayerWhen) -> Unit,
    enableJoystick: Boolean,
    onJoystickSwitch: (Boolean) -> Unit,
    onJoystickTip: () -> Unit,
    onSave: () -> Unit,
    saveAndExit: () -> Unit,
    onExit: () -> Unit
) {
    DualMenuSubscreen(
        state = state,
        closeScreen = closeScreen,
        leftMenuTitle = {
            Text(
                modifier = Modifier.padding(all = 8.dp),
                text = stringResource(R.string.control_editor_menu_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        leftMenuContent = {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(all = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //添加按钮
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isPreviewMode.not(),
                        text = stringResource(R.string.control_editor_menu_new_widget_button),
                        onClick = addNewButton
                    )
                }

                //添加文本框
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isPreviewMode.not(),
                        text = stringResource(R.string.control_editor_menu_new_widget_text),
                        onClick = addNewText
                    )
                }

                //控件外观列表
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_edit_style_config),
                        enabled = isPreviewMode.not(),
                        onClick = {
                            openStyleList()
                            closeScreen()
                        }
                    )
                }

                //摇杆样式
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_special_joystick_style),
                        enabled = isPreviewMode.not(),
                        appendLayout = {
                            //摇杆提示弹窗
                            IconButton(
                                onClick = {
                                    onJoystickTip()
                                    closeScreen()
                                },
                                enabled = isPreviewMode.not()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = stringResource(R.string.generic_tip)
                                )
                            }
                        },
                        onClick = {
                            onEditJoystickStyle()
                            closeScreen()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                //预览控制布局
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_preview_mode),
                        switch = isPreviewMode,
                        onSwitch = { onPreviewChanged(it) }
                    )
                }

                //预览场景
                item {
                    MenuListLayout(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_editor_menu_preview_mode_scenario),
                        items = PreviewScenario.entries,
                        currentItem = previewScenario,
                        onItemChange = onPreviewScenarioChanged,
                        getItemText = { scenario ->
                            stringResource(scenario.textRes)
                        },
                        enabled = isPreviewMode
                    )
                }

                //正在使用实体鼠标
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_preview_is_mouse),
                        switch = previewHideLayerWhen == HideLayerWhen.WhenMouse,
                        onSwitch = { value ->
                            onPreviewHideLayerChanged(
                                if (value) HideLayerWhen.WhenMouse
                                else HideLayerWhen.None
                            )
                        },
                        enabled = isPreviewMode
                    )
                }

                //正在使用手柄
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_preview_is_gamepad),
                        switch = previewHideLayerWhen == HideLayerWhen.WhenGamepad,
                        onSwitch = { value ->
                            onPreviewHideLayerChanged(
                                if (value) HideLayerWhen.WhenGamepad
                                else HideLayerWhen.None
                            )
                        },
                        enabled = isPreviewMode
                    )
                }

                //启用摇杆
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.game_styles_joystick_enable),
                        switch = enableJoystick,
                        onSwitch = { value ->
                            onJoystickSwitch(value)
                        },
                        enabled = isPreviewMode
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                //控件吸附
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_widget_snap),
                        switch = AllSettings.editorEnableWidgetSnap.state,
                        onSwitch = { AllSettings.editorEnableWidgetSnap.save(it) }
                    )
                }

                //所有控制层范围吸附
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_widget_snap_all_layers),
                        switch = AllSettings.editorSnapInAllLayers.state,
                        onSwitch = { AllSettings.editorSnapInAllLayers.save(it) }
                    )
                }

                //控件吸附模式
                item {
                    MenuListLayout(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_editor_menu_widget_snap_mode),
                        items = SnapMode.entries,
                        currentItem = AllSettings.editorWidgetSnapMode.state,
                        onItemChange = { AllSettings.editorWidgetSnapMode.save(it) },
                        getItemText = { mode ->
                            val textRes = when (mode) {
                                SnapMode.FullScreen -> R.string.control_editor_menu_widget_snap_mode_fullscreen
                                SnapMode.Local -> R.string.control_editor_menu_widget_snap_mode_local
                            }
                            stringResource(textRes)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                //保存
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.generic_save),
                        onClick = onSave
                    )
                }

                //保存并退出
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_save_and_exit),
                        onClick = saveAndExit
                    )
                }

                //直接退出
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_exit_confirm),
                        onClick = onExit
                    )
                }
            }
        },
        rightMenuTitle = {
            Text(
                modifier = Modifier.padding(all = 8.dp),
                text = stringResource(R.string.control_editor_layers_title),
                style = MaterialTheme.typography.titleMedium
            )
            //切换控件层聚焦
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    onLayerFocusChanged(isLayerFocus.not())
                },
                enabled = isPreviewMode.not() && selectedLayer != null
            ) {
                Crossfade(
                    targetState = isLayerFocus
                ) { isFocus ->
                    Icon(
                        imageVector = if (isFocus) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusStrong,
                        contentDescription = null
                    )
                }
            }
        },
        rightMenuContent = {
            ControlLayerMenu(
                layers = layers,
                onReorder = onReorder,
                selectedLayer = selectedLayer,
                onLayerSelected = onLayerSelected,
                createLayer = createLayer,
                onAttribute = onAttribute,
                influencedByBackground = false,
                enabled = isPreviewMode.not()
            )
        }
    )
}

@Composable
private fun ColumnScope.ControlLayerMenu(
    layers: List<ObservableControlLayer>,
    onReorder: (from: Int, to: Int) -> Unit,
    selectedLayer: ObservableControlLayer?,
    onLayerSelected: (ObservableControlLayer?) -> Unit,
    createLayer: () -> Unit,
    onAttribute: (ObservableControlLayer) -> Unit,
    influencedByBackground: Boolean = false,
    enabled: Boolean = true
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            onReorder(from.index, to.index)
        }
    )

    LaunchedEffect(Unit) {
        runCatching {
            val index = layers.indexOfFirst { it == selectedLayer }
            if (index >= 0 && index < layers.size) {
                lazyListState.animateScrollToItem(index)
            }
        }
    }

    //检查列表新增情况，自动滚动到顶部
    var previousSize by remember { mutableIntStateOf(0) }
    val currentSize = layers.size
    LaunchedEffect(currentSize) {
        if (currentSize != previousSize) {
            if (currentSize - previousSize > 0) {
                runCatching {
                    lazyListState.animateScrollToItem(0)
                }
            }
            previousSize = currentSize
        }
    }

    LazyColumn(
        modifier = Modifier.weight(1f),
        state = lazyListState,
        contentPadding = PaddingValues(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(layers, { it.uuid }) { layer ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = layer.uuid,
                enabled = enabled,
            ) { isDragging ->
                val shadowElevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
                ControlLayerItem(
                    modifier = Modifier.fillMaxWidth(),
                    layer = layer,
                    dragButtonModifier = Modifier.draggableHandle(),
                    selected = selectedLayer == layer,
                    shadowElevation = shadowElevation,
                    onSelected = {
                        onLayerSelected(layer)
                    },
                    onUnSelected = {
                        onLayerSelected(null)
                    },
                    onAttribute = {
                        onAttribute(layer)
                    },
                    influencedByBackground = influencedByBackground,
                    enabled = enabled
                )
            }
        }
    }
    ScalingActionButton(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp)
            .fillMaxWidth(),
        onClick = createLayer
    ) {
        MarqueeText(text = stringResource(R.string.control_editor_layers_create))
    }
}

@Composable
private fun ControlLayerItem(
    modifier: Modifier = Modifier,
    layer: ObservableControlLayer,
    dragButtonModifier: Modifier,
    selected: Boolean,
    onSelected: () -> Unit,
    onUnSelected: () -> Unit,
    onAttribute: () -> Unit,
    influencedByBackground: Boolean = false,
    color: Color = itemLayoutColor(influencedByBackground = influencedByBackground),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
    shadowElevation: Dp = itemLayoutShadowElevation(influencedByBackground = influencedByBackground),
    enabled: Boolean = true
) {
    val borderWidth by animateDpAsState(
        if (selected && enabled) 4.dp
        else (-1).dp
    )

    Surface(
        modifier = modifier.border(
            width = borderWidth,
            color = borderColor,
            shape = shape
        ),
        color = color,
        contentColor = contentColor,
        shape = shape,
        shadowElevation = shadowElevation,
        onClick = {
            if (selected) onUnSelected()
            else onSelected()
        },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    layer.editorHide = layer.editorHide.not()
                },
                enabled = enabled
            ) {
                Crossfade(
                    targetState = layer.editorHide
                ) { isHide ->
                    Icon(
                        imageVector = if (isHide) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            }
            MarqueeText(
                modifier = Modifier.weight(1f),
                text = layer.name,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = onAttribute,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.control_editor_layers_attribute)
                )
            }
            Row(
                modifier = dragButtonModifier
                    .fillMaxHeight()
                    .clip(CircleShape),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(all = 4.dp),
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null
                )
            }
        }
    }
}
