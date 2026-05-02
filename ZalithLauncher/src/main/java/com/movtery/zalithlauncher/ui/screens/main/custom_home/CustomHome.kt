/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 */

package com.movtery.zalithlauncher.ui.screens.main.custom_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.rememberMarkdownState
import com.movtery.zalithlauncher.ui.components.defaultMDTypography
import com.movtery.zalithlauncher.ui.components.markdownColorsOnBackground

@Composable
fun CustomHome(
    content: String,
    modifier: Modifier = Modifier,
    onLauncherEvent: (String) -> Unit = {}
) {
    val blocks = remember(content) {
        parseMarkdownBlocks(content)
    }

    MarkdownBlocksRenderer(
        modifier = modifier,
        blocks = blocks,
        markdownColors = markdownColorsOnBackground(), //仅父级使用
        onLauncherEvent = onLauncherEvent
    )
}

@Composable
fun CustomHome(
    blocks: List<MarkdownBlock>,
    modifier: Modifier = Modifier,
    onLauncherEvent: (String) -> Unit = {}
) {
    MarkdownBlocksRenderer(
        modifier = modifier,
        blocks = blocks,
        markdownColors = markdownColorsOnBackground(), //仅父级使用
        onLauncherEvent = onLauncherEvent
    )
}

@Composable
private fun MarkdownBlocksRenderer(
    blocks: List<MarkdownBlock>,
    modifier: Modifier = Modifier,
    markdownColors: MarkdownColors = markdownColor(),
    onLauncherEvent: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        blocks.forEach { block ->
            key(block.stableKey) {
                BlockItem(
                    block = block,
                    markdownColors = markdownColors,
                    onLauncherEvent = onLauncherEvent
                )
            }
        }
    }
}

@Composable
private fun BlockItem(
    block: MarkdownBlock,
    markdownColors: MarkdownColors = markdownColor(),
    onLauncherEvent: (String) -> Unit
) {
    when (block) {
        is MarkdownBlock.Normal -> {
            Markdown(
                markdownState = rememberMarkdownState(block.content),
                typography = defaultMDTypography(),
                colors = markdownColors,
                imageTransformer = Coil3ImageTransformerImpl
            )
        }
        is MarkdownBlock.Card -> {
            CustomHomeCard(
                title = block.title,
                shape = parseCardShape(block.params),
                contentPadding = parseCardPadding(block.params)
            ) {
                MarkdownBlocksRenderer(
                    blocks = block.content,
                    onLauncherEvent = onLauncherEvent
                )
            }
        }
        is MarkdownBlock.Button -> {
            CustomHomeButton(
                text = block.text,
                event = block.event,
                type = block.style,
                onLauncherEvent = onLauncherEvent
            )
        }
        is MarkdownBlock.RowBlock -> {
            Row(
                horizontalArrangement = block.horizontalArrangement,
                verticalAlignment = block.verticalAlignment,
                modifier = Modifier.fillMaxWidth()
            ) {
                block.buttons.forEach { button ->
                    CustomHomeButton(
                        text = button.text,
                        event = button.event,
                        type = button.style,
                        onLauncherEvent = onLauncherEvent
                    )
                }
            }
        }
    }
}


sealed interface MarkdownBlock {
    val stableKey: Any

    /**
     * 普通的Markdown内容
     */
    data class Normal(val content: String) : MarkdownBlock {
        override val stableKey: Any get() = content.hashCode()
    }

    /**
     * 一个预设好的Card组件
     * @param title 卡片的标题，必须存在，如果字符串内容不为空，则正常显示标题，如果为空，则不显示标题
     * @param params 卡片的参数字符串
     * @param content 卡片内部的组件
     */
    data class Card(
        val title: String,
        val params: String,
        val content: List<MarkdownBlock>
    ) : MarkdownBlock {
        override val stableKey: Any get() = "card_${title}_${params.hashCode()}"
    }

    /**
     * 一个Button组件
     * @param text 必须携带的文本内容组件，按钮的文本内容
     * @param event 可选的按钮执行事件组件，目前支持以下事件
     * ``` text
     * url=实际要访问链接内容
     * 一个网页链接访问事件，点击按钮后，在浏览器内打开该链接
     *
     * launcher=启动器对应事件tag
     * 启动器事件，启动器读取事件tag并触发对应事件
     * ```
     * @param style 该按钮的样式
     */
    data class Button(
        val text: String,
        val event: String?,
        val style: HomeButtonType
    ) : MarkdownBlock {
        override val stableKey: Any get() = "btn_${text}_${event}_${style}"
    }

