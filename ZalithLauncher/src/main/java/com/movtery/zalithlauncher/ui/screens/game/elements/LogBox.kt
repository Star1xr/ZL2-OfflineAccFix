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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.game.elements.log_parser.LogHighlighter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections

@Composable
fun LogBox(
    enableLog: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()

    val logList = remember { mutableStateListOf<AnnotatedString>() }
    val buffer = remember { Collections.synchronizedList(mutableListOf<AnnotatedString>()) }

    val logHighlighter = remember { LogHighlighter() }

    val config = remember {
        object {
            /** 缓冲区刷新间隔，单位：ms */
            val BUFFER_FLUSH_INTERVAL: Long = AllSettings.logBufferFlushInterval.getValue().toLong()
        }
    }

    LaunchedEffect(enableLog) {
        if (enableLog) {
            val scrollChannel = Channel<Unit>(capacity = 100)

            LoggerBridge.setListener { log ->
                synchronized(buffer) {
                    val string = logHighlighter.highlight(log)
                    buffer.add(string)
                }
            }

            launch(Dispatchers.Default) {
                val mutex = Mutex()
                while (isActive) {
                    delay(config.BUFFER_FLUSH_INTERVAL)

                    mutex.withLock {
                        synchronized(buffer) {
                            if (buffer.isNotEmpty()) {
                                logList.addAll(buffer)
                                buffer.clear()
                                //尝试进行滚动
                                scrollChannel.trySend(Unit)
                            }
                        }
                    }
                }
            }

            //自动滚动部分
            launch(Dispatchers.Main) {
                scrollChannel.consumeAsFlow().collect {
                    runCatching {
                        val targetIndex = logList.lastIndex
                        if (targetIndex >= 0 && targetIndex < logList.size) {
                            scrollState.animateScrollToItem(targetIndex)
                        }
                    }
                }
            }
        } else {
            LoggerBridge.setListener(null)
            logList.clear()
            buffer.clear()
        }
    }

    if (enableLog) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState
            ) {
                items(logList) { log ->
                    val textSize = AllSettings.logTextSize.state
                    val fontSize = remember(textSize) {
                        TextUnit(textSize.toFloat(), TextUnitType.Sp)
                    }
                    val lineHeight = remember(textSize) {
                        val height = textSize.toFloat() * 1.1f
                        TextUnit(height, TextUnitType.Sp)
                    }

                    Text(
                        text = log,
                        modifier = Modifier.fillParentMaxWidth(),
                        fontSize = fontSize,
                        lineHeight = lineHeight
                    )
                }
            }
        }
    }
}