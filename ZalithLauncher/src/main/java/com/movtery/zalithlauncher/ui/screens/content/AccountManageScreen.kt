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

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.COPY_LABEL_ACCOUNT_UUID
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.capeTranslatedName
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginItem
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeCapeOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginTipDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SelectCapeDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SelectSkinModelDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerItem
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.copyText
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.AccountManageEffect
import com.movtery.zalithlauncher.viewmodel.AccountManageIntent
import com.movtery.zalithlauncher.viewmodel.AccountManageUiState
import com.movtery.zalithlauncher.viewmodel.AccountManageViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 封装 UI 交互回调，减少参数数量
 */
private data class AccountActions(
    val onIntent: (AccountManageIntent) -> Unit,
    val openLink: (url: String) -> Unit,
    val backToMainScreen: () -> Unit,
    val navigateToWeb: (url: String) -> Unit,
    val checkIfInWebScreen: () -> Boolean,
    val formatError: (Context, Throwable) -> String,
    val submitError: (ErrorViewModel.ThrowableMessage) -> Unit
)

/**
 * 账号管理界面
 * 提供微软登录、离线登录、第三方服务器登录以及账号信息管理功能
 */
@Composable
fun AccountManageScreen(
    backStackViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    openLink: (url: String) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    viewModel: AccountManageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 处理来自 ViewModel 的一次性副作用 (Effect)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccountManageEffect.ShowError -> {
                    submitError(ErrorViewModel.ThrowableMessage(effect.title, effect.message))
                }

                is AccountManageEffect.ShowToast -> {
                    val message = if (effect.formatArgs.isEmpty()) {
                        context.getString(effect.messageRes)
                    } else {
                        context.getString(effect.messageRes, *effect.formatArgs.toTypedArray())
                    }
                    Toast.makeText(context, message, effect.duration).show()
                }
            }
        }
    }

    val actions = remember(viewModel, backToMainScreen, openLink, backStackViewModel, submitError) {
        AccountActions(
            onIntent = viewModel::onIntent,
            openLink = openLink,
            backToMainScreen = backToMainScreen,
            navigateToWeb = { url -> backStackViewModel.mainScreen.backStack.navigateToWeb(url) },
            checkIfInWebScreen = { backStackViewModel.mainScreen.currentKey is NormalNavKey.WebScreen },
            formatError = { ctx, th -> viewModel.formatAccountError(ctx, th) },
            submitError = submitError
        )
    }

    BaseScreen(
        screenKey = NormalNavKey.AccountManager,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        AccountManageContent(
            isVisible = isVisible,
            uiState = uiState,
            actions = actions
        )
    }
}

/**
 * 账号管理界面内容布局
 */
@Composable
private fun AccountManageContent(
    isVisible: Boolean,
    uiState: AccountManageUiState,
    actions: AccountActions
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 左侧：登录方式与服务器列表菜单
        ServerTypeMenu(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = 12.dp)
                .weight(3f),
            authServers = uiState.authServers,
            actions = actions
        )
        // 右侧：已添加的账号列表
        AccountsLayout(
            isVisible = isVisible,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                .weight(7f),
            accounts = uiState.accounts,
            currentAccount = uiState.currentAccount,
            accountOperation = uiState.accountOperation,
            accountSkinOperationMap = uiState.accountSkinOperationMap,
            actions = actions
        )
    }

    // 各类业务逻辑的操作处理组件（通常包含 Dialog 或副作用触发）
    MicrosoftLoginOperation(uiState.microsoftLoginOperation, actions)
    MicrosoftChangeSkinOperation(uiState.microsoftChangeSkinOperation, actions)
    MicrosoftChangeCapeOperation(uiState.microsoftChangeCapeOperation, actions)
    LocalLoginOperation(uiState.localLoginOperation, actions)
    OtherLoginOperation(uiState.otherLoginOperation, actions)
    ServerTypeOperation(uiState.serverOperation, actions)
}

/**
 * 服务器与登录类型菜单
 */
@Composable
private fun ServerTypeMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    authServers: List<com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer>,
    actions: AccountActions
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier
            .offset { IntOffset(x = xOffset.roundToPx(), y = 0) }
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            // 微软登录入口
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                serverName = stringResource(R.string.account_type_microsoft),
            ) {
                if (!isMicrosoftLogging()) {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftLoginOp(
                            MicrosoftLoginOperation.Tip
                        )
                    )
                }
            }
            // 离线登录入口
            LoginItem(
                modifier = Modifier.fillMaxWidth(),
                serverName = stringResource(R.string.account_type_local)
            ) {
                actions.onIntent(AccountManageIntent.UpdateLocalLoginOp(LocalLoginOperation.Edit))
            }

            // 自定义验证服务器入口
            authServers.forEach { server ->
                ServerItem(
                    server = server,
                    onClick = {
                        actions.onIntent(
                            AccountManageIntent.UpdateOtherLoginOp(
                                OtherLoginOperation.OnLogin(server)
                            )
                        )
                    },
                    onDeleteClick = {
                        actions.onIntent(
                            AccountManageIntent.UpdateServerOp(
                                ServerOperation.Delete(server)
                            )
                        )
                    }
                )
            }
        }

        // 添加新服务器按钮
        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = { actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.AddNew)) }
        ) {
            MarqueeText(text = stringResource(R.string.account_add_new_server_button))
        }
    }
}

