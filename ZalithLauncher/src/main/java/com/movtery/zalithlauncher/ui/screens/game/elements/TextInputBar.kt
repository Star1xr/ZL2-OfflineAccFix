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

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface
import com.movtery.zalithlauncher.ui.control.input.InputListener
import com.movtery.zalithlauncher.ui.control.input.TouchCharInput

enum class InputBarMode {
    /**
     * 悬浮模式，比如输入法为悬浮窗模式，未占用大部分画面时
     * 可以将输入栏设置为悬浮模式（不可拖动，固定在屏幕上方悬浮）
     */
    Floating,

    /**
     * 填充剩余画面模式，输入法为全屏模式
     * 可以将输入栏设置为填充模式，填充宽度，并将输入栏贴在输入法上方
     */
    Filling
}

/**
 * 输入模式，控制如何进行输入
 */
enum class InputMode(val textRes: Int) {
    /**
     * 默认模式：拥有输入框，根据输入的内容进行智障发送（几乎实时）
     */
    Default(R.string.game_input_proxy_mode_default) {
        override fun next(): InputMode = Simple
    },

    /**
     * 简单模式：不显式拥有输入框，输入内容即发送（几乎实时）
     */
    Simple(R.string.game_input_proxy_mode_simple) {
        override fun next(): InputMode = Send
    },

    /**
     * 发送模式：拥有输入框，输入好内容，手动点击发送按钮发送（不实时）
     */
    Send(R.string.game_input_proxy_mode_send) {
        override fun next(): InputMode = Default
    };

    abstract fun next(): InputMode
}

