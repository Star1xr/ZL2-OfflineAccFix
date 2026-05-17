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

package com.star1xr.treelauncher.ui.screens.game.multiplayer

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.terracotta.Terracotta
import com.star1xr.treelauncher.ui.components.SimpleEditDialog
import com.star1xr.treelauncher.utils.string.isEmptyOrBlank
import net.burningtnt.terracotta.TerracottaAndroidAPI

/** 等待中：房客操作状态 */
sealed interface GuestWaitingOperation {
    data object None : GuestWaitingOperation
    /** 被点击，开始输入邀请码 */
    data object OnClick : GuestWaitingOperation
}

@Composable
fun GuestWaitingOperation(
    operation: GuestWaitingOperation,
    onChange: (GuestWaitingOperation) -> Unit,
    onPositive: (roomCode: String) -> Unit
) {
    when (operation) {
        is GuestWaitingOperation.None -> {}
        is GuestWaitingOperation.OnClick -> {
            InviteCodeInputDialog(
                onPositive = onPositive,
                onDismiss = {
                    onChange(GuestWaitingOperation.None)
                }
            )
        }
    }
}

/**
 * 房客输入邀请码对话框
 */
@Composable
private fun InviteCodeInputDialog(
    onPositive: (roomCode: String) -> Unit,
    onDismiss: () -> Unit
) {
    val terracottaLegacyText = stringResource(R.string.terracotta_status_waiting_guest_prompt_terracotta_legacy)
    val pcl2ceLegacyText = stringResource(R.string.terracotta_status_waiting_guest_prompt_pcl2ce)
    val scaffoldingText = stringResource(R.string.terracotta_status_waiting_guest_prompt_scaffolding)
    val codeInvalid = stringResource(R.string.terracotta_status_waiting_guest_prompt_invalid)

    var code by remember { mutableStateOf("") }

    /** 验证不通过时 */
    var isError by remember { mutableStateOf(false) }
    val supportingText: String? = remember(code) {
        if (code.isEmpty()) {
            //还未填写内容
            isError = false
            return@remember null
        }

        val type = Terracotta.parseRoomCode(code)
        when (type) {
            TerracottaAndroidAPI.RoomType.TERRACOTTA_LEGACY -> terracottaLegacyText
            TerracottaAndroidAPI.RoomType.PCL2CE -> pcl2ceLegacyText
            TerracottaAndroidAPI.RoomType.SCAFFOLDING -> scaffoldingText
            else -> null
        }.also { text ->
            //根据是否检测出对应格式判断
            isError = text == null
        } ?: codeInvalid
    }

    val context = LocalContext.current
    SimpleEditDialog(
        title = stringResource(R.string.terracotta_status_waiting_guest_prompt_title),
        value = code,
        onValueChange = { value ->
            code = value
        },
        label = (@Composable { Text(text = "U/XXXX-XXXX-XXXX-XXXX") }).takeIf { code.isEmpty() },
        supportingText = supportingText?.let { string ->
            {
                Text(text = string)
            }
        },
        singleLine = true,
        isError = isError,
        onConfirm = {
            if (isError || code.isEmptyOrBlank() || Terracotta.parseRoomCode(code) == null) {
                Toast.makeText(context, codeInvalid, Toast.LENGTH_SHORT).show()
            } else {
                onPositive(code)
                onDismiss()
            }
        },
        onDismissRequest = onDismiss
    )
}