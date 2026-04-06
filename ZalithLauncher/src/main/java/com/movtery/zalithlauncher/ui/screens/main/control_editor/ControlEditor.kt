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

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.layer_controller.ControlEditorLayer
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.CenterPosition
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.createAdaptiveButtonSize
import com.movtery.layer_controller.data.createWidgetWithUUID
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.createNewLayer
import com.movtery.layer_controller.observable.DefaultObservableJoystickStyle
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.EditJoystickStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_joystick.EditJoystickStyleMode
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditControlLayerDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditSwitchLayersVisibilityDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.EditButtonStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.StyleListDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.EditWidgetDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectLayers
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectedWidgetData
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import java.io.File

/**
 * 控制布局编辑器主要UI，用于编辑控制布局
 * @param exit 保存后执行的退出
 * @param menuExit 通过菜单直接调用的“直接退出”
 */
@Composable
fun BoxWithConstraintsScope.ControlEditor(
    viewModel: EditorViewModel,
    targetFile: File,
    exit: () -> Unit,
    menuExit: () -> Unit
) {
    val layers by viewModel.observableLayout.layers.collectAsStateWithLifecycle()
    val styles by viewModel.observableLayout.styles.collectAsStateWithLifecycle()
    val special by viewModel.observableLayout.special.collectAsStateWithLifecycle()
    val joystickStyle by special.joystickStyle.collectAsStateWithLifecycle()

    /** 默认新建的控件层的名称 */
    val defaultLayerName = stringResource(R.string.control_editor_edit_layer_default)
    /** 默认新建的按键的名称 */
    val defaultButtonName = stringResource(R.string.control_editor_edit_button_default)
    /** 默认新建的文本框的名称 */
    val defaultTextName = stringResource(R.string.control_editor_edit_text_default)

    val density = LocalDensity.current
    val screenSize = rememberBoxSize()

    if (viewModel.isPreviewMode) {
        PreviewControlBox(
            modifier = Modifier.fillMaxSize(),
            observableLayout = viewModel.observableLayout,
            previewScenario = viewModel.previewScenario,
            previewHideLayerWhen = viewModel.previewHideLayerWhen,
            enableJoystick = viewModel.enableJoystick
        )
    } else {
        ControlEditorLayer(
            observedLayout = viewModel.observableLayout,
            onButtonTap = { data, layer ->
                viewModel.selectedWidget = SelectedWidgetData(data, layer)
                viewModel.editorOperation = EditorOperation.SelectButton
            },
            enableSnap = AllSettings.editorEnableWidgetSnap.state,
            snapInAllLayers = AllSettings.editorSnapInAllLayers.state,
            snapMode = AllSettings.editorWidgetSnapMode.state,
            focusedLayer = viewModel.selectedLayer?.takeIf { viewModel.isLayerFocus },
            isDark = isLauncherInDarkTheme()
        )
    }

    EditorMenu(
        state = viewModel.editorMenu,
        closeScreen = { viewModel.editorMenu = MenuState.HIDE },
        layers = layers,
        onReorder = { from, to ->
            viewModel.observableLayout.reorder(from, to)
        },
        selectedLayer = viewModel.selectedLayer,
        onLayerSelected = { layer ->
            viewModel.selectedLayer = layer
        },
        createLayer = {
            val newLayer = viewModel.observableLayout.addLayer(
                layer = createNewLayer(defaultLayerName = defaultLayerName)
            )
            viewModel.editorOperation = EditorOperation.EditLayer(newLayer)
        },
        onAttribute = { layer ->
            viewModel.editorOperation = EditorOperation.EditLayer(layer)
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                layer.addNormalButton(
                    createWidgetWithUUID { uuid ->
                        NormalData(
                            text = createTranslatable(default = defaultButtonName),
                            uuid = uuid,
                            position = CenterPosition,
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density
                            ),
                            visibilityType = VisibilityType.ALWAYS,
                            isSwipple = false,
                            isPenetrable = false,
                            isToggleable = false
                        )
                    }
                )
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(
                    createWidgetWithUUID { uuid ->
                        TextData(
                            text = createTranslatable(default = defaultTextName),
                            uuid = uuid,
                            position = CenterPosition,
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density,
                                type = ButtonSize.Type.WrapContent //文本框默认使用包裹内容
                            ),
                            visibilityType = VisibilityType.ALWAYS
                        )
                    }
                )
            }
        },
        openStyleList = {
            viewModel.editorOperation = EditorOperation.OpenStyleList
        },
        onEditJoystickStyle = {
            if (joystickStyle == null) {
                viewModel.editorOperation = EditorOperation.CreateJoystickStyle
            } else {
                viewModel.editorOperation = EditorOperation.EditJoystickStyle
            }
        },
        isLayerFocus = viewModel.isLayerFocus,
        onLayerFocusChanged = { viewModel.isLayerFocus = it },
        isPreviewMode = viewModel.isPreviewMode,
        onPreviewChanged = { mode ->
            viewModel.applyEditorHide()
            viewModel.isPreviewMode = mode
        },
        previewScenario = viewModel.previewScenario,
        onPreviewScenarioChanged = { scenario ->
            viewModel.previewScenario = scenario
        },
        previewHideLayerWhen = viewModel.previewHideLayerWhen,
        onPreviewHideLayerChanged = { hideWhen ->
            viewModel.previewHideLayerWhen = hideWhen
        },
        enableJoystick = viewModel.enableJoystick,
        onJoystickSwitch = { value ->
            viewModel.enableJoystick = value
        },
        onJoystickTip = {
            viewModel.editorOperation = EditorOperation.TipJoystick
        },
        onSave = {
            viewModel.save(targetFile, onSaved = {})
        },
        saveAndExit = {
            viewModel.save(targetFile, onSaved = exit)
        },
        onExit = menuExit,
    )

    MenuBox(
        position = viewModel.editorBallPosition,
        onPositionChanged = { viewModel.editorBallPosition = it }
    ) {
        viewModel.switchMenu()
    }

    EditWidgetDialog(
        data = viewModel.selectedWidget,
        visible = viewModel.editorOperation == EditorOperation.SelectButton,
        styles = styles,
        onDismissRequest = {
            viewModel.editorOperation = EditorOperation.None
        },
        onDelete = { data, layer ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.DeleteButton(data, layer)
        },
        onClone = { data, layer ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.CloneButton(data, layer)
        },
        onEditWidgetText = { string ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.EditWidgetText(string)
        },
        switchControlLayers = { data, type ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.SwitchLayersVisibility(data, type)
        },
        sendText = { data ->
            viewModel.editorWidgetOperation = EditorWidgetOperation.SendText(data)
        },
        openStyleList = {
            viewModel.editorOperation = EditorOperation.OpenStyleList
        }
    )

    EditButtonStyleDialog(
        visible = viewModel.editorOperation == EditorOperation.EditButtonStyle,
        style = viewModel.selectedStyle,
        onClose = {
            viewModel.editorOperation = EditorOperation.None
        }
    )

    EditJoystickStyleDialog(
        visible = viewModel.editorOperation == EditorOperation.EditJoystickStyle,
        style = joystickStyle,
        mode = EditJoystickStyleMode.ControlLayout,
        onClose = {
            viewModel.editorOperation = EditorOperation.None
        },
        onInfoButtonClick = {
            viewModel.editorOperation = EditorOperation.DeleteJoystickStyle
        }
    )

    EditorOperation(
        operation = viewModel.editorOperation,
        changeOperation = { viewModel.editorOperation = it },
        onDeleteLayer = { layer ->
            viewModel.removeLayer(layer)
        },
        onMergeDownward = { layer ->
            viewModel.observableLayout.mergeDownward(layer)
        },
        onCopy = { layer ->
            val baseLayer = layer.pack()
            val newLayer = viewModel.observableLayout.addLayer(
                layer = createNewLayer(
                    defaultLayerName = defaultLayerName
                ).copy(
                    hide = baseLayer.hide,
                    hideWhenMouse = baseLayer.hideWhenMouse,
                    hideWhenGamepad = baseLayer.hideWhenGamepad,
                    hideWhenJoystick = baseLayer.hideWhenJoystick,
                    visibilityType = baseLayer.visibilityType,
                    normalButtons = baseLayer.normalButtons,
                    textBoxes = baseLayer.textBoxes
                )
            )
            viewModel.editorOperation = EditorOperation.EditLayer(newLayer)
        },
        onEditStyle = { style ->
            viewModel.selectedStyle = style
            viewModel.editorOperation = EditorOperation.EditButtonStyle
        },
        onCreateStyle = { name ->
            viewModel.createNewStyle(name)
        },
        onCloneStyle = { style ->
            viewModel.cloneStyle(style)
        },
        onDeleteStyle = { style ->
            viewModel.removeStyle(style)
        },
        onCreateJoystickStyle = {
            special.setJoystickStyle(DefaultObservableJoystickStyle)
            viewModel.editorOperation = EditorOperation.EditJoystickStyle
        },
        onDeleteJoystickStyle = {
            special.setJoystickStyle(null)
            viewModel.editorOperation = EditorOperation.None
        },
        styles = styles
    )

    EditorWidgetOperation(
        operation = viewModel.editorWidgetOperation,
        changeOperation = { viewModel.editorWidgetOperation = it },
        controlLayers = layers,
        onCloneWidgets = { widget, layers ->
            viewModel.cloneWidgetToLayers(widget, layers)
        },
        onDeleteWidget = { widget, layer ->
            viewModel.removeWidget(layer, widget)
            viewModel.editorOperation = EditorOperation.None
        }
    )

    EditorWarningOperation(
        operation = viewModel.editorWarningOperation,
        changeOperation = { viewModel.editorWarningOperation = it }
    )
}