/**
 * 微软登录操作逻辑处理
 */
@Composable
private fun MicrosoftLoginOperation(
    operation: MicrosoftLoginOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.Tip -> {
            MicrosoftLoginTipDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftLoginOp(
                            MicrosoftLoginOperation.None
                        )
                    )
                },
                onConfirm = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftLoginOp(
                            MicrosoftLoginOperation.None
                        )
                    )
                    actions.onIntent(
                        AccountManageIntent.PerformMicrosoftLogin(
                            context = context,
                            toWeb = actions.navigateToWeb,
                            backToMain = actions.backToMainScreen,
                            checkIfInWebScreen = actions.checkIfInWebScreen
                        )
                    )
                },
                openLink = actions.openLink
            )
        }

        is MicrosoftLoginOperation.RunTask -> {}
    }
}

/**
 * 微软皮肤更换操作逻辑处理
 */
@Composable
private fun MicrosoftChangeSkinOperation(
    operation: MicrosoftChangeSkinOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeSkinOperation.None -> {}
        is MicrosoftChangeSkinOperation.ImportFile -> {
            LaunchedEffect(operation) {
                actions.onIntent(
                    AccountManageIntent.ImportSkinFile(
                        context,
                        operation.account,
                        operation.uri
                    )
                )
            }
        }

        is MicrosoftChangeSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftSkinOp(
                            MicrosoftChangeSkinOperation.None
                        )
                    )
                },
                onSelected = { type ->
                    actions.onIntent(AccountManageIntent.UpdateMicrosoftSkinOp(MicrosoftChangeSkinOperation.None)) // Bug 1 fix
                    actions.onIntent(
                        AccountManageIntent.UploadMicrosoftSkin(
                            context,
                            operation.account,
                            operation.file,
                            type
                        )
                    )
                }
            )
        }

        is MicrosoftChangeSkinOperation.RunTask -> {}
    }
}

/**
 * 微软披风更换操作逻辑处理
 */
@Composable
private fun MicrosoftChangeCapeOperation(
    operation: MicrosoftChangeCapeOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeCapeOperation.None -> {}
        is MicrosoftChangeCapeOperation.FetchProfiles -> {
            LaunchedEffect(operation) {
                actions.onIntent(
                    AccountManageIntent.FetchMicrosoftCapes(
                        context,
                        operation.account
                    )
                )
            }
        }

        is MicrosoftChangeCapeOperation.SelectCape -> {
            val account = operation.account
            val profile = operation.profile
            val capes = remember(profile.capes) { listOf(EmptyCape) + profile.capes }

            SelectCapeDialog(
                capes = capes,
                onSelected = { cape ->
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftCapeOp(
                            MicrosoftChangeCapeOperation.RunTask(account, cape)
                        )
                    )
                },
                onDismiss = {
                    actions.onIntent(
                        AccountManageIntent.UpdateMicrosoftCapeOp(
                            MicrosoftChangeCapeOperation.None
                        )
                    )
                }
            )
        }

        is MicrosoftChangeCapeOperation.RunTask -> {
            val capeName = operation.cape.capeTranslatedName()
            LaunchedEffect(operation) {
                val capeId: String? = operation.cape.takeIf { it != EmptyCape }?.id
                actions.onIntent(
                    AccountManageIntent.ApplyMicrosoftCape(
                        context,
                        operation.account,
                        capeId,
                        capeName,
                        operation.cape == EmptyCape
                    )
                )
            }
        }
    }
}

/**
 * 离线登录操作逻辑处理
 */
