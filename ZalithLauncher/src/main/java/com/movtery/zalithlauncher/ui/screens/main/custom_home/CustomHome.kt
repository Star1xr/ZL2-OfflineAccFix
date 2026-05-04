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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextStyle
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.defaultRichTextStyle
import com.movtery.zalithlauncher.ui.theme.itemColor

fun LazyListScope.customHomePage(
    blocks: List<MarkdownBlock>,
    richTextStyle: RichTextStyle,
    onEvent: (MarkdownBlock.Button.Event) -> Unit = {}
) {
    items(
        items = blocks,
        key = { it.stableKey },
        contentType = { it::class }
    ) { block ->
        BlockItem(
            block = block,
            richTextStyle = richTextStyle,
            onEvent = onEvent
        )
    }
}

@Composable
private fun MarkdownInnerRenderer(
    blocks: List<MarkdownBlock>,
    modifier: Modifier = Modifier,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
    onEvent: (MarkdownBlock.Button.Event) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        blocks.forEach { block ->
            key(block.stableKey) {
                BlockItem(
                    block = block,
                    richTextStyle = richTextStyle,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun BlockItem(
    block: MarkdownBlock,
    modifier: Modifier = Modifier,
    inRow: Boolean = false,
    richTextStyle: RichTextStyle = defaultRichTextStyle(),
    onEvent: (MarkdownBlock.Button.Event) -> Unit
) {
    when (block) {
        is MarkdownBlock.Normal -> {
            MarkdownView(
                node = block.astNode,
                modifier = modifier,
                richTextStyle = richTextStyle
            )
        }
        is MarkdownBlock.Card -> {
            CustomHomeCard(
                modifier = modifier,
                title = block.title,
                shape = parseShape(block.params),
                contentPadding = parseCardPadding(block.params)
            ) {
                MarkdownInnerRenderer(
                    blocks = block.content,
                    richTextStyle = defaultRichTextStyle(
                        influencedByBackground = false,
                        codeBackground = itemColor(false)
                    ),
                    onEvent = onEvent
                )
            }
        }
        is MarkdownBlock.Button -> {
            CustomHomeButton(
                modifier = modifier,
                text = block.text,
                event = block.event,
                type = block.style,
                onEvent = onEvent
            )
        }
        is MarkdownBlock.Image -> {
            val useWeight = inRow && parseWeight(block.params) != null
            CustomHomeImage(
                modifier = modifier,
                url = block.url,
                width = if (useWeight) null else block.width,
                shape = parseShape(block.params)
            )
        }
        is MarkdownBlock.RowBlock -> {
            Row(
                horizontalArrangement = block.horizontal,
                verticalAlignment = block.vertical,
                modifier = modifier.fillMaxWidth()
            ) {
                block.children.forEach { child ->
                    val weightInfo = parseWeight(child.params)
                    val childModifier = if (weightInfo != null) {
                        Modifier.weight(weightInfo.first, weightInfo.second)
                    } else {
                        Modifier
                    }
                    BlockItem(
                        block = child,
                        modifier = childModifier,
                        inRow = true,
                        richTextStyle = richTextStyle,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}


sealed interface MarkdownBlock {
    val stableKey: Any
    val params: String

    /**
     * 普通的Markdown内容
     */
    data class Normal(val astNode: AstNode) : MarkdownBlock {
        override val stableKey: Any get() = astNode.hashCode()
        override val params: String get() = ""
    }

    /**
     * 一个预设好的Card组件
     * @param title 卡片的标题，必须存在，如果字符串内容不为空，则正常显示标题，如果为空，则不显示标题
     * @param params 卡片的参数字符串
     * @param content 卡片内部的组件
     */
    data class Card(
        val title: String,
        override val params: String,
        val content: List<MarkdownBlock>
    ) : MarkdownBlock {
        override val stableKey: Any get() = "card_${title}_${params.hashCode()}"
    }

    /**
     * 一个Button组件
     * @param text 必须携带的文本内容组件，按钮的文本内容
     * @param event 可选的按钮执行事件组件
     * @param style 该按钮的样式
     */
    data class Button(
        val text: String,
        val event: Event?,
        val style: HomeButtonType,
        override val params: String
    ) : MarkdownBlock {
        override val stableKey: Any get() = "btn_${text}_${event}_${style}_${params.hashCode()}"

        /**
         * 按钮事件
         */
        data class Event(
            val key: String,
            val data: String? = null
        )
    }

    /**
     * 图片组件
     * @param url 必须携带的图片链接
     * @param width 可选的宽度属性
     */
    data class Image(
        val url: String,
        val width: Width?,
        override val params: String
    ) : MarkdownBlock {
        override val stableKey: Any get() = "img_${url}_width=${width}_${params.hashCode()}"

        sealed interface Width {
            data class Percent(val value: Float): Width
            data class DP(val value: Dp): Width
        }
    }

    /**
     * Row组件，和Compose原生的Row一致
     */
    data class RowBlock(
        val horizontal: Arrangement.Horizontal,
        val vertical: Alignment.Vertical,
        val children: List<MarkdownBlock>,
        override val params: String
    ) : MarkdownBlock {
        override val stableKey: Any get() = "row_${children.hashCode()}"
    }
}



private val blockPattern = Regex(
    """^[ \t]*(?:(\.\.\.card-start([^\n]*))|(\.\.\.button(-outlined|-filled-tonal|-text)?\s+([^\n]*))|(\.\.\.row-start([^\n]*))|(\.\.\.image\s+([^\n]*)))""",
    RegexOption.MULTILINE
)

/**
 * 由于扩展组件相对独立，需要将其拆分出来，作为单独的部分进行渲染，
 * 这里会把内容进行拆分成多个块
 */
fun parseMarkdownBlocks(
    content: String,
    parseMarkdown: (String) -> AstNode,
): List<MarkdownBlock> {
    var inCodeBlock = false
    val cleaned = content.lineSequence()
        .filter { line ->
            val trimmed = line.trimStart()
            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock
                return@filter true
            }
            if (inCodeBlock) return@filter true
            //清洗掉在代码框之外的注释行
            !trimmed.startsWith("//")
        }
        .joinToString("\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\u2028")

    return parseMarkdownBlocksInternal(
        cleared = cleaned,
        parseMarkdown = parseMarkdown
    )
}


private val titleRegex = Regex("""title\s*=\s*"([^"]*)"""")
private val rowEndPattern = Regex("(?m)^[ \t]*\\.\\.\\.row-end")
private fun parseMarkdownBlocksInternal(
    cleared: String,
    parseMarkdown: (String) -> AstNode,
    allowCard: Boolean = true,
    allowRow: Boolean = true,
): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()

    var lastIndex = 0
    while (lastIndex < cleared.length) {
        val match = blockPattern.find(cleared, lastIndex)
        if (match == null) {
            //没有更多匹配，添加剩余内容
            val remaining = cleared.substring(lastIndex).trim()
            if (remaining.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(astNode = parseMarkdown(remaining)))
            }
            break
        }

        //处理匹配项之前的普通文本
        if (match.range.first > lastIndex) {
            val text = cleared.substring(lastIndex, match.range.first).trim()
            if (text.isNotEmpty()) {
                blocks.add(MarkdownBlock.Normal(astNode = parseMarkdown(text)))
            }
        }

        val isCardStart = match.groupValues[1].isNotEmpty()
        val isButton = match.groupValues[3].isNotEmpty()
        val isRowStart = match.groupValues[6].isNotEmpty()
        val isImage = match.groupValues[8].isNotEmpty()

        when {
            isCardStart && allowCard -> {
                val params = match.groupValues[2]
                val title = titleRegex.find(params)?.groupValues?.get(1) ?: ""
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
                                parseMarkdown = parseMarkdown,
                                allowCard = false, //不允许内部嵌套卡片组件
                                allowRow = true
                            )
                        )
                    )
                    lastIndex = closingRange.last + 1
                } else {
                    //如果没找到匹配的结束标记，则将此开始标记视为Markdown
                    blocks.add(
                        MarkdownBlock.Normal(
                            astNode = parseMarkdown(match.value)
                        )
                    )
                    lastIndex = match.range.last + 1
                }
            }

            isRowStart && allowRow -> {
                val params = match.groupValues[7]
                val closingMatch = rowEndPattern.find(cleared, match.range.last + 1)
                if (closingMatch != null) {
                    val innerContent = cleared.substring(
                        startIndex = match.range.last + 1,
                        endIndex = closingMatch.range.first
                    ).trim('\n')

                    val children = parseMarkdownBlocksInternal(
                        cleared = innerContent,
                        parseMarkdown = parseMarkdown,
                        allowCard = false,
                        allowRow = false
                    ).filter { it is MarkdownBlock.Button || it is MarkdownBlock.Image }

                    blocks.add(
                        MarkdownBlock.RowBlock(
                            horizontal = parseHorizontalArrangement(params = params),
                            vertical = parseVerticalAlignment(params),
                            children = children,
                            params = params
                        )
                    )
                    lastIndex = closingMatch.range.last + 1
                } else {
                    blocks.add(
                        MarkdownBlock.Normal(
                            astNode = parseMarkdown(match.value)
                        )
                    )
                    lastIndex = match.range.last + 1
                }
            }

            isButton -> {
                parseButton(
                    styleSuffix = match.groupValues[4],
                    params = match.groupValues[5]
                )?.let { button ->
                    blocks.add(button)
                }
                lastIndex = match.range.last + 1
            }

            isImage -> {
                parseImage(match.groupValues[9])?.let { image ->
                    blocks.add(image)
                }

                lastIndex = match.range.last + 1
            }

            else -> {
                //如果是标记但当前上下文不允许（比如卡片嵌套），则视为普通Markdown
                blocks.add(
                    MarkdownBlock.Normal(
                        astNode = parseMarkdown(match.value)
                    )
                )
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
    val pattern = Regex("(?m)^[ \t]*(?:($openTagPattern)|(${Regex.escape(closeTag)}))")

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

private val buttonTextRegex = Regex("""text\s*=\s*"([^"]*)"""")
private val buttonEventRegex = Regex("""event\s*=\s*"([^"]*)"""")
private val buttonEventDataRegex = Regex("""^([^\s{]+)(?:\s*\{([\s\S]*)\})?$""")
private fun parseButton(
    styleSuffix: String,
    params: String
): MarkdownBlock.Button? {
    val style = when (styleSuffix) {
        "-outlined" -> HomeButtonType.Outlined
        "-filled-tonal" -> HomeButtonType.FilledTonal
        "-text" -> HomeButtonType.Text
        else -> HomeButtonType.Filled
    }
    val text = buttonTextRegex.find(params)?.groupValues?.get(1) ?: return null
    val eventValue = buttonEventRegex.find(params)?.groupValues?.get(1)
    val event = eventValue?.let { eventValue0 ->
        val value = buttonEventDataRegex.find(eventValue0) ?: return@let null
        val eventKey = value.groupValues.getOrNull(1) ?: return@let null
        val eventData = value.groupValues.getOrNull(2)
        MarkdownBlock.Button.Event(eventKey, eventData)
    }
    return MarkdownBlock.Button(
        text = text,
        event = event,
        style = style,
        params = params
    )
}

private val horizontalRegex = Regex("""horizontal\s*=\s*(spacedBy\([^)]*\)|[^\s\t\n]+)""")
private val spacedByRegex = Regex("""spacedBy\(\s*(\d+(?:\.\d+)?)\s*(?:,\s*(\w+))?\s*\)""")

private fun parseHorizontalArrangement(
    params: String,
): Arrangement.Horizontal {
    val horizontalValue = horizontalRegex.find(params)?.groupValues?.get(1)?.trim()

    if (horizontalValue != null) {
        spacedByRegex.find(horizontalValue)?.let { match ->
            val space = match.groupValues[1].toFloatOrNull() ?: 0f
            val alignment = when (match.groupValues[2]) {
                "Start" -> Alignment.Start
                "End" -> Alignment.End
                "Center" -> Alignment.CenterHorizontally
                else -> null
            }
            return if (alignment != null) {
                Arrangement.spacedBy(space.dp, alignment)
            } else {
                Arrangement.spacedBy(space.dp)
            }
        }
        return when (horizontalValue) {
            "Start" -> Arrangement.Start
            "End" -> Arrangement.End
            "Center" -> Arrangement.Center
            "SpaceEvenly" -> Arrangement.SpaceEvenly
            "SpaceBetween" -> Arrangement.SpaceBetween
            "SpaceAround" -> Arrangement.SpaceAround
            else -> Arrangement.Start
        }
    }

    return Arrangement.Start
}

private val verticalRegex = Regex("""vertical\s*=\s*(\w+)""")
private fun parseVerticalAlignment(params: String): Alignment.Vertical {
    val verticalValue = verticalRegex.find(params)?.groupValues?.get(1)

    return when (verticalValue) {
        "Top" -> Alignment.Top
        "Center", "CenterVertically" -> Alignment.CenterVertically
        "Bottom" -> Alignment.Bottom
        else -> Alignment.Top
    }
}

private val shapeRegex = Regex("""shape\s*=\s*([^\s\t\n]+)""")
@Composable
private fun parseShape(params: String): Shape? {
    val shapeValue = shapeRegex.find(params)?.groupValues?.get(1) ?: return null
    return when (shapeValue) {
        "extraSmall" -> MaterialTheme.shapes.extraSmall
        "small" -> MaterialTheme.shapes.small
        "medium" -> MaterialTheme.shapes.medium
        "large" -> MaterialTheme.shapes.large
        "extraLarge" -> MaterialTheme.shapes.extraLarge
        else -> {
            when {
                shapeValue.endsWith("dp") -> {
                    val size = shapeValue.dropLast(2).toFloatOrNull() ?: return null
                    RoundedCornerShape(size.dp)
                }
                shapeValue.endsWith("%") -> {
                    val percent = shapeValue.dropLast(1).toIntOrNull() ?: return null
                    RoundedCornerShape(percent)
                }
                else -> null
            }
        }
    }
}

private val paddingRegex = Regex("""contentPadding\s*=\s*\(([\d.\s,]+)\)""")
private fun parseCardPadding(params: String): PaddingValues? {
    val match = paddingRegex.find(params) ?: return null
    val values = match.groupValues[1]
        .split(",")
        .map {
            it.trim().toFloatOrNull() ?: 0f
        }
    return when (values.size) {
        1 -> PaddingValues(
            all = values[0].dp
        )
        2 -> PaddingValues(
            horizontal = values[0].dp,
            vertical = values[1].dp
        )
        4 -> PaddingValues(
            start = values[0].dp,
            top = values[1].dp,
            end = values[2].dp,
            bottom = values[3].dp
        )
        else -> null
    }
}

private val urlRegex = Regex("""url\s*=\s*"([^"]*)"""")
private val widthRegex = Regex("""width\s*=\s*(\d+%|\d+(?:\.\d+)?dp)""")
private fun parseImage(params: String): MarkdownBlock.Image? {
    val url = urlRegex.find(params)?.groupValues?.get(1) ?: return null
    val widthParam = widthRegex.find(params)?.groupValues?.get(1)?.let { w ->
        when {
            w.endsWith("%") -> {
                val percent = w.dropLast(1).toIntOrNull() ?: 100
                MarkdownBlock.Image.Width.Percent((percent.toFloat() / 100f).coerceIn(0f, 1f))
            }
            w.endsWith("dp") -> {
                val value = w.dropLast(2).toFloatOrNull() ?: 0f
                MarkdownBlock.Image.Width.DP(value.dp)
            }
            else -> return null
        }
    }

    return MarkdownBlock.Image(
        url = url,
        width = widthParam,
        params = params
    )
}

private val weightRegex = Regex("""weight\s*=\s*\(([\d.]+)(?:,\s*(noFill))?\)""")
private fun parseWeight(params: String): Pair<Float, Boolean>? {
    val match = weightRegex.find(params) ?: return null
    val weight = match.groupValues[1].toFloatOrNull() ?: return null
    val fill = match.groupValues[2] != "noFill"
    return Pair(weight, fill)
}