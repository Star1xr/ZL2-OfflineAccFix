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

package com.movtery.zalithlauncher.setting.enums

import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
enum class ChromaMode : Parcelable {
    NONE,
    RGB,
    RED_BLUE,
    SUNSET,
    OCEAN,
    FOREST,
    NEON;

    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ChromaMode> {
            override fun createFromParcel(source: android.os.Parcel): ChromaMode {
                return entries[source.readInt()]
            }

            override fun newArray(size: Int): Array<ChromaMode?> {
                return arrayOfNulls(size)
            }
        }
    }
}