@Composable
private fun EditorOperation(
    operation: EditorOperation,
    changeOperation: (EditorOperation) -> Unit,
    onDeleteLayer: (ObservableControlLayer) -> Unit,
    onMergeDownward: (ObservableControlLayer) -> Unit,
    onCopy: (ObservableControlLayer) -> Unit,
    onEditStyle: (ObservableButtonStyle) -> Unit,
    onCreateStyle: (name: String) -> Unit,
    onCloneStyle: (ObservableButtonStyle) -> Unit,
    onDeleteStyle: (ObservableButtonStyle) -> Unit,
    onCreateJoystickStyle: () -> Unit,
    onDeleteJoystickStyle: () -> Unit,
    styles: List<ObservableButtonStyle>
) {
    when (operation) {
        is EditorOperation.None,
        is EditorOperation.SelectButton,
        is EditorOperation.EditButtonStyle,
        is EditorOperation.EditJoystickStyle -> {}

        is EditorOperation.EditLayer -> {
            val layer = operation.layer
            EditControlLayerDialog(
                layer = layer,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onDelete = {
                    changeOperation(EditorOperation.DeleteLayer(layer))
                },
                onMergeDownward = {
                    onMergeDownward(layer)
                },
                onCopy = {
                    onCopy(layer)
                },
            )
        }
        is EditorOperation.DeleteLayer -> {
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_layers_delete, layer.name),
                onDismiss = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onDeleteLayer(layer)
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.OpenStyleList -> {
            StyleListDialog(
                styles = styles,
                onEditStyle = onEditStyle,
                onCreate = {
                    changeOperation(EditorOperation.CreateStyle)
                },
                onClone = { style ->
                    onCloneStyle(style)
                },
                onDelete = { style ->
                    changeOperation(EditorOperation.DeleteButtonStyle(style))
                },
                onClose = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.CreateStyle -> {
            var name by remember { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_style_config_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onCreateStyle(name)
                    changeOperation(EditorOperation.OpenStyleList)
                }
            )
        }
        is EditorOperation.DeleteButtonStyle -> {
            val style = operation.style
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_edit_style_config_delete, style.name),
                onDismiss = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onDeleteStyle(style)
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.TipJoystick -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_special_joystick_style_tip_title),
                text = stringResource(R.string.control_editor_special_joystick_style_tip_summary),
                onDismiss = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.CreateJoystickStyle -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_special_joystick_style_create_title),
                text = stringResource(R.string.control_editor_special_joystick_style_create_summary),
                confirmText = stringResource(R.string.control_manage_create_new),
                onConfirm = onCreateJoystickStyle,
                onDismiss = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.DeleteJoystickStyle -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_special_joystick_style_delete_title),
                text = stringResource(R.string.control_editor_special_joystick_style_delete_summary),
                confirmText = stringResource(R.string.generic_delete),
                onConfirm = onDeleteJoystickStyle,
                onDismiss = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.Saving -> {
            ProgressDialog(
                title = stringResource(R.string.control_manage_saving)
            )
        }
        is EditorOperation.SaveFailed -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_manage_failed_to_save),
                text = operation.error.getMessageOrToString()
            ) {
                changeOperation(EditorOperation.None)
            }
        }
    }
}

