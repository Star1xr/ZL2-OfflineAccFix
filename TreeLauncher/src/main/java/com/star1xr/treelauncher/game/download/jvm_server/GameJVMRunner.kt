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

package com.star1xr.treelauncher.game.download.jvm_server

import com.star1xr.treelauncher.components.jre.Jre
import com.star1xr.treelauncher.context.GlobalContext
import com.star1xr.treelauncher.notification.NoticeProgress
import com.star1xr.treelauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 运行一个简易的JVM环境，安装ModLoader，同时在jvm退出时，尝试使用其他的Java环境重试
 * @param logId 记录日志的 tag
 * @param start 刚开始启动会调用的回调
 */
suspend fun runJvmRetryRuntimes(
    logId: String,
    jvmArgs: String,
    prefixArgs: (Jre) -> String?,
    jre: Jre,
    userHome: String,
    postSummary: String? = null,
    postProgress: NoticeProgress? = null,
    start: () -> Unit = {}
): Unit = withContext(Dispatchers.Default) {
    while (!isOnlyMainProcessesRunning(context = GlobalContext)) {
        lInfo("$logId Waiting for other processes stop...")
        delay(100)
    }

    start()

    val finalArgs = prefixArgs(jre)?.let {
        "$it $jvmArgs"
    } ?: jvmArgs

    val exitCode = startJvmServiceAndWaitExit(
        jvmArgs = finalArgs,
        jreName = jre.jreName,
        userHome = userHome,
        postSummary = postSummary,
        postProgress = postProgress
    )

    if (exitCode != 0) {
        val nextJava: Jre? = when (jre) {
            Jre.JRE_8 -> Jre.JRE_17
            Jre.JRE_17 -> Jre.JRE_21
            else -> null
        }

        nextJava?.let { jre ->
            lInfo("Retry with jre ${jre.name}...")
            runJvmRetryRuntimes(
                logId = logId,
                jvmArgs = jvmArgs,
                prefixArgs = prefixArgs,
                jre = jre,
                userHome = userHome,
                postSummary = postSummary,
                postProgress = postProgress
            )
        } ?: throw JvmCrashException(exitCode)
    }
}

suspend fun startJvmServiceAndWaitExit(
    jvmArgs: String,
    jreName: String? = null,
    userHome: String? = null,
    postSummary: String? = null,
    postProgress: NoticeProgress? = null,
): Int = withContext(Dispatchers.IO) {
    val doneSignal = CompletableDeferred<Unit>()

    startJvmService(
        context = GlobalContext,
        jvmArgs = jvmArgs,
        userHome = userHome,
        jreName = jreName,
        postSummary = postSummary,
        postProgress = postProgress
    )

    JVMSocketServer.start { receiveMsg ->
        lInfo("receive msg: $receiveMsg, stopping server...")
        if (!doneSignal.isCompleted) {
            doneSignal.complete(Unit)
        }
        JVMSocketServer.stop()
    }
    doneSignal.await()

    val code = JVMSocketServer.receiveMsg?.toIntOrNull()
    lInfo("receive exit code: ${code ?: "unknown, default 0"}")
    code ?: 0
}