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

package com.star1xr.treelauncher.utils.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.context.COPY_LABEL_LINK
import com.star1xr.treelauncher.path.TIME_OUT
import com.star1xr.treelauncher.path.URL_USER_AGENT
import com.star1xr.treelauncher.path.createOkHttpClient
import com.star1xr.treelauncher.path.createRequestBuilder
import com.star1xr.treelauncher.utils.copyText
import com.star1xr.treelauncher.utils.file.compareSHA1
import com.star1xr.treelauncher.utils.file.ensureParentDirectory
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lWarning
import com.star1xr.treelauncher.utils.string.isEmptyOrBlank
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.apache.commons.io.FileUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

/**
 * @return 当前网络是否可用
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

/**
 * @return 当前是否正在使用移动网络
 */
fun isUsingMobileData(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}

/**
 * 同步下载文件到本地
 * @param url 要下载的文件URL
 * @param outputFile 要保存的目标文件
 * @param bufferSize 缓冲区大小
 * @param sha1 文件SHA1验证值
 * @param sizeCallback 正在下载的大小回调
 */
fun downloadFileWithHttp(
    url: String,
    outputFile: File,
    bufferSize: Int = 65536,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
) {
    val maxAttempts = if (sha1 != null) 2 else 1
    var attempt = 0
    var totalReportedBytes = 0L

    while (true) {
        attempt++
        //本次尝试中已回调的大小
        var attemptReportedBytes = 0L

        try {
            outputFile.ensureParentDirectory()

            val conn = URL(url).openConnection() as HttpURLConnection

            conn.apply {
                readTimeout = TIME_OUT.toInt()
                connectTimeout = TIME_OUT.toInt()
                useCaches = true
                setRequestProperty("User-Agent", "Mozilla/5.0/$URL_USER_AGENT")
            }

            conn.connect()
            if (conn.responseCode !in 200..299) {
                if (conn.responseCode == 404) throw FileNotFoundException("HTTP ${conn.responseCode} - ${conn.responseMessage}")
                throw IOException("HTTP ${conn.responseCode} - ${conn.responseMessage}")
            }

            val contentLength = conn.contentLengthLong
            val buffer = ByteArray(bufferSize)

            conn.inputStream.use { inputStream ->
                BufferedOutputStream(FileOutputStream(outputFile)).use { fos ->
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        sizeCallback(bytesRead.toLong())
                        attemptReportedBytes += bytesRead
                        totalReportedBytes += bytesRead
                    }

                    if (contentLength != -1L && totalBytesRead != contentLength) {
                        throw IOException("Download incomplete. Expected $contentLength bytes, received $totalBytesRead bytes.")
                    }
                }
            }

            sha1?.let {
                if (!compareSHA1(outputFile, it)) {
                    throw IOException("SHA1 verification failed for $url")
                }
            }

            return //下载并验证成功
        } catch (e: Exception) {
            FileUtils.deleteQuietly(outputFile)

            if (attemptReportedBytes > 0) {
                //回退本次尝试的下载量
                sizeCallback(-attemptReportedBytes)
                totalReportedBytes -= attemptReportedBytes
            }

            if (e.isInterruptedIOException()) {
                lDebug("Download task cancelled. url: $url")
                return //取消了，不需要抛出异常
            } else if (e is FileNotFoundException) {
                if (attempt >= maxAttempts) throw e //目标不存在
            } else {
                if (attempt >= maxAttempts) {
                    throw IOException("Download failed after $maxAttempts attempts: $url", e)
                }
            }
        }
    }
}

/**
 * 同步下载文件到本地
 * @param url 要下载的文件URL
 * @param outputFile 要保存的目标文件
 * @param bufferSize 缓冲区大小
 * @param sha1 文件SHA1验证值
 * @param sizeCallback 正在下载的大小回调
 */
suspend fun downloadFileSuspend(
    url: String,
    outputFile: File,
    bufferSize: Int = 65536,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
) = withContext(Dispatchers.IO) {
    runInterruptible {
        downloadFileWithHttp(
            url = url,
            outputFile = outputFile,
            bufferSize = bufferSize,
            sha1 = sha1,
            sizeCallback = sizeCallback
        )
    }
}

/**
 * 从多个下载地址中尝试下载
 * @param urls 要下载的文件链接列表
 * @param outputFile 要保存的目标文件
 * @param bufferSize 缓冲区大小
 * @param sha1 文件SHA1验证值
 * @param sizeCallback 正在下载的大小回调
 */
fun downloadFromMirrorList(
    urls: List<String>,
    outputFile: File,
    bufferSize: Int = 65536,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
) {
    require(urls.isNotEmpty()) { "URL list must not be empty." }

    val errors = mutableListOf<Exception>()
    var lastException: Exception? = null
    var totalReportedBytes = 0L

    for (url in urls) {
        var attempt = 0
        val maxAttempts = if (sha1 != null) 2 else 1

        while (attempt < maxAttempts) {
            attempt++
            //本次镜像尝试中已回调的大小
            var mirrorAttemptReported = 0L

            try {
                val mirrorCallback = { bytes: Long ->
                    if (bytes > 0) {
                        mirrorAttemptReported += bytes
                        totalReportedBytes += bytes
                    }
                    sizeCallback(bytes)
                }

                downloadFileWithHttp(
                    url = url,
                    outputFile = outputFile,
                    bufferSize = bufferSize,
                    sha1 = sha1,
                    sizeCallback = mirrorCallback
                )
                return //下载成功
            } catch (e: Exception) {
                FileUtils.deleteQuietly(outputFile)
                lastException = e

                if (mirrorAttemptReported > 0) {
                    //回退本次镜像尝试的下载量
                    sizeCallback(-mirrorAttemptReported)
                    totalReportedBytes -= mirrorAttemptReported
                }

                if (e.isInterruptedIOException()) {
                    throw e
                } else if (e is FileNotFoundException) {
                    errors.add(e)
                    break
                } else {
                    errors.add(e)
                }
            }
        }
    }

    throw IOException("Failed to download file from all mirrors (${errors.size} errors)", lastException).apply {
        errors.forEachIndexed { i, e ->
            addSuppressed(Exception("Mirror error #${i + 1}: ${e.message}"))
        }
    }
}

