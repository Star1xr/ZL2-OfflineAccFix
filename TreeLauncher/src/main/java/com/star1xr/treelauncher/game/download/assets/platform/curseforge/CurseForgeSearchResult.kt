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

package com.star1xr.treelauncher.game.download.assets.platform.curseforge

import com.star1xr.treelauncher.game.download.assets.platform.PlatformClasses
import com.star1xr.treelauncher.game.download.assets.platform.PlatformSearchResult
import com.star1xr.treelauncher.game.download.assets.platform.curseforge.models.CurseForgeData
import com.star1xr.treelauncher.game.download.assets.platform.curseforge.models.CurseForgePagination
import com.star1xr.treelauncher.game.download.assets.platform.searchRankWithChineseBias
import com.star1xr.treelauncher.game.download.assets.utils.getTranslations
import com.star1xr.treelauncher.ui.screens.content.download.assets.elements.AssetsPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeSearchResult(
    /**
     * 响应数据
     */
    @SerialName("data")
    val data: Array<CurseForgeData>,

    /**
     * 响应分页信息
     */
    @SerialName("pagination")
    val pagination: CurseForgePagination
): PlatformSearchResult {
    override fun getAssetsPage(classes: PlatformClasses): AssetsPage {
        val mcmodData = data.map {
            it to classes.getTranslations().getModBySlugId(it.slug)
        }
        val pageSize = pagination.pageSize
        val isLastPage = pagination.resultCount < pageSize ||
                (pagination.index + pagination.resultCount) >= pagination.totalCount

        return AssetsPage(
            pageNumber = pagination.index / pageSize + 1,
            pageIndex = pagination.index,
            totalPage = ((pagination.totalCount + pageSize - 1) / pageSize).toInt(),
            isLastPage = isLastPage,
            data = mcmodData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult {
        val newData = data.toList()
            .searchRankWithChineseBias(searchFilter, classes) { it.slug }
            .toTypedArray()
        return CurseForgeSearchResult(newData, pagination)
    }
}