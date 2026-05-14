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

package com.movtery.zalithlauncher.game.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import com.movtery.zalithlauncher.game.account.wardrobe.CapeFileDownloader
import com.movtery.zalithlauncher.game.account.wardrobe.SkinFileDownloader
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

@Entity(tableName = "accounts")
@Serializable
data class Account(
    /**
     * 唯一 UUID，标识该账号
     */
    @PrimaryKey
    val uniqueUUID: String = UUID.randomUUID().toString().lowercase(),
    var accessToken: String = "0",
    var expiresAt: Long = 0L,
    var clientToken: String = "0",
    var username: String = "Steve",
    var profileId: String = getLocalUUIDWithSkinModel(username, SkinModelType.NONE),
    var refreshToken: String = "0",
    var xUid: String? = null,
    var otherBaseUrl: String? = null,
    var otherAccount: String? = null,
    var otherPassword: String? = null,
    var accountType: String? = null,
    var skinModelType: SkinModelType = SkinModelType.NONE
) {
    val hasSkinFile: Boolean
        get() = getSkinFile().exists()

    fun getSkinFile() = File(PathManager.DIR_ACCOUNT_SKIN, "$uniqueUUID.png")

    fun getCapeFile() = File(PathManager.DIR_ACCOUNT_CAPE, "$uniqueUUID.png")

    /**
     * 下载并更新账号的皮肤文件
     */
    suspend fun downloadYggdrasil() = withContext(Dispatchers.IO) {
        val baseUrl = when {
            isMicrosoftAccount() -> "https://sessionserver.mojang.com"
            isAuthServerAccount() -> otherBaseUrl!!.removeSuffix("/") + "/sessionserver/"
            else -> null
        }
        baseUrl?.let { url ->
            listOf(
                async {
                    updateSkin(url)
                },
                async {
                    updateCape(url)
                }
            ).joinAll()
        }
    }

    private suspend fun updateSkin(url: String) {
        val skinFile = getSkinFile()
        if (skinFile.exists()) FileUtils.deleteQuietly(skinFile) //清除一次皮肤文件

        runCatching {
            SkinFileDownloader().download(url, skinFile, profileId) { modelType ->
                this.skinModelType = modelType
            }
            lInfo("Update skin success")
        }.onFailure { e ->
            lError("Could not update skin", e)
        }
        AccountsManager.refreshWardrobe()
    }

    private suspend fun updateCape(url: String) {
        val capeFile = getCapeFile()
        if (capeFile.exists()) FileUtils.deleteQuietly(capeFile) //清除一次披风文件

        runCatching {
            CapeFileDownloader().download(url, capeFile, profileId)
            lInfo("Update cape success")
        }.onFailure { e ->
            lError("Could not update cape", e)
        }
        AccountsManager.refreshWardrobe()
    }
}