@Composable
private fun LocalLoginOperation(
    operation: LocalLoginOperation,
    actions: AccountActions
) {
    when (operation) {
        is LocalLoginOperation.None -> {}
        is LocalLoginOperation.Edit -> {
            LocalLoginDialog(
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.None
                        )
                    )
                },
                onConfirm = { isInvalid, name, uuid ->
                    val nextOp = if (isInvalid) LocalLoginOperation.Alert(
                        name,
                        uuid
                    ) else LocalLoginOperation.Create(name, uuid)
                    actions.onIntent(AccountManageIntent.UpdateLocalLoginOp(nextOp))
                },
                openLink = actions.openLink
            )
        }

        is LocalLoginOperation.Create -> {
            LaunchedEffect(operation) {
                actions.onIntent(
                    AccountManageIntent.CreateLocalAccount(
                        operation.userName,
                        operation.userUUID
                    )
                )
            }
        }

        is LocalLoginOperation.Alert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = {

                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint1))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.account_supporting_username_invalid_local_message_hint2),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint3))
                        Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint4))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.account_supporting_username_invalid_local_message_hint5),
                            fontWeight = FontWeight.Bold
                        )
                },
                confirmText = stringResource(R.string.account_supporting_username_invalid_still_use),
                onConfirm = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.Create(operation.userName, operation.userUUID)
                        )
                    )
                },
                onCancel = {
                    actions.onIntent(
                        AccountManageIntent.UpdateLocalLoginOp(
                            LocalLoginOperation.None
                        )
                    )
                }
            )
        }
    }
}

/**
 * 自定义验证服务器登录操作逻辑处理
 */
@Composable
private fun OtherLoginOperation(
    operation: OtherLoginOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is OtherLoginOperation.None -> {}
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = operation.server,
                onRegisterClick = { url ->
                    actions.openLink(url)
                    actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
                },
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateOtherLoginOp(
                            OtherLoginOperation.None
                        )
                    )
                },
                onConfirm = { email, password ->
                    actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
                    actions.onIntent(
                        AccountManageIntent.LoginWithOtherServer(
                            context,
                            operation.server,
                            email,
                            password
                        )
                    )
                }
            )
        }

        is OtherLoginOperation.OnFailed -> {
            val message = actions.formatError(context, operation.th)
            LaunchedEffect(operation) { // Bug 2 fix
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = context.getString(R.string.account_logging_in_failed),
                        message = message
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateOtherLoginOp(OtherLoginOperation.None))
            }
        }

        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                items = operation.profiles,
                itemTextProvider = { it.name },
                onItemSelected = { operation.selected(it) },
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateOtherLoginOp(
                            OtherLoginOperation.None
                        )
                    )
                }
            )
        }
    }
}

/**
 * 验证服务器管理（添加/删除）操作逻辑处理
 */
@Composable
private fun ServerTypeOperation(
    operation: ServerOperation,
    actions: AccountActions
) {
    val context = LocalContext.current // Context moved outside LaunchedEffect
    when (operation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                singleLine = true,
                onDismissRequest = {
                    actions.onIntent(
                        AccountManageIntent.UpdateServerOp(
                            ServerOperation.None
                        )
                    )
                },
                onConfirm = {
                    if (serverUrl.isNotEmpty()) actions.onIntent(
                        AccountManageIntent.UpdateServerOp(
                            ServerOperation.Add(serverUrl)
                        )
                    )
                }
            )
        }

        is ServerOperation.Add -> {
            LaunchedEffect(operation) {
                actions.onIntent(AccountManageIntent.AddServer(operation.serverUrl))
            }
        }

        is ServerOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(
                    R.string.account_other_login_delete_server_message,
                    operation.server.serverName
                ),
                onDismiss = { actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None)) },
                onConfirm = { actions.onIntent(AccountManageIntent.DeleteServer(operation.server)) }
            )
        }

        is ServerOperation.OnThrowable -> {
            val message = operation.throwable.getMessageOrToString()
            LaunchedEffect(operation) { // Bug 2 fix
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = context.getString(R.string.account_other_login_adding_failure),
                        message = message
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateServerOp(ServerOperation.None))
            }
        }

        is ServerOperation.None -> {}
    }
}

/**
 * 账号列表显示布局
 */
