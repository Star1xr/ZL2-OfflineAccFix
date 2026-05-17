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

package com.star1xr.treelauncher.ui.code_editor

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.ui.theme.onBackgroundColor
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub

/**
 * 编辑器内容状态
 */
sealed interface EditorState {
    data object Loading : EditorState
    /** 编辑器内容加载完成 */
    data class Success(val content: Content) : EditorState
}

@Composable
fun SoraEditor(
    modifier: Modifier = Modifier,
    state: EditorState,
    isReadOnly: Boolean = false,
    language: Language? = null,
    scheme: EditorColorScheme = SchemeGitHub(),
    onSaveClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = onBackgroundColor(),
        floatingActionButton = {
            if (!isReadOnly && state is EditorState.Success) {
                FloatingActionButton(
                    onClick = onSaveClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save_filled),
                        contentDescription = stringResource(R.string.generic_save)
                    )
                }
            }
        }
    ) { paddingValues ->
        var view by remember {
            mutableStateOf<CodeEditor?>(null)
        }

        val controller = LocalSoftwareKeyboardController.current
        DisposableEffect(Unit) {
            onDispose {
                view?.hideSoftInput()
                controller?.hide()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is EditorState.Loading -> {
                    LoadingIndicator()
                }

                is EditorState.Success -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            CodeEditor(ctx).apply {
                                typefaceText = Typeface.MONOSPACE
                                setLineNumberEnabled(true)
                                setPinLineNumber(true)
                                isEditable = !isReadOnly
                                setText(state.content, true, null)
                            }.also { view = it }
                        },
                        update = { view ->
                            if (view.text != state.content) {
                                view.setText(state.content, true, null)
                            }
                            if (view.editorLanguage != language) {
                                view.setEditorLanguage(language)
                            }
                            if (view.isEditable == isReadOnly) {
                                view.isEditable = !isReadOnly
                            }
                            if (view.colorScheme !== scheme) {
                                view.colorScheme = scheme
                            }
                        },
                        onRelease = { view ->
                            view.release()
                        }
                    )
                }
            }
        }
    }
}
