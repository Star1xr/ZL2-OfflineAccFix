package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * 启动器主页类型
 */
enum class HomePageType(
    @field:StringRes
    val textRes: Int
) {
    /**
     * 空白主页
     */
    Blank(R.string.settings_launcher_home_page_type_blank),

    /**
     * 从本地加载
     */
    FromLocal(R.string.settings_launcher_home_page_type_local),

    /**
     * 从网络加载
     */
    FromURL(R.string.settings_launcher_home_page_type_url)
}