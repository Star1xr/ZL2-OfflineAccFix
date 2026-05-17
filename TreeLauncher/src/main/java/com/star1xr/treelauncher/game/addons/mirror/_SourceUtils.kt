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

package com.star1xr.treelauncher.game.addons.mirror

import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

data class MirrorSource<T>(
    val delayMillis: Long = 0L,
    val type: SourceType,
    val block: suspend () -> T?
)

suspend fun <T> runMirrorable(
    sources: List<MirrorSource<T>>
): T? = withContext(Dispatchers.IO) {
    var result: T? = null
    var succeed = false
    var lastException: Throwable? = null

    loop@ for (source in sources) {
        ensureActive()
        if (source.delayMillis > 0) {
            delay(source.delayMillis)
        }
        ensureActive()

        runCatching {
            val res = source.block()
            result = res
            succeed = true
            break@loop
        }.onFailure {
            lDebug("Source ${source.type.displayName} failed!", it)
            lastException = it
        }
    }

    if (!succeed) throw lastException ?: IOException("Failed to retrieve information from the source!")

    result
}