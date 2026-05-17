/*
 * Zalith Launcher 2
 * Copyright (C) 2025 Star1xr and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.star1xr.treelauncher.utils.settings

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.star1xr.treelauncher.database.AppDatabase
import com.star1xr.treelauncher.game.account.Account
import com.star1xr.treelauncher.game.account.AccountsManager
import com.star1xr.treelauncher.game.account.auth_server.data.AuthServer
import com.star1xr.treelauncher.setting.AllSettings
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.File

@Serializable
data class SettingsExport(
    val settings: Map<String, String>,
    val accounts: List<Account>? = null,
    val authServers: List<AuthServer>? = null,
    val skins: Map<String, String>? = null,
    val capes: Map<String, String>? = null
)

object SettingsTransferUtils {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun getExportDir(): File {
        val dir = File("/storage/emulated/0/zalithplus")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun exportSettings(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val settingsMap = mutableMapOf<String, String>()
            AllSettings.allSettings.forEach { unit ->
                val value = unit.getValue()
                if (value != null) {
                    settingsMap[unit.key] = value.toString()
                }
            }

            val export = SettingsExport(settings = settingsMap)
            val jsonString = json.encodeToString(export)
            
            val exportFile = File(getExportDir(), "settings.json")
            exportFile.writeText(jsonString)
            lInfo("Settings exported to ${exportFile.absolutePath}")
            exportFile
        } catch (e: Exception) {
            lError("Failed to export settings", e)
            null
        }
    }

    suspend fun exportAccounts(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val accounts = db.accountDao().getAllAccounts()
            val authServers = db.authServerDao().getAllServers()
            
            val skinsMap = mutableMapOf<String, String>()
            val capesMap = mutableMapOf<String, String>()
            
            accounts.forEach { account ->
                val skinFile = account.getSkinFile()
                if (skinFile.exists()) {
                    val bytes = FileUtils.readFileToByteArray(skinFile)
                    skinsMap[account.uniqueUUID] = Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
                
                val capeFile = account.getCapeFile()
                if (capeFile.exists()) {
                    val bytes = FileUtils.readFileToByteArray(capeFile)
                    capesMap[account.uniqueUUID] = Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }

            val export = SettingsExport(
                settings = emptyMap(),
                accounts = accounts,
                authServers = authServers,
                skins = skinsMap,
                capes = capesMap
            )
            val jsonString = json.encodeToString(export)
            
            val accountName = accounts.firstOrNull()?.username ?: "backup"
            val exportFile = File(getExportDir(), "account($accountName).json")
            exportFile.writeText(jsonString)
            lInfo("Accounts exported to ${exportFile.absolutePath}")
            exportFile
        } catch (e: Exception) {
            lError("Failed to export accounts", e)
            null
        }
    }

    suspend fun importData(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext false
            
            val export = json.decodeFromString<SettingsExport>(jsonString)
            
            // Import settings
            if (export.settings.isNotEmpty()) {
                AllSettings.allSettings.forEach { unit ->
                    export.settings[unit.key]?.let { valueStr ->
                        try {
                            when (unit.defaultValue) {
                                is Boolean -> (unit as? com.star1xr.treelauncher.setting.unit.AbstractSettingUnit<Boolean>)?.save(valueStr.toBoolean())
                                is Int -> (unit as? com.star1xr.treelauncher.setting.unit.AbstractSettingUnit<Int>)?.save(valueStr.toInt())
                                is Long -> (unit as? com.star1xr.treelauncher.setting.unit.AbstractSettingUnit<Long>)?.save(valueStr.toLong())
                                is String -> (unit as? com.star1xr.treelauncher.setting.unit.AbstractSettingUnit<String>)?.save(valueStr)
                            }
                        } catch (e: Exception) {
                            lError("Failed to import setting ${unit.key}", e)
                        }
                    }
                }
            }

            // Import accounts and auth servers
            val db = AppDatabase.getInstance(context)
            export.authServers?.forEach { server ->
                db.authServerDao().saveServer(server)
            }
            export.accounts?.forEach { account ->
                db.accountDao().saveAccount(account)
            }
            
            // Import skins and capes
            export.skins?.forEach { (uuid, base64) ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    // We need to recreate the account object or path manager logic to find the file
                    // Or just use the known directory
                    val skinFile = File(com.star1xr.treelauncher.path.PathManager.DIR_ACCOUNT_SKIN, "$uuid.png")
                    FileUtils.writeByteArrayToFile(skinFile, bytes)
                } catch (e: Exception) {
                    lError("Failed to import skin for $uuid", e)
                }
            }
            
            export.capes?.forEach { (uuid, base64) ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val capeFile = File(com.star1xr.treelauncher.path.PathManager.DIR_ACCOUNT_CAPE, "$uuid.png")
                    FileUtils.writeByteArrayToFile(capeFile, bytes)
                } catch (e: Exception) {
                    lError("Failed to import cape for $uuid", e)
                }
            }

            if (export.accounts != null || export.authServers != null) {
                AccountsManager.reloadAccounts()
                AccountsManager.reloadAuthServers()
                AccountsManager.refreshWardrobe()
            }
            
            lInfo("Data imported successfully")
            true
        } catch (e: Exception) {
            lError("Failed to import data", e)
            false
        }
    }
}
