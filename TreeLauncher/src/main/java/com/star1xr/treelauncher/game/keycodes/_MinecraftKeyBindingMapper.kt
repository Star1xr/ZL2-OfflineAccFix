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

package com.star1xr.treelauncher.game.keycodes

import com.star1xr.treelauncher.game.launch.MCOptions

//W
const val MOVEMENT_FORWARD = "key_key.forward"
const val MOVEMENT_FORWARD_VALUE = "key.keyboard.w"
//A
const val MOVEMENT_LEFT = "key_key.left"
const val MOVEMENT_LEFT_VALUE = "key.keyboard.a"
//S
const val MOVEMENT_BACK = "key_key.back"
const val MOVEMENT_BACK_VALUE = "key.keyboard.s"
//D
const val MOVEMENT_RIGHT = "key_key.right"
const val MOVEMENT_RIGHT_VALUE = "key.keyboard.d"

//Hotbar
const val HOTBAR_1 =        "key_key.hotbar.1"
const val HOTBAR_2 =        "key_key.hotbar.2"
const val HOTBAR_3 =        "key_key.hotbar.3"
const val HOTBAR_4 =        "key_key.hotbar.4"
const val HOTBAR_5 =        "key_key.hotbar.5"
const val HOTBAR_6 =        "key_key.hotbar.6"
const val HOTBAR_7 =        "key_key.hotbar.7"
const val HOTBAR_8 =        "key_key.hotbar.8"
const val HOTBAR_9 =        "key_key.hotbar.9"
const val HOTBAR_1_VALUE =  "key.keyboard.1"
const val HOTBAR_2_VALUE =  "key.keyboard.2"
const val HOTBAR_3_VALUE =  "key.keyboard.3"
const val HOTBAR_4_VALUE =  "key.keyboard.4"
const val HOTBAR_5_VALUE =  "key.keyboard.5"
const val HOTBAR_6_VALUE =  "key.keyboard.6"
const val HOTBAR_7_VALUE =  "key.keyboard.7"
const val HOTBAR_8_VALUE =  "key.keyboard.8"
const val HOTBAR_9_VALUE =  "key.keyboard.9"

//Chat
const val OPEN_CHAT = "key_key.chat"
const val OPEN_CHAT_VALUE = "key.keyboard.t"

//Spring
const val SPRING = "key_key.sprint"
const val SPRING_VALUE = "key.keyboard.left.control"

/**
 * 将字符串键映射到其对应的键码
 * @return 如果找到映射则返回键码，否则返回 `null`
 */
fun mapToKeycode(bindingKey: String?, defaultValue: String): Int? {
    val binding = bindingKey?.let { MCOptions.get(it) } ?: defaultValue

    return if (binding.startsWith("key.")) {
        //新版MC键绑定映射
        MinecraftKeyBindingMapper.getGlfwKeycode(binding)?.toInt()
    } else {
        binding.toIntOrNull()?.let { lwjgl2Code ->
            //MC旧版本直接存了LWJGL2的键值
            //将旧版本LWJGL2的键码转换为GLFW
            Lwjgl2Keycode.lwjgl2ToGlfw(lwjgl2Code)
        }
    }
}

/**
 * 将字符串键映射到其对应的控制布局事件标识
 * @return 如果找到映射则返回对应的标识，否则返回 `null`
 */
fun mapToControlEvent(bindingKey: String?, defaultValue: String): String? {
    val binding = bindingKey?.let { MCOptions.get(it) } ?: defaultValue

    return if (binding.startsWith("key.")) {
        MinecraftKeyBindingMapper.getControlEvent(binding)
    } else {
        binding.toIntOrNull()?.let { lwjgl2Code ->
            //MC旧版本直接存了LWJGL2的键值
            //将旧版本LWJGL2的键码转换为控制事件标识
            Lwjgl2Keycode.lwjgl2ToControlEvent(lwjgl2Code)
        }
    }
}