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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor

@Composable
fun defaultMDTypography(
    h1: TextStyle = MaterialTheme.typography.titleLarge, //22sp
    h2: TextStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
    h3: TextStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
    h4: TextStyle = MaterialTheme.typography.titleMedium, //16sp
    h5: TextStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), //15sp
    h6: TextStyle = MaterialTheme.typography.titleSmall, //14sp
    text: TextStyle = MaterialTheme.typography.bodyMedium,
    code: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    inlineCode: TextStyle = text.copy(fontFamily = FontFamily.Monospace),
    quote: TextStyle = MaterialTheme.typography.bodyMedium.plus(SpanStyle(fontStyle = FontStyle.Italic)),
    paragraph: TextStyle = MaterialTheme.typography.bodyMedium,
    ordered: TextStyle = MaterialTheme.typography.bodyMedium,
    bullet: TextStyle = MaterialTheme.typography.bodyMedium,
    list: TextStyle = MaterialTheme.typography.bodyMedium,
    textLink: TextLinkStyles = TextLinkStyles(
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
    table: TextStyle = text,
) = markdownTypography(
    h1 = h1, h2 = h2, h3 = h3, h4 = h4, h5 = h5, h6 = h6,
    text = text, code = code, inlineCode = inlineCode,
    quote = quote,
    paragraph = paragraph,
    ordered = ordered,
    bullet = bullet,
    list = list,
    textLink = textLink,
    table = table
)

@Composable
fun markdownColorsOnBackground(
    influencedByBackground: Boolean = true,
    text: Color = onBackgroundColor(),
    codeBackground: Color = cardColor(influencedByBackground),
    inlineCodeBackground: Color = codeBackground,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant,
    tableBackground: Color = codeBackground,
) = markdownColor(
    text = text,
    codeBackground = codeBackground,
    inlineCodeBackground = inlineCodeBackground,
    dividerColor = dividerColor,
    tableBackground = tableBackground
)