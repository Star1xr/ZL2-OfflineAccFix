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

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.TableStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.movtery.zalithlauncher.ui.theme.cardColor

@Composable
fun MarkdownView(
    content: String,
    modifier: Modifier = Modifier,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
) {
    RichText(
        modifier = modifier,
        style = richTextStyle
    ) {
        Markdown(content = content)
    }
}

@Composable
fun defaultRichTextStyle(
    influencedByBackground: Boolean = true,
    codeBackground: Color = cardColor(influencedByBackground),
): RichTextStyle {
    return RichTextStyle(
        headingStyle = { level, textStyle ->
            when (level) {
                0 -> TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                1 -> TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                2 -> TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                3 -> TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                4 -> TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                5 -> TextStyle(
                    fontWeight = FontWeight.Bold,
                )
                else -> textStyle
            }
        },
        codeBlockStyle = CodeBlockStyle(
            modifier = Modifier.background(
                color = codeBackground,
                shape = MaterialTheme.shapes.small
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            padding = 8.sp
        ),
        tableStyle = TableStyle(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        stringStyle = RichTextStringStyle(
            linkStyle = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                ),
                pressedStyle = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    background = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            ),
            codeStyle = SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = codeBackground
            )
        )
    )
}