@Composable
private fun AccountsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    accounts: List<Account>,
    currentAccount: Account?,
    accountOperation: AccountOperation,
    accountSkinOperationMap: Map<String, AccountSkinOperation>,
    actions: AccountActions
) {
    val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
    val context = LocalContext.current

    // 账号通用管理操作（如删除、刷新失败提示）
    AccountOperation(accountOperation, actions)

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (accounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.extraLarge),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(accounts, key = { it.uniqueUUID }) { account ->
                    var refreshAvatar by remember { mutableStateOf(false) }
                    val skinOp =
                        accountSkinOperationMap[account.uniqueUUID] ?: AccountSkinOperation.None

                    // 针对单个账号的皮肤管理操作
                    AccountSkinOperation(
                        account = account,
                        accountSkinOperation = skinOp,
                        updateOperation = {
                            actions.onIntent(
                                AccountManageIntent.UpdateAccountSkinOp(
                                    account.uniqueUUID,
                                    it
                                )
                            )
                        },
                        onRefreshAvatar = { refreshAvatar = !refreshAvatar },
                        actions = actions
                    )

                    // 皮肤文件选择器
                    val skinPicker =
                        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                            uri?.let {
                                if (account.isLocalAccount()) actions.onIntent(
                                    AccountManageIntent.UpdateAccountSkinOp(
                                        account.uniqueUUID,
                                        AccountSkinOperation.SelectSkinModel(it)
                                    )
                                )
                                else if (account.isMicrosoftAccount()) actions.onIntent(
                                    AccountManageIntent.UpdateMicrosoftSkinOp(
                                        MicrosoftChangeSkinOperation.ImportFile(account, it)
                                    )
                                )
                            }
                        }

                    // 单个账号卡片
                    AccountItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        currentAccount = currentAccount,
                        account = account,
                        refreshKey = refreshAvatar,
                        onSelected = { AccountsManager.setCurrentAccount(it) },
                        onChangeSkin = {
                            if (!account.isAuthServerAccount()) skinPicker.launch(
                                arrayOf("image/png")
                            )
                        },
                        onChangeCape = {
                            if (account.isMicrosoftAccount()) actions.onIntent(
                                AccountManageIntent.UpdateMicrosoftCapeOp(
                                    MicrosoftChangeCapeOperation.FetchProfiles(account)
                                )
                            )
                        },
                        onResetSkin = {
                            actions.onIntent(
                                AccountManageIntent.UpdateAccountSkinOp(
                                    account.uniqueUUID,
                                    AccountSkinOperation.PreResetSkin
                                )
                            )
                        },
                        onRefreshClick = {
                            actions.onIntent(
                                AccountManageIntent.RefreshAccount(
                                    context,
                                    account
                                )
                            )
                        },
                        onCopyUUID = {
                            copyText(COPY_LABEL_ACCOUNT_UUID, account.profileId, context, false)
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.account_local_uuid_copied,
                                    account.username
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDeleteClick = {
                            actions.onIntent(
                                AccountManageIntent.UpdateAccountOp(
                                    AccountOperation.Delete(account)
                                )
                            )
                        }
                    )
                }
            }
        } else {
            // 空列表状态显示
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.account_no_account)
                )
            }
        }
    }
}

/**
 * 账号皮肤管理操作逻辑处理
 */
@Composable
private fun AccountSkinOperation(
    account: Account,
    accountSkinOperation: AccountSkinOperation,
    updateOperation: (AccountSkinOperation) -> Unit,
    onRefreshAvatar: () -> Unit,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (accountSkinOperation) {
        is AccountSkinOperation.None -> {}
        is AccountSkinOperation.SaveSkin -> {
            LaunchedEffect(accountSkinOperation.uri) {
                actions.onIntent(
                    AccountManageIntent.SaveLocalSkin(
                        context,
                        account,
                        accountSkinOperation.uri,
                        onRefreshAvatar
                    )
                )
            }
        }

        is AccountSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = { updateOperation(AccountSkinOperation.None) },
                onSelected = { type ->
                    account.skinModelType = type
                    account.profileId =
                        com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel(
                            account.username,
                            type
                        )
                    updateOperation(AccountSkinOperation.SaveSkin(accountSkinOperation.uri))
                }
            )
        }

        is AccountSkinOperation.PreResetSkin -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.account_change_skin_reset_skin_message),
                onDismiss = { updateOperation(AccountSkinOperation.None) },
                onConfirm = { updateOperation(AccountSkinOperation.ResetSkin) }
            )
        }

        is AccountSkinOperation.ResetSkin -> {
            LaunchedEffect(Unit) {
                actions.onIntent(AccountManageIntent.ResetSkin(account, onRefreshAvatar))
            }
        }
    }
}

/**
 * 通用账号管理操作逻辑处理
 */
@Composable
private fun AccountOperation(
    operation: AccountOperation,
    actions: AccountActions
) {
    val context = LocalContext.current
    when (operation) {
        is AccountOperation.Delete -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message, operation.account.username),
                onConfirm = { actions.onIntent(AccountManageIntent.DeleteAccount(operation.account)) },
                onDismiss = { actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None)) }
            )
        }

        is AccountOperation.OnFailed -> {
            val message = actions.formatError(context, operation.th)
            LaunchedEffect(operation) { // Bug 2 fix
                actions.submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = context.getString(R.string.account_logging_in_failed),
                        message = message
                    )
                )
                actions.onIntent(AccountManageIntent.UpdateAccountOp(AccountOperation.None))
            }
        }

        is AccountOperation.None -> {}
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun AccountManageContentPreview() {
    CompositionLocalProvider(LocalBackgroundViewModel provides null) {
        MaterialTheme {
            Surface {
                AccountManageContent(
                    isVisible = true,
                    uiState = AccountManageUiState(),
                    actions = AccountActions(
                        onIntent = {},
                        openLink = {},
                        backToMainScreen = {},
                        navigateToWeb = {},
                        checkIfInWebScreen = { false },
                        formatError = { _, _ -> "" },
                        submitError = {}
                    )
                )
            }
        }
    }
}