/**
 * 从多个下载地址中尝试下载
 * @param urls 要下载的文件链接列表
 * @param outputFile 要保存的目标文件
 * @param bufferSize 缓冲区大小
 * @param sha1 文件SHA1验证值
 * @param sizeCallback 正在下载的大小回调
 */
suspend fun downloadFromMirrorListSuspend(
    urls: List<String>,
    outputFile: File,
    bufferSize: Int = 65536,
    sha1: String? = null,
    sizeCallback: (Long) -> Unit = {}
) = withContext(Dispatchers.IO) {
    runInterruptible {
        downloadFromMirrorList(
            urls = urls,
            outputFile = outputFile,
            bufferSize = bufferSize,
            sha1 = sha1,
            sizeCallback = sizeCallback
        )
    }
}

/**
 * 速率监测报告
 * @param onSpeedReport 在1秒延迟后汇报期间的数据量，单位：bytes
 */
suspend fun <T> withSpeedReport(
    onSpeedReport: (Long) -> Unit,
    onClear: () -> Unit = {},
    block: suspend (onBytesWritten: (Long) -> Unit) -> T
): T = coroutineScope {
    val bytesWritten = AtomicLong(0L)

    withSpeedReport(
        onTimeReport = {
            val currentBytes = bytesWritten.getAndSet(0L)
            onSpeedReport(currentBytes)
        },
        onClear = {
            bytesWritten.set(0L)
            onClear()
        },
        block = {
            block { bytes ->
                bytesWritten.addAndGet(bytes)
            }
        }
    )
}

/**
 * 速率监测报告
 * @param onTimeReport 在1秒延迟后调用，可在此期间汇报
 */
suspend fun <T> withSpeedReport(
    onTimeReport: () -> Unit,
    onClear: () -> Unit,
    block: suspend () -> T
): T = coroutineScope {
    var reportJob: Job? = null

    try {
        onClear()
        reportJob = launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                onTimeReport()
            }
        }

        block()
    } finally {
        reportJob?.cancelAndJoin()
        onClear()
    }
}

/**
 * 同步获取 URL 返回的字符串内容
 * @param url 要请求的URL地址
 * @return 服务器返回的字符串内容
 * @throws IllegalArgumentException 当URL无效时
 * @throws IOException 当网络请求失败或响应解析失败时
 */
@Throws(IOException::class, IllegalArgumentException::class)
suspend fun fetchStringFromUrl(url: String): String = withContext(Dispatchers.IO) {
    runInterruptible {
        call(url) { call ->
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} - ${response.message}")
                }

                return@call response.body.use { it.string() }
            }
        }
    }
}

/**
 * 同步获取 URL 返回的字符串内容
 * @param urls 要请求的URL源地址
 * @return 服务器返回的字符串内容
 * @throws IllegalArgumentException 当URL无效时
 * @throws IOException 当网络请求失败或响应解析失败时
 */
@Throws(IOException::class, IllegalArgumentException::class)
suspend fun fetchStringFromUrls(urls: List<String>): String = withContext(Dispatchers.IO) {
    var result: String? = null
    var succeed = false
    var lastException: Throwable? = null

    loop@ for (url in urls) {
        runCatching {
            result = fetchStringFromUrl(url)
            succeed = true
            break@loop
        }.onFailure { th ->
            if (th is CancellationException || th.isInterruptedIOException()) throw th
            lDebug("Source $url failed!", th)
            lastException = th
        }
    }

    if (!succeed || result == null) throw lastException ?: IOException("Failed to retrieve information from the source!")

    result
}

private fun <T> call(url: String, call: (Call) -> T): T {
    val client = createOkHttpClient()
    val request = createRequestBuilder(url).build()

    return call(client.newCall(request))
}

/**
 * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
 * @param link 要访问的链接
 */
fun Activity.openLink(link: String) {
    this.openLink(link, null)
}

/**
 * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
 * @param link 要访问的链接
 * @param dataType 设置 intent 的数据以及显式 MIME 数据类型
 */
fun Activity.openLink(link: String, dataType: String?) {
    if (link.isEmptyOrBlank()) {
        return
    }

    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.generic_open_link)
        .setMessage(link)
        .setPositiveButton(R.string.generic_confirm) { _, _ ->
            openLinkInternal(link, dataType)
        }
        .setNegativeButton(R.string.generic_cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .setNeutralButton(R.string.generic_copy) { dialog, _ ->
            copyText(COPY_LABEL_LINK, link, this)
            dialog.dismiss()
        }
        .show()
}

/**
 * 直接在浏览器打开指定链接
 */
fun Activity.openLinkInternal(link: String, dataType: String? = null) {
    try {
        val uri = link.toUri()
        val browserIntent = if (dataType != null) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, dataType)
            }
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        startActivity(browserIntent)
    } catch (e: Exception) {
        lWarning("Failed to open link: $link", e)
    }
}

/**
 * 检查是不是单纯的中断异常，而不是网络超时导致的中断
 */
fun Throwable.isInterruptedIOException(): Boolean {
    return this is InterruptedIOException && this !is SocketTimeoutException
}