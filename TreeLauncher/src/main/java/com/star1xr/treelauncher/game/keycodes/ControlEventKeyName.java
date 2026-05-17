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

package com.star1xr.treelauncher.game.keycodes;

import static com.star1xr.treelauncher.game.keycodes.ControlEventKeycode.*;

import androidx.annotation.Nullable;

public class ControlEventKeyName {
    /**
     * 利用Java语言switch的性能，快速匹配键对应的显示名称
     */
    public static @Nullable String getNameByKey(String key) {
        switch (key) {
            case GLFW_KEY_SPACE: return "Space";
            case GLFW_KEY_APOSTROPHE: return "'";
            case GLFW_KEY_COMMA: return ",";
            case GLFW_KEY_MINUS: return "-";
            case GLFW_KEY_PERIOD: return ".";
            case GLFW_KEY_SLASH: return "/";
            case GLFW_KEY_0: return "0";
            case GLFW_KEY_1: return "1";
            case GLFW_KEY_2: return "2";
            case GLFW_KEY_3: return "3";
            case GLFW_KEY_4: return "4";
            case GLFW_KEY_5: return "5";
            case GLFW_KEY_6: return "6";
            case GLFW_KEY_7: return "7";
            case GLFW_KEY_8: return "8";
            case GLFW_KEY_9: return "9";
            case GLFW_KEY_SEMICOLON: return ";";
            case GLFW_KEY_EQUAL: return "+";
            case GLFW_KEY_A: return "A";
            case GLFW_KEY_B: return "B";
            case GLFW_KEY_C: return "C";
            case GLFW_KEY_D: return "D";
            case GLFW_KEY_E: return "E";
            case GLFW_KEY_F: return "F";
            case GLFW_KEY_G: return "G";
            case GLFW_KEY_H: return "H";
            case GLFW_KEY_I: return "I";
            case GLFW_KEY_J: return "J";
            case GLFW_KEY_K: return "K";
            case GLFW_KEY_L: return "L";
            case GLFW_KEY_M: return "M";
            case GLFW_KEY_N: return "N";
            case GLFW_KEY_O: return "O";
            case GLFW_KEY_P: return "P";
            case GLFW_KEY_Q: return "Q";
            case GLFW_KEY_R: return "R";
            case GLFW_KEY_S: return "S";
            case GLFW_KEY_T: return "T";
            case GLFW_KEY_U: return "U";
            case GLFW_KEY_V: return "V";
            case GLFW_KEY_W: return "W";
            case GLFW_KEY_X: return "X";
            case GLFW_KEY_Y: return "Y";
            case GLFW_KEY_Z: return "Z";
            case GLFW_KEY_LEFT_BRACKET: return "[";
            case GLFW_KEY_BACKSLASH: return "\\";
            case GLFW_KEY_RIGHT_BRACKET: return "]";
            case GLFW_KEY_GRAVE_ACCENT: return "`";
            case GLFW_KEY_ESCAPE: return "Esc";
            case GLFW_KEY_ENTER: return "Enter";
            case GLFW_KEY_TAB: return "Tab";
            case GLFW_KEY_BACKSPACE: return "Backspace";
            case GLFW_KEY_INSERT: return "Insert";
            case GLFW_KEY_DELETE: return "Delete";
            case GLFW_KEY_RIGHT: return "→";
            case GLFW_KEY_LEFT: return "←";
            case GLFW_KEY_DOWN: return "↓";
            case GLFW_KEY_UP: return "↑";
            case GLFW_KEY_PAGE_UP: return "Page Up";
            case GLFW_KEY_PAGE_DOWN: return "Page Down";
            case GLFW_KEY_HOME: return "Home";
            case GLFW_KEY_END: return "End";
            case GLFW_KEY_CAPS_LOCK: return "Caps Lock";
            case GLFW_KEY_SCROLL_LOCK: return "Scroll Lock";
            case GLFW_KEY_NUM_LOCK: return "Num lock";
            case GLFW_KEY_PRINT_SCREEN: return "Print Screen";
            case GLFW_KEY_PAUSE: return "Pause";
            case GLFW_KEY_F1: return "F1";
            case GLFW_KEY_F2: return "F2";
            case GLFW_KEY_F3: return "F3";
            case GLFW_KEY_F4: return "F4";
            case GLFW_KEY_F5: return "F5";
            case GLFW_KEY_F6: return "F6";
            case GLFW_KEY_F7: return "F7";
            case GLFW_KEY_F8: return "F8";
            case GLFW_KEY_F9: return "F9";
            case GLFW_KEY_F10: return "F10";
            case GLFW_KEY_F11: return "F11";
            case GLFW_KEY_F12: return "F12";
            case GLFW_KEY_F13: return "F13";
            case GLFW_KEY_F14: return "F14";
            case GLFW_KEY_F15: return "F15";
            case GLFW_KEY_F16: return "F16";
            case GLFW_KEY_F17: return "F17";
            case GLFW_KEY_F18: return "F18";
            case GLFW_KEY_F19: return "F19";
            case GLFW_KEY_F20: return "F20";
            case GLFW_KEY_F21: return "F21";
            case GLFW_KEY_F22: return "F22";
            case GLFW_KEY_F23: return "F23";
            case GLFW_KEY_F24: return "F24";
            case GLFW_KEY_F25: return "F25";
            case GLFW_KEY_KP_0: return "Num 0";
            case GLFW_KEY_KP_1: return "Num 1";
            case GLFW_KEY_KP_2: return "Num 2";
            case GLFW_KEY_KP_3: return "Num 3";
            case GLFW_KEY_KP_4: return "Num 4";
            case GLFW_KEY_KP_5: return "Num 5";
            case GLFW_KEY_KP_6: return "Num 6";
            case GLFW_KEY_KP_7: return "Num 7";
            case GLFW_KEY_KP_8: return "Num 8";
            case GLFW_KEY_KP_9: return "Num 9";
            case GLFW_KEY_KP_DECIMAL: return "Num .";
            case GLFW_KEY_KP_DIVIDE: return "Num /";
            case GLFW_KEY_KP_MULTIPLY: return "Num *";
            case GLFW_KEY_KP_SUBTRACT: return "Num -";
            case GLFW_KEY_KP_ADD: return "Num +";
            case GLFW_KEY_KP_ENTER: return "Num Enter";
            case GLFW_KEY_LEFT_SHIFT: return "Left Shift";
            case GLFW_KEY_LEFT_CONTROL: return "Left Control";
            case GLFW_KEY_LEFT_ALT: return "Left Alt";
            case GLFW_KEY_RIGHT_SHIFT: return "Right Shift";
            case GLFW_KEY_RIGHT_CONTROL: return "Right Ctrl";
            case GLFW_KEY_RIGHT_ALT: return "Right Alt";
            default:
                return null;
        }
    }
}
