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

package com.star1xr.treelauncher.game.account.auth_server

import android.content.Context
import com.google.gson.Gson
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.game.account.Account
import com.star1xr.treelauncher.game.account.auth_server.models.AuthRequest
import com.star1xr.treelauncher.game.account.auth_server.models.AuthResult
import com.star1xr.treelauncher.game.account.auth_server.models.Refresh
import com.star1xr.treelauncher.info.InfoDistributor
import com.star1xr.treelauncher.path.GLOBAL_CLIENT
import com.star1xr.treelauncher.utils.logging.Logger.lDebug
import com.star1xr.treelauncher.utils.logging.Logger.lError
import com.star1xr.treelauncher.utils.network.safeBodyAsJson
import com.star1xr.treelauncher.utils.network.safeBodyAsText
import com.star1xr.treelauncher.utils.string.decodeUnicode
import com.star1xr.treelauncher.utils.string.toUuidStr
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.util.Objects

class AuthServerApi(private var baseUrl: String) {
    fun formatUrl(baseUrl: String): String {
        var url = baseUrl
        if (baseUrl.endsWith("/")) {
            url = baseUrl.dropLast(1)
        }
        return url
    }

    init {
        baseUrl = formatUrl(baseUrl)
    }

    @Throws(IOException::class)
    suspend fun login(
        context: Context,
        userName: String,
        password: String,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (th: Throwable) -> Unit = {}
    ) {
        if (Objects.isNull(baseUrl)) {
            onFailed(ResponseException(context.getString(R.string.account_other_login_baseurl_not_set)))
            return
        }

        val agent = AuthRequest.Agent(
            name = "Minecraft",
            version = 1
        )

        val authRequest = AuthRequest(
            username = userName,
            password = password,
            agent = agent,
            requestUser = true,
            clientToken = InfoDistributor.LAUNCHER_NAME.toUuidStr().replace("-", "")
        )

        val data = Gson().toJson(authRequest)
        callLogin(data, "/authserver/authenticate", onSuccess, onFailed)
    }

    @Throws(IOException::class)
    suspend fun refresh(
        context: Context,
        account: Account,
        select: Boolean,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (th: Throwable) -> Unit = {}
    ) {
        if (Objects.isNull(baseUrl)) {
            onFailed(ResponseException(context.getString(R.string.account_other_login_baseurl_not_set)))
            return
        }

        val refresh = Refresh(
            clientToken = account.clientToken,
            accessToken = account.accessToken
        )

        if (select) {
            refresh.selectedProfile = Refresh.SelectedProfile(
                name = account.username,
                id = account.profileId
            )
        }

        val json = Gson().toJson(refresh)
        callLogin(json, "/authserver/refresh", onSuccess, onFailed)
    }

    private suspend fun callLogin(
        data: String,
        url: String,
        onSuccess: suspend (AuthResult) -> Unit = {},
        onFailed: suspend (th: Throwable) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = GLOBAL_CLIENT.post(baseUrl + url) {
                contentType(ContentType.Application.Json)
                setBody(data)
            }

            if (response.status == HttpStatusCode.OK) {
                val result: AuthResult = response.safeBodyAsJson()
                onSuccess(result)
            } else {
                val errorMessage = response.getErrorMessage()
                lError(errorMessage)
                onFailed(ResponseException(errorMessage))
            }
        } catch (e: ClientRequestException) {
            val errorMessage = e.response.getErrorMessage()
            lError(errorMessage, e)
            onFailed(ResponseException(errorMessage))
        } catch (e: CancellationException) {
            lDebug("Login cancelled")
            throw e
        } catch (e: Exception) {
            lError("Request failed", e)
            onFailed(e)
        }
    }

    private suspend fun HttpResponse.getErrorMessage(): String {
        return "(${status.value}) ${parseError(this)}"
    }

    private suspend fun parseError(response: HttpResponse): String {
        return try {
            val json = response.safeBodyAsJson<JsonObject>()
            var message = when {
                "errorMessage" in json -> json["errorMessage"]?.jsonPrimitive?.content ?: "Unknown error"
                "message" in json -> json["message"]?.jsonPrimitive?.content ?: "Unknown error"
                else -> "Unknown error"
            }
            if (message.contains("\\u")) {
                message = decodeUnicode(message.replace("\\\\u", "\\u"))
            }
            message
        } catch (e: Exception) {
            lError("Failed to parse error", e)
            "Unknown error"
        }
    }
}

suspend fun getAuthServeInfo(url: String): String? = withContext(Dispatchers.IO) {
    val response = GLOBAL_CLIENT.get(url)
    if (response.status == HttpStatusCode.OK) {
        response.safeBodyAsText()
    } else {
        null
    }
}