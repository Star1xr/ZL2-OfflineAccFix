/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 */

package com.movtery.zalithlauncher.ui.screens.main.custom_home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CardTitleLayout
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 一个用于包装 Markdown 内容的卡片组件
 */
@Composable
fun CustomHomeCard(
    title: String,
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    shape: Shape? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit
) {
    BackgroundCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = shape ?: MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = cardColor(influencedByBackground),
            contentColor = onCardColor()
        ),
    ) {
        if (title.isNotEmpty()) {
            CardTitleLayout {
                Text(
                    modifier = Modifier.padding(all = 16.dp),
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding ?: PaddingValues(8.dp))
        ) {
            content()
        }
    }
}