@Composable
private fun EditorWidgetOperation(
    operation: EditorWidgetOperation,
    changeOperation: (EditorWidgetOperation) -> Unit,
    controlLayers: List<ObservableControlLayer>,
    onCloneWidgets: (ObservableWidget, List<ObservableControlLayer>) -> Unit,
    onDeleteWidget: (ObservableWidget, ObservableControlLayer) -> Unit,
) {
    when (operation) {
        is EditorWidgetOperation.None -> {}
        is EditorWidgetOperation.CloneButton -> {
            val data = operation.data
            val layer = operation.layer
            SelectLayers(
                layers= controlLayers,
                initLayer = layer,
                onDismissRequest = {
                    changeOperation(EditorWidgetOperation.None)
                },
                title = stringResource(R.string.control_editor_edit_dialog_clone_widget_title),
                confirmText = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                onConfirm = { layers ->
                    onCloneWidgets(data, layers)
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
        is EditorWidgetOperation.DeleteButton -> {
            val data = operation.data
            val layer = operation.layer
            SimpleAlertDialog(
                title = stringResource(R.string.generic_delete),
                text = stringResource(R.string.control_editor_edit_dialog_delete_widget),
                onDismiss = {
                    changeOperation(EditorWidgetOperation.None)
                },
                onConfirm = {
                    onDeleteWidget(data, layer)
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
        is EditorWidgetOperation.EditWidgetText -> {
            EditTranslatableTextDialog(
                text = operation.string,
                singleLine = false,
                onClose = {
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
        is EditorWidgetOperation.SendText -> {
            val data = operation.data
            //文本内容
            var value by remember {
                mutableStateOf(data.clickEvents.find { it.type == ClickEvent.Type.SendText }?.key ?: "")
            }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_event_launcher_send_text),
                value = value,
                onValueChange = { new ->
                    value = new
                },
                extraBody = {
                    Text(
                        text = stringResource(R.string.control_editor_edit_event_launcher_send_text_summary),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                label = {
                    Text(text = stringResource(R.string.control_editor_edit_event_launcher_send_text_hint))
                },
                singleLine = true,
                onConfirm = {
                    //清除所有发送文本事件，如果文本不为空则再添加
                    data.removeAllEvent(ClickEvent.Type.SendText)
                    if (value.isNotEmpty()) {
                        data.addEvent(ClickEvent(ClickEvent.Type.SendText, value))
                    }
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
        is EditorWidgetOperation.SwitchLayersVisibility -> {
            val data = operation.data
            val type = operation.type
            EditSwitchLayersVisibilityDialog(
                data = data,
                layers = controlLayers,
                type = type,
                onDismissRequest = {
                    changeOperation(EditorWidgetOperation.None)
                }
            )
        }
    }
}

@Composable
private fun EditorWarningOperation(
    operation: EditorWarningOperation,
    changeOperation: (EditorWarningOperation) -> Unit
) {
    when (operation) {
        is EditorWarningOperation.None -> {}
        is EditorWarningOperation.WarningNoLayers -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_layers_title),
                text = stringResource(R.string.control_editor_menu_no_layers_message)
            ) {
                changeOperation(EditorWarningOperation.None)
            }
        }
        is EditorWarningOperation.WarningNoSelectLayer -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_selected_layer_title),
                text = stringResource(R.string.control_editor_menu_no_selected_layer_message)
            ) {
                changeOperation(EditorWarningOperation.None)
            }
        }
    }
}