@Composable
fun TextInputBarArea(
    content: @Composable BoxScope.(innerModifier: Modifier, mode: InputBarMode) -> Unit
) {
    val density = LocalDensity.current
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()

    val imeBottomDp = WindowInsets.ime
        .asPaddingValues()
        .calculateBottomPadding()

    val mode = remember(density, screenHeightPx, imeBottomDp) {
        val imeBottomPx = with(density) { imeBottomDp.toPx() }
        val isFullscreenKeyboard = imeBottomPx > 0 && imeBottomPx > screenHeightPx * 0.4f

        if (isFullscreenKeyboard) {
            InputBarMode.Filling
        } else {
            InputBarMode.Floating
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = when (mode) {
            InputBarMode.Floating -> Alignment.TopCenter
            InputBarMode.Filling -> Alignment.BottomCenter
        }
    ) {
        val innerModifier = when (mode) {
            InputBarMode.Floating -> Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
            InputBarMode.Filling -> Modifier
                .fillMaxWidth()
                .padding(bottom = imeBottomDp)
        }

        content(innerModifier, mode)
    }
}

/**
 * 游戏画面置顶输入条，代理用户输入的文本，确保编辑好后再发送文本到游戏
 * 之前使用的实现[InputConnection]的方式手动尝试适配输入法，但是这个方案还是因为兼容性问题下掉了
 * 不是很能顶得住适配所有输入法 :(
 *
 * @param mode 控制输入条的显示模式，主要用于照顾全屏输入法（悬浮输入法或者关闭输入法时，可以使用 [InputBarMode.Floating]）
 * @param inputMode 控制输入代理的输入模式，这里仅用于控制外观，以及回调方式，具体逻辑在回调中完成
 * @param show 控制是否显示输入条，主要用于淡出淡入的动画效果
 * @param enabledActionBar 是否启用操作栏
 * @param onHandle 当输入模式为[InputMode.Default]时，会使用这个回调返回状态
 * @param onSend 当输入模式为[InputMode.Send]或[InputMode.Simple]时，会使用这个回调返回状态
 */
@Composable
fun TextInputBar(
    modifier: Modifier = Modifier,
    mode: InputBarMode,
    inputMode: InputMode,
    onInputModeChange: (InputMode) -> Unit,
    textFieldState: TextFieldState,
    show: Boolean,
    enabledActionBar: Boolean = true,
    onChangeActionBar: (Boolean) -> Unit = {},
    onClose: () -> Unit,
    onHandle: (text: String, selection: TextRange) -> Unit,
    onSend: (text: String) -> Unit,
    onClear: () -> Unit,
    onShiftClick: (press: Boolean) -> Unit,
    onCtrlClick: (press: Boolean) -> Unit,
    onTabClick: () -> Unit,
    onEnterClick: () -> Unit,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onBackspaceClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(
            initialOffsetY = { -it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ) + fadeOut(),
    ) {
        //发送模式：不会自动发送，由用户手动发送
        //简单模式：由TouchCharInput控制发送
        if (inputMode == InputMode.Default) {
            val currentOnHandle by rememberUpdatedState(onHandle)
            LaunchedEffect(textFieldState.text, textFieldState.selection) {
                val currentText = textFieldState.text.toString()
                val currentSelection = textFieldState.selection
                currentOnHandle(currentText, currentSelection)
            }
        }

        val currentOnClear by rememberUpdatedState(onClear)
        DisposableEffect(Unit) {
            onDispose {
                currentOnClear()
            }
        }

        val surfaceShape = when (mode) {
            InputBarMode.Floating -> MaterialTheme.shapes.extraLarge
            InputBarMode.Filling -> RectangleShape
        }

        /**
         * 可见输入框
         */
        @Composable
        fun ShowableInputLayout(
            inputFocus: FocusRequester,
            focusManager: FocusManager,
            keyboardController: SoftwareKeyboardController?,
            modifier: Modifier = Modifier
        ) {
            OutlinedTextField(
                modifier = modifier.focusRequester(inputFocus),
                state = textFieldState,
                leadingIcon = {
                    //关闭按钮
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.generic_close)
                        )
                    }
                },
                trailingIcon = {
                    //收起输入法
                    IconButton(
                        onClick = {
                            focusManager.clearFocus(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.generic_close)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = {
                    focusManager.clearFocus(true)
                    onEnterClick()
                    onClear()
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = MaterialTheme.shapes.large
            )

            //根据show来决定是否显示/隐藏输入法
            LaunchedEffect(show) {
                if (show) {
                    inputFocus.requestFocus()
                    keyboardController?.show()
                } else {
                    focusManager.clearFocus(true)
                    keyboardController?.hide()
                }
            }
        }

        @Composable
        fun HidableInputLayout(
            inputFocus: FocusRequester,
            focusManager: FocusManager,
            keyboardController: SoftwareKeyboardController?,
            view: TouchCharInput?,
            onViewReady: (TouchCharInput) -> Unit
        ) {
            AndroidView(
                modifier = Modifier
                    .alpha(0f)
                    .size(1.dp)
                    .focusRequester(inputFocus),
                factory = { context ->
                    TouchCharInput(context).apply {
                        imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or
                                EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                                EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                                EditorInfo.IME_ACTION_DONE

                        inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                                InputType.TYPE_TEXT_VARIATION_FILTER or
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE

                        setEms(10)
                    }.also {
                        onViewReady(it)
                    }
                }
            )

            //根据show来决定是否显示/隐藏输入法
            LaunchedEffect(view, show) {
                if (show) {
                    inputFocus.requestFocus()
                    keyboardController?.show()
                    view?.enableKeyboard()
                } else {
                    focusManager.clearFocus(true)
                    keyboardController?.hide()
                }
            }
        }

        /**
         * 控制输入的按钮栏
         */
        @Composable
        fun ActionButtonsLayout(
            inputFocus: FocusRequester,
            keyboardController: SoftwareKeyboardController?,
            onKeyboardClick: () -> Unit = {},
            modifier: Modifier = Modifier
        ) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //退格按钮
                SurfaceButton(
                    icon = Icons.AutoMirrored.Default.Backspace,
                    contentDescription = stringResource(R.string.generic_delete),
                    onClick = onBackspaceClick,
                    color = itemLayoutColorOnSurface(),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )

                //简单模式不需要进行切换，默认显示操作栏
                if (mode == InputBarMode.Floating && inputMode != InputMode.Simple) {
                    //操作栏切换按钮
                    SurfaceButton(
                        onClick = {
                            onChangeActionBar(!enabledActionBar)
                        },
                        color = itemLayoutColorOnSurface(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        icon = {
                            Crossfade(
                                targetState = enabledActionBar
                            ) { enabled ->
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    imageVector = if (enabled) {
                                        Icons.Default.ArrowDropUp
                                    } else {
                                        Icons.Default.MoreHoriz
                                    },
                                    contentDescription = stringResource(R.string.generic_more)
                                )
                            }
                        },
                    )
                }

                AnimatedVisibility(
                    visible = inputMode == InputMode.Simple
                ) {
                    //拉起键盘按钮
                    SurfaceButton(
                        icon = Icons.Default.Keyboard,
                        contentDescription = stringResource(R.string.game_button_input),
                        onClick = {
                            inputFocus.requestFocus()
                            keyboardController?.show()
                            onKeyboardClick()
                        },
                        color = itemLayoutColorOnSurface(),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                AnimatedVisibility(
                    visible = inputMode == InputMode.Send
                ) {
                    //发送按钮
                    SurfaceButton(
                        icon = Icons.AutoMirrored.Default.Send,
                        contentDescription = stringResource(R.string.control_editor_edit_event_launcher_send_text),
                        enabled = inputMode == InputMode.Send,
                        onClick = {
                            val text0 = textFieldState.text.toString()
                            //不应该发送空字符串
                            if (text0.isNotEmpty()) {
                                onSend(text0)
                            }
                        }
                    )
                }
            }
        }

        /**
         * 操作按钮栏
         */
        @Composable
        fun ActionBarLayout(
            modifier: Modifier = Modifier
        ) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //在悬浮模式下，显示更多的操作项
                val scrollState = rememberScrollState()
                ActionBar(
                    modifier = Modifier
                        .fadeEdge(state = scrollState, direction = EdgeDirection.Horizontal)
                        .weight(1f)
                        .horizontalScroll(scrollState),
                    onShiftClick = { press ->
                        onShiftClick(press)
                        if (press) onClear()
                    },
                    onCtrlClick = { press ->
                        onCtrlClick(press)
                        if (press) onClear()
                    },
                    onTabClick = {
                        onTabClick()
                        onClear()
                    },
                    onEnterClick = {
                        onEnterClick()
                        onClear()
                    },
                    onUpClick = {
                        onUpClick()
                        onClear()
                    },
                    onDownClick = {
                        onDownClick()
                        onClear()
                    },
                    onLeftClick = {
                        onLeftClick()
                        onClear()
                    },
                    onRightClick = {
                        onRightClick()
                        onClear()
                    }
                )

                //切换输入模式
                Button(
                    onClick = {
                        onClear()
                        onInputModeChange(inputMode.next())
                    }
                ) {
                    Text(text = stringResource(inputMode.textRes))
                }
            }
        }

        Surface(
            modifier = modifier.height(IntrinsicSize.Min),
            shape = surfaceShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            val inputFocus = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            Crossfade(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                targetState = inputMode == InputMode.Simple
            ) { isSimple ->
                if (isSimple) {
                    var view by remember { mutableStateOf<TouchCharInput?>(null) }

                    //隐藏的输入法
                    HidableInputLayout(
                        inputFocus = inputFocus,
                        focusManager = focusManager,
                        keyboardController = keyboardController,
                        view = view,
                        onViewReady = { view0 ->
                            view = view0.also {
                                it.setListener(
                                    object : InputListener {
                                        override fun onEnter() {
                                            onEnterClick()
                                        }
                                        override fun onSend(char: Char) {
                                            onSend(char.toString())
                                        }
                                    }
                                )
                            }
                        }
                    )

                    //简单输入模式
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //关闭按钮
                        SurfaceButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.generic_close),
                            onClick = onClose
                        )

                        ActionBarLayout(
                            modifier = Modifier.weight(1f)
                        )

                        ActionButtonsLayout(
                            inputFocus = inputFocus,
                            keyboardController = keyboardController,
                            onKeyboardClick = {
                                view?.switchKeyboardState()
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (mode == InputBarMode.Floating && enabledActionBar) {
                                    Modifier
                                        //特调操作按钮栏底部边距
                                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)
                                } else {
                                    Modifier.padding(all = 16.dp)
                                }
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        //基础输入法功能栏
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ShowableInputLayout(
                                modifier = Modifier.weight(1f),
                                inputFocus = inputFocus,
                                focusManager = focusManager,
                                keyboardController = keyboardController,
                            )

                            ActionButtonsLayout(
                                inputFocus = inputFocus,
                                keyboardController = keyboardController
                            )
                        }

                        if (mode == InputBarMode.Floating && enabledActionBar) {
                            ActionBarLayout(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBar(
    modifier: Modifier = Modifier,
    onShiftClick: (press: Boolean) -> Unit,
    onCtrlClick: (press: Boolean) -> Unit,
    onTabClick: () -> Unit,
    onEnterClick: () -> Unit,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    var isShiftPress by remember { mutableStateOf(false) }
    var isCtrlPress by remember { mutableStateOf(false) }

    //关闭时，清除按键状态
    DisposableEffect(Unit) {
        onDispose {
            if (isShiftPress) {
                isShiftPress = false
                onShiftClick(false)
            }
            if (isCtrlPress) {
                isCtrlPress = false
                onCtrlClick(false)
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomButton(
            text = "Shift",
            onClick = {
                val press = !isShiftPress
                onShiftClick(press)
                isShiftPress = press
            },
            pressed = isShiftPress
        )

        CustomButton(
            text = "Ctrl",
            onClick = {
                val press = !isCtrlPress
                onCtrlClick(press)
                isCtrlPress = press
            },
            pressed = isCtrlPress
        )

        CustomButton(
            text = "Tab",
            onClick = onTabClick
        )

        CustomButton(
            text = "Enter",
            onClick = onEnterClick
        )

        CustomButton(
            text = "↑",
            onClick = onUpClick
        )

        CustomButton(
            text = "↓",
            onClick = onDownClick
        )

        CustomButton(
            text = "←",
            onClick = onLeftClick
        )

        CustomButton(
            text = "→",
            onClick = onRightClick
        )
    }
}

@Composable
private fun SurfaceButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    iconSize: Dp = 18.dp,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.standardShape,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(color)
) {
    SurfaceButton(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = icon,
                contentDescription = contentDescription
            )
        },
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled
    )
}

@Composable
private fun SurfaceButton(
    modifier: Modifier = Modifier,
    icon: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.standardShape,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(color),
) {
    Surface(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        color = color,
        contentColor = contentColor,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.padding(all = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
private fun CustomButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    pressed: Boolean = false
) {
    if (pressed) {
        Button(
            modifier = modifier,
            onClick = onClick
        ) {
            Text(text = text)
        }
    } else {
        FilledTonalButton(
            modifier = modifier,
            onClick = onClick
        ) {
            Text(text = text)
        }
    }
}