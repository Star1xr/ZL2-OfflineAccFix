/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 */

package com.movtery.zalithlauncher.ui.screens.main.custom_home

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

enum class HomeButtonType {
    Filled, Outlined, FilledTonal, Text
}

/**
 * 自定义主页内的按钮组件
 */
@Composable
fun CustomHomeButton(
    text: String,
    event: String?,
    type: HomeButtonType,
    onLauncherEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val onClick: () -> Unit = {
        event?.let { e ->
            when {
                e.startsWith("url=") -> {
                    val url = e.substringAfter("url=")
                    runCatching {
                        uriHandler.openUri(url)
                    }
                }
                e.startsWith("launcher=") -> {
                    val action = e.substringAfter("launcher=")
                    onLauncherEvent(action)
                }
                else -> {
                    //忽略无法解析的事件
                }
            }
        }
    }

    val content: @Composable (RowScope.() -> Unit) = @Composable {
        Text(text = text)
    }

    val buttonModifier = modifier.padding(vertical = 4.dp)

    when (type) {
        HomeButtonType.Filled -> {
            Button(
                modifier = buttonModifier,
                onClick = onClick,
                content = content
            )
        }
        HomeButtonType.Outlined -> {
            OutlinedButton(
                modifier = buttonModifier,
                onClick = onClick,
                content = content
            )
        }
        HomeButtonType.FilledTonal -> {
            FilledTonalButton(
                modifier = buttonModifier,
                onClick = onClick,
                content = content
            )
        }
        HomeButtonType.Text -> {
            TextButton(
                modifier = buttonModifier,
                onClick = onClick,
                content = content
            )
        }
    }
}
