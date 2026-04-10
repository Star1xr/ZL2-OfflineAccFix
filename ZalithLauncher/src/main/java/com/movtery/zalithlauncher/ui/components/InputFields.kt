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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.utils.string.toSingleLine

@Composable
fun SimpleTextInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(percent = 50),
    contextPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    textStyle: TextStyle = TextStyle(color = contentColor).copy(fontSize = 12.sp),
    cursorBrush: Brush = SolidColor(LocalTextSelectionColors.current.handleColor),
    singleLine: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        val focusManager = LocalFocusManager.current

        BasicTextField(
            modifier = Modifier
                .wrapContentHeight()
                .padding(contextPadding),
            value = value,
            onValueChange = { new ->
                onValueChange(
                    if (singleLine) new.toSingleLine() else new
                )
            },
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(true)
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        hint?.invoke()
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun SmallOutlinedEditField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: (@Composable () -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    textStyle: TextStyle = TextStyle(color = contentColor).copy(fontSize = 12.sp),
    cursorBrush: Brush = SolidColor(LocalTextSelectionColors.current.handleColor),
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    singleLine: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val focusManager = LocalFocusManager.current

    val borderWidth by animateDpAsState(
        if (isFocused) 2.dp else 1.dp
    )
    val borderColor by animateColorAsState(
        if (isFocused) colors.focusedIndicatorColor else colors.unfocusedIndicatorColor
    )
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = { new ->
            onValueChange(
                if (singleLine) new.toSingleLine() else new
            )
        },
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions
            ?: if (singleLine) KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            else KeyboardOptions.Default,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onDone?.invoke(this@KeyboardActions)
            },
            onGo = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onGo?.invoke(this@KeyboardActions)
            },
            onNext = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onNext?.invoke(this@KeyboardActions)
            },
            onPrevious = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onPrevious?.invoke(this@KeyboardActions)
            },
            onSearch = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onSearch?.invoke(this@KeyboardActions)
            },
            onSend = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onSend?.invoke(this@KeyboardActions)
            }
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(width = borderWidth, color = borderColor, shape = shape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        hint?.invoke()
                    }
                    innerTextField()
                }
            }
        },
        interactionSource = interactionSource,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = { string ->
            if (singleLine) {
                onValueChange(string.toSingleLine())
            } else {
                onValueChange(string)
            }
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
            ?: if (singleLine) KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            else KeyboardOptions.Default,
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onDone?.invoke(this@KeyboardActions)
            },
            onGo = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onGo?.invoke(this@KeyboardActions)
            },
            onNext = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onNext?.invoke(this@KeyboardActions)
            },
            onPrevious = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onPrevious?.invoke(this@KeyboardActions)
            },
            onSearch = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onSearch?.invoke(this@KeyboardActions)
            },
            onSend = {
                focusManager.clearFocus(true)
                (keyboardActions ?: KeyboardActions.Default).onSend?.invoke(this@KeyboardActions)
            }
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
}