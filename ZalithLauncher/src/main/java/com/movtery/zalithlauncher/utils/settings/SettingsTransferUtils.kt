/*
 * Zalith Launcher 2
 * Copyright (C) 2025 Star1xr and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.utils.settings

import android.content.Context
import android.net.Uri
import com.movtery.zalithlauncher.database.AppDatabase
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SettingsExport(
    val settings: Map<String, String>,
    val accounts: List<Account>? = null,
    val authServers: List<AuthServer>? = null
)

object SettingsTransferUtils {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
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
            
            val exportFile = File(context.cacheDir, "zalith_settings_export.json")
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

            val export = SettingsExport(
                settings = emptyMap(),
                accounts = accounts,
                authServers = authServers
            )
            val jsonString = json.encodeToString(export)
            
            val exportFile = File(context.cacheDir, "zalith_accounts_export.json")
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
                            // This is a bit hacky because we don't have a generic "parse" in SettingUnit
                            // But for simple types it works. For Enums/Parcelables we might need more logic.
                            when (unit.defaultValue) {
                                is Boolean -> (unit as? com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit<Boolean>)?.save(valueStr.toBoolean())
                                is Int -> (unit as? com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit<Int>)?.save(valueStr.toInt())
                                is Long -> (unit as? com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit<Long>)?.save(valueStr.toLong())
                                is String -> (unit as? com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit<String>)?.save(valueStr)
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

            if (export.accounts != null || export.authServers != null) {
                AccountsManager.reloadAccounts()
                AccountsManager.reloadAuthServers()
            }
            
            lInfo("Data imported successfully")
            true
        } catch (e: Exception) {
            lError("Failed to import data", e)
            false
        }
    }
}
