/*
 * Zalith Launcher 2
 * Copyright (C) 2025 Star1xr and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.star1xr.treelauncher.upgrade

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseApi(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String,
    @SerialName("body")
    val body: String,
    @SerialName("published_at")
    val publishedAt: String,
    @SerialName("assets")
    val assets: List<Asset>
) {
    @Serializable
    data class Asset(
        @SerialName("name")
        val name: String,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        @SerialName("size")
        val size: Long
    )
}
