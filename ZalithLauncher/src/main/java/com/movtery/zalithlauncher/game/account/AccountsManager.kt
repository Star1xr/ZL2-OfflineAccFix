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
 * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.account

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.database.AppDatabase
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer
import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServerDao
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.network.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.util.concurrent.CopyOnWriteArrayList

object AccountsManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Account state management
    private val _accounts = CopyOnWriteArrayList<Account>()
    private val _accountsFlow = MutableStateFlow<List<Account>>(emptyList())
    val accountsFlow = _accountsFlow.asStateFlow()

    private val _currentAccountFlow = MutableStateFlow<Account?>(null)
    val currentAccountFlow = _currentAccountFlow.asStateFlow()

    // Auth server state management
    private val _authServers = CopyOnWriteArrayList<AuthServer>()
    private val _authServersFlow = MutableStateFlow<List<AuthServer>>(emptyList())
    val authServersFlow = _authServersFlow.asStateFlow()

    private val _refreshWardrobe = MutableStateFlow(false)
    /** Controls refreshing all account wardrobes */
    val refreshWardrobe = _refreshWardrobe.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()

    private lateinit var database: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var authServerDao: AuthServerDao

    /**
     * Initializes the account system
     */
    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        accountDao = database.accountDao()
        authServerDao = database.authServerDao()
    }

    /**
     * Refreshes currently logged-in accounts from the database
     */
    fun reloadAccounts() {
        scope.launch {
            suspendReloadAccounts()
        }
    }

    /**
     * Triggers a wardrobe refresh toggle
     */
    fun refreshWardrobe() {
        _refreshWardrobe.update { !it }
    }

    private suspend fun suspendReloadAccounts() {
        val loadedAccounts = accountDao.getAllAccounts()
        _accounts.clear()
        _accounts.addAll(loadedAccounts.sortedWith(
            compareBy<Account>({ it.accountTypePriority() }, { it.username })
        ))

        _accountsFlow.update { _accounts.toList() }

        val currentId = AllSettings.currentAccount.getValue()
        if (_accounts.isNotEmpty() && !isAccountExists(currentId)) {
            setCurrentAccount(_accounts[0])
        } else {
            refreshCurrentAccountState()
        }

        lInfo("Loaded ${_accounts.size} accounts")
    }

    /**
     * Refreshes saved auth servers from the database
     */
    fun reloadAuthServers() {
        scope.launch {
            val loadedServers = authServerDao.getAllServers()
            _authServers.clear()
            _authServers.addAll(loadedServers.sortedBy { it.serverName })
            
            _authServersFlow.update { _authServers.toList() }
            lInfo("Loaded ${_authServers.size} auth servers")
        }
    }

    /**
     * Performs login operation
     */
    fun performLogin(
        context: Context,
        account: Account,
        onSuccess: suspend (Account, task: Task) -> Unit = { _, _ -> },
        onFailed: (th: Throwable) -> Unit = {}
    ) {
        performLoginTask(context, account, onSuccess, onFailed)?.let { 
            TaskSystem.submitTask(it) 
        }
    }

    /**
     * Generates a login task based on account type
     */
    fun performLoginTask(
        context: Context,
        account: Account,
        onSuccess: suspend (Account, task: Task) -> Unit = { _, _ -> },
        onFailed: (th: Throwable) -> Unit = {},
        onFinally: () -> Unit = {}
    ): Task? = when {
        account.isNoLoginRequired() -> null
        account.isAuthServerAccount() -> {
            otherLogin(context, account, onSuccess, onFailed, onFinally)
        }
        account.isMicrosoftAccount() -> {
            microsoftRefresh(account, onSuccess, onFailed, onFinally)
        }
        else -> null
    }

    /**
     * Refreshes account data if network is available
     */
    fun refreshAccount(
        context: Context,
        account: Account,
        onFailed: (th: Throwable) -> Unit = {},
    ) {
        if (isNetworkAvailable(context)) {
            performLogin(
                context = context,
                account = account,
                onSuccess = { updatedAccount, task ->
                    task.updateMessage(R.string.account_logging_in_saving)
                    updatedAccount.downloadYggdrasil()
                    suspendSaveAccount(updatedAccount)
                },
                onFailed = onFailed
            )
        }
    }

    /**
     * Gets the current active account
     */
    private fun getCurrentAccount(): Account? {
        val currentId = AllSettings.currentAccount.getValue()
        return _accounts.find { it.uniqueUUID == currentId } ?: _accounts.firstOrNull()
    }

    /**
     * Sets and persists the current active account
     */
    fun setCurrentAccount(account: Account) {
        AllSettings.currentAccount.save(account.uniqueUUID)
        refreshCurrentAccountState()
    }

    /**
     * Syncs current account state with UI flows
     */
    private fun refreshCurrentAccountState() {
        val currentAccount = getCurrentAccount()
        val isOffline = false // Reserved for future logic
        _currentAccountFlow.update { if (isOffline) null else currentAccount }
        _isOffline.update { isOffline }
    }

    /**
     * Saves account to DB asynchronously
     */
    fun saveAccount(account: Account) {
        scope.launch {
            suspendSaveAccount(account)
        }
    }

    /**
     * Suspend function to save account and reload list
     */
    suspend fun suspendSaveAccount(account: Account) {
        runCatching {
            accountDao.saveAccount(account)
            lInfo("Saved account: ${account.username}")
        }.onFailure { e ->
            lError("Failed to save account: ${account.username}", e)
        }
        suspendReloadAccounts()
    }

    /**
     * Deletes account and associated files
     */
    fun deleteAccount(account: Account) {
        scope.launch {
            accountDao.deleteAccount(account)
            FileUtils.deleteQuietly(account.getSkinFile())
            suspendReloadAccounts()
        }
    }

    /**
     * Saves auth server to DB
     */
    suspend fun saveAuthServer(server: AuthServer) {
        runCatching {
            authServerDao.saveServer(server)
            lInfo("Saved auth server: ${server.serverName} -> ${server.baseUrl}")
        }.onFailure { e ->
            lError("Failed to save auth server: ${server.serverName}", e)
        }
        reloadAuthServers()
    }

    /**
     * Deletes auth server from DB
     */
    fun deleteAuthServer(server: AuthServer) {
        scope.launch {
            authServerDao.deleteServer(server)
            reloadAuthServers()
        }
    }

    fun hasMicrosoftAccount(): Boolean = _accounts.any { it.isMicrosoftAccount() }

    fun loadFromProfileID(profileId: String, accountType: String? = null): Account? =
        _accounts.find { it.profileId == profileId && (accountType == null || it.accountType == accountType) }

    fun isAccountExists(uniqueUUID: String): Boolean =
        uniqueUUID.isNotEmpty() && _accounts.any { it.uniqueUUID == uniqueUUID }

    fun isAuthServerExists(baseUrl: String): Boolean =
        baseUrl.isNotEmpty() && _authServers.any { it.baseUrl == baseUrl }
}
