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

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme

val zl1MenuBarText = Color(0xFFEBF4F3)
val zl1LightBackgroundMenuTop = Color(0xFF2D8B9C)
val zl1LightBackgroundApp = Color(0xFFBDD9DD)
val zl1LightBackgroundOverlay = Color(0xBEFFFFFF)
val zl1LightButtonBackground = Color(0xFFDDE0E1)
val zl1LightButtonBackgroundSelected = Color(0xFFBFC7CA)
val zl1LightPrimaryText = Color(0xFF0E0E0E)
val zl1DarkBackgroundMenuTop = Color(0xFF232323)
val zl1DarkBackgroundApp = Color(0xFF181818)
val zl1DarkBackgroundOverlay = Color(0xBE232323)
val zl1DarkButtonBackground = Color(0xFF3C3C3C)
val zl1DarkButtonBackgroundSelected = Color(0xFF5B5B5B)
val zl1DarkPrimaryText = Color(0xFFFFFFFF)

@Composable
fun menuTopBarBackgroundColor(
    color: Color,
    enabled: Boolean = true,
): Color {
    return if (enabled) {
        val isDark = isLauncherInDarkTheme()
        if (isDark) zl1DarkBackgroundMenuTop else zl1LightBackgroundMenuTop
    } else color
}

@Composable
fun menuTopBarContentColor(
    color: Color,
    enabled: Boolean = true,
): Color {
    return if (enabled) zl1MenuBarText else color
}

/**
 * @param hasBackgroundContent 是否设置了背景图片/视频
 */
@Composable
fun menuBackgroundColor(
    color: Color,
    hasBackgroundContent: Boolean = false,
    enabled: Boolean = true,
): Color {
    return if (enabled) {
        if (hasBackgroundContent) {
            Color.Transparent
        } else {
            val isDark = isLauncherInDarkTheme()
            if (isDark) zl1DarkBackgroundApp else zl1LightBackgroundApp
        }
    } else color
}

@Composable
fun menuContentColor(
    color: Color,
    enabled: Boolean = true,
): Color {
    return if (enabled) {
        val isDark = isLauncherInDarkTheme()
        if (isDark) zl1DarkPrimaryText else zl1LightPrimaryText
    } else color
}

@Composable
fun menuOverlayColor(): Color {
    val isDark = isLauncherInDarkTheme()
    return if (isDark) zl1DarkBackgroundOverlay else zl1LightBackgroundOverlay
}

@Composable
fun menuOverlayContentColor(): Color {
    val isDark = isLauncherInDarkTheme()
    return if (isDark) zl1DarkPrimaryText else zl1LightPrimaryText
}