    /**
     * Row组件，和Compose原生的Row一致，不过此处主要修饰Button组件
     */
    data class RowBlock(
        val horizontalArrangement: Arrangement.Horizontal,
        val verticalAlignment: Alignment.Vertical,
        val buttons: List<Button>
    ) : MarkdownBlock {
        override val stableKey: Any get() = "row_${buttons.hashCode()}"
    }
}



private val blockPattern = Regex(
    """(\.\.\.card-start([^\n]*))|(\.\.\.button(-outlined|-filled-tonal|-text)?\s+text="([^"]*)"(?:\s+(?:event|onClick)="([^"]*)")?)|(\.\.\.row-start([^\n]*))""",
    RegexOption.DOT_MATCHES_ALL
)

/**
 * 由于扩展组件相对独立，需要将其拆分出来，作为单独的部分进行渲染，
 * 这里会把内容进行拆分成多个块
 */
fun parseMarkdownBlocks(
    content: String,
): List<MarkdownBlock> {
    val cleaned = content.lineSequence()
        //清洗注释行
        .filterNot { it.trimStart().startsWith("//") }
        .joinToString("\n")
    return parseMarkdownBlocksInternal(cleaned)
}


private fun parseMarkdownBlocksInternal(
    cleared: String,
    allowCard: Boolean = true,
    allowRow: Boolean = true
): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()

    var lastIndex = 0
    while (lastIndex < cleared.length) {
        val match = blockPattern.find(cleared, lastIndex)
        if (match == null) {
            //没有更多匹配，添加剩余内容
            val remaining = cleared.substring(lastIndex).trim()
            if (remaining.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(content = remaining))
            }
            break
        }

        //处理匹配项之前的普通文本
        if (match.range.first > lastIndex) {
            val text = cleared.substring(lastIndex, match.range.first).trim()
            if (text.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(content = text))
            }
        }

        val isCardStart = match.groupValues[1].isNotEmpty()
        val isButton = match.groupValues[3].isNotEmpty()
        val isRowStart = match.groupValues[7].isNotEmpty()

        when {
            isCardStart && allowCard -> {
                val params = match.groupValues[2]
                val title = Regex("""title\s*=\s*"([^"]*)"""").find(params)?.groupValues?.get(1) ?: ""
                //寻找对应的卡片闭合标签
                val closingRange = findNestedClosingTag(
                    content = cleared,
                    startIndex = match.range.last + 1, //这里是为了防止嵌套行为，动态添加闭合范围
                    openTagPattern = """\.\.\.card-start""",
                    closeTag = "...card-end"
                )

                if (closingRange != null) {
                    val innerContent = cleared.substring(match.range.last + 1, closingRange.first).trim('\n')
                    blocks.add(
                        MarkdownBlock.Card(
                            title = title,
                            params = params,
                            content = parseMarkdownBlocksInternal(
                                cleared = innerContent,
                                allowCard = false, //不允许内部嵌套卡片组件
                                allowRow = true
                            )
                        )
                    )
                    lastIndex = closingRange.last + 1
                } else {
                    //如果没找到匹配的结束标记，则将此开始标记视为Markdown
                    blocks.add(MarkdownBlock.Normal(match.value))
                    lastIndex = match.range.last + 1
                }
            }

            isRowStart && allowRow -> {
                val params = match.groupValues[8]
                val closingIndex = cleared.indexOf(
                    string = "...row-end",
                    startIndex = match.range.last + 1
                )
                if (closingIndex != -1) {
                    val innerContent = cleared.substring(
                        startIndex = match.range.last + 1,
                        endIndex = closingIndex
                    ).trim('\n')

                    val buttons = parseMarkdownBlocksInternal(
                        cleared = innerContent,
                        allowCard = false,
                        allowRow = false
                    ).filterIsInstance<MarkdownBlock.Button>()

                    blocks.add(
                        MarkdownBlock.RowBlock(
                            horizontalArrangement = parseHorizontalArrangement(params),
                            verticalAlignment = parseVerticalAlignment(params),
                            buttons = buttons
                        )
                    )
                    lastIndex = closingIndex + "...row-end".length
                } else {
                    blocks.add(MarkdownBlock.Normal(match.value))
                    lastIndex = match.range.last + 1
                }
            }

            isButton -> {
                blocks.add(
                    parseButton(
                        styleSuffix = match.groupValues[4],
                        text = match.groupValues[5],
                        event = match.groupValues[6]
                    )
                )
                lastIndex = match.range.last + 1
            }

            else -> {
                //如果是标记但当前上下文不允许（比如卡片嵌套），则视为普通Markdown
                blocks.add(MarkdownBlock.Normal(match.value))
                lastIndex = match.range.last + 1
            }
        }
    }

    return blocks
}

/**
 * 寻找嵌套结构的闭合标记
 * @param content 完整内容
 * @param startIndex 开始寻找的位置
 * @param openTagPattern 开始标记的正则模式（如 \.\.\.card-start）
 * @param closeTag 结束标记的字符串（如 ...card-end）
 * @return 结束标记的范围，如果未找到则返回 null
 */
private fun findNestedClosingTag(
    content: String,
    startIndex: Int,
    openTagPattern: String,
    closeTag: String
): IntRange? {
    var depth = 1
    var current = startIndex
    val pattern = Regex("($openTagPattern)|(${Regex.escape(closeTag)})")

    while (current < content.length) {
        val match = pattern.find(content, current) ?: return null
        if (match.groupValues[1].isNotEmpty()) {
            depth++
        } else {
            depth--
        }
        if (depth == 0) return match.range
        current = match.range.last + 1
    }
    return null
}

private fun parseButton(
    styleSuffix: String,
    text: String,
    event: String?
): MarkdownBlock.Button {
    val style = when (styleSuffix) {
        "-outlined" -> HomeButtonType.Outlined
        "-filled-tonal" -> HomeButtonType.FilledTonal
        "-text" -> HomeButtonType.Text
        else -> HomeButtonType.Filled
    }
    return MarkdownBlock.Button(
        text = text,
        event = event.takeIf { it?.isNotEmpty() == true },
        style = style
    )
}

private fun parseHorizontalArrangement(
    params: String,
//    alignment: String,
): Arrangement.Horizontal {
    val spacedByRegex = Regex("""Arrangement\.spacedBy\((\d+)(?:,\s*Alignment\.(Start|End|CenterHorizontally))?\)""")
    spacedByRegex.find(params)?.let { match ->
        val space = match.groupValues[1].toIntOrNull() ?: 0
        val alignment = when (match.groupValues[2]) {
            "Start" -> Alignment.Start
            "End" -> Alignment.End
            "CenterHorizontally" -> Alignment.CenterHorizontally
            else -> null
        }
        return if (alignment != null) Arrangement.spacedBy(space.dp, alignment) else Arrangement.spacedBy(space.dp)
    }

    return when {
        params.contains("Arrangement.Start") -> Arrangement.Start
        params.contains("Arrangement.End") -> Arrangement.End
        params.contains("Arrangement.Center") -> Arrangement.Center
        params.contains("Arrangement.SpaceEvenly") -> Arrangement.SpaceEvenly
        params.contains("Arrangement.SpaceBetween") -> Arrangement.SpaceBetween
        params.contains("Arrangement.SpaceAround") -> Arrangement.SpaceAround
        else -> Arrangement.Start
    }
}

private fun parseVerticalAlignment(params: String): Alignment.Vertical {
    return when {
        params.contains("Alignment.Top") -> Alignment.Top
        params.contains("Alignment.CenterVertically") -> Alignment.CenterVertically
        params.contains("Alignment.Bottom") -> Alignment.Bottom
        else -> Alignment.Top
    }
}

@Composable
private fun parseCardShape(params: String): Shape? {
    return when {
        params.contains("shape=extraSmall") -> MaterialTheme.shapes.extraSmall
        params.contains("shape=small") -> MaterialTheme.shapes.small
        params.contains("shape=medium") -> MaterialTheme.shapes.medium
        params.contains("shape=large") -> MaterialTheme.shapes.large
        params.contains("shape=extraLarge") -> MaterialTheme.shapes.extraLarge
        params.contains(Regex("""shape=\d+dp""")) -> {
            val size = Regex("""shape=(\d+)dp""").find(params)?.groupValues?.get(1)?.toIntOrNull() ?: return null
            RoundedCornerShape(size.dp)
        }
        params.contains(Regex("""shape=\d+(?!\w)""")) -> {
            val percent = Regex("""shape=(\d+)(?!\w)""").find(params)?.groupValues?.get(1)?.toIntOrNull() ?: return null
            RoundedCornerShape(percent)
        }
        else -> null
    }
}

private fun parseCardPadding(params: String): PaddingValues? {
    val regex = Regex("""contentPadding\s*=\s*\(([\d\s,]+)\)""")
    val match = regex.find(params) ?: return null
    val values = match.groupValues[1].split(",").map { it.trim().toIntOrNull() ?: 0 }
    return when (values.size) {
        1 -> PaddingValues(values[0].dp)
        2 -> PaddingValues(horizontal = values[0].dp, vertical = values[1].dp)
        4 -> PaddingValues(
            start = values[0].dp,
            top = values[1].dp,
            end = values[2].dp,
            bottom = values[3].dp
        )
        else -> null
    }
}
