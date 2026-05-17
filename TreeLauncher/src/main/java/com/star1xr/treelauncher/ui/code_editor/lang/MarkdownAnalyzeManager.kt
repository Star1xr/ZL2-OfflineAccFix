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

package com.star1xr.treelauncher.ui.code_editor.lang

import io.github.rosemoe.sora.lang.analysis.SimpleAnalyzeManager
import io.github.rosemoe.sora.lang.styling.MappedSpans
import io.github.rosemoe.sora.lang.styling.SpanFactory
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Markdown配色解析器
 * @param homePageExtra 是否启用自定义主页扩展语法识别
 */
class MarkdownAnalyzeManager(
    val homePageExtra: Boolean
) : SimpleAnalyzeManager<Any?>() {
    private val headerStyle  = TextStyle.makeStyle(EditorColorScheme.KEYWORD,      0, true, false, false)
    private val boldStyle    = TextStyle.makeStyle(EditorColorScheme.FUNCTION_NAME, 0, true, false, false)
    private val italicStyle  = TextStyle.makeStyle(EditorColorScheme.FUNCTION_NAME, 0, false, true, false)
    private val codeStyle    = TextStyle.makeStyle(EditorColorScheme.ANNOTATION,    0, false, false, false)
    private val linkStyle    = TextStyle.makeStyle(EditorColorScheme.LITERAL,       0, false, false, false)
    private val quoteStyle   = TextStyle.makeStyle(EditorColorScheme.COMMENT,       0, false, true, false)
    private val operatorStyle= TextStyle.makeStyle(EditorColorScheme.OPERATOR,      0, false, false, false)
    private val normalStyle  = TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)

    //扩展组件样式
    /** 扩展组件 */
    private val componentStyle = TextStyle.makeStyle(EditorColorScheme.KEYWORD,      0, true, false, false)
    /** 扩展组件属性名 */
    private val attrNameStyle  = TextStyle.makeStyle(EditorColorScheme.ANNOTATION,   0, false, false, false)
    /** 扩展组件属性值 */
    private val attrValueStyle = TextStyle.makeStyle(EditorColorScheme.LITERAL,      0, false, false, false)
    /** 扩展语法：注释 */
    private val commentStyle   = TextStyle.makeStyle(EditorColorScheme.COMMENT,      0, false, true, false)

    /**
     * 扩展组件行正则
     */
    private val compRegex = Regex(
        """^(\s*)(\.\.\.(?:card-start|card-end|row-start|row-end|column-start|column-end|button(?:-outlined|-filled-tonal|-text)?|image))(\s+.*)?$"""
    )

    private val headerRegex = Regex("^#{1,6}\\s+")
    private val listRegex = Regex("^\\s*([-*+]|\\d+\\.)\\s+")
    private val quoteRegex = Regex("^\\s*>")
    private val inlineRegex = Regex("(\\*\\*.*?\\*\\*)|(__.*?__)|(\\*.*?\\*)|(_.*?_)|(`.*?`)|(\\[.*?]\\(.*?\\))")

    override fun analyze(text: StringBuilder, delegate: Delegate<Any?>): Styles {
        val builder = MappedSpans.Builder()
        val lines = text.toString().split("\n")
        var inCodeBlock = false

        for (lineIndex in lines.indices) {
            if (delegate.isCancelled) break
            val line = lines[lineIndex]

            if (line.trimStart().startsWith("```")) {
                inCodeBlock = !inCodeBlock
                builder.add(lineIndex, SpanFactory.obtainNoExt(0, codeStyle))
                continue
            }
            if (inCodeBlock) {
                //代码块内行全为代码样式
                builder.add(lineIndex, SpanFactory.obtainNoExt(0, codeStyle))
                continue
            }

            //在代码框外，处理注释上色
            if (homePageExtra && line.trimStart().startsWith("//")) {
                val commentStart = line.indexOf("//")
                builder.add(lineIndex, SpanFactory.obtainNoExt(commentStart, commentStyle))
                continue
            }

            builder.addIfNeeded(lineIndex, 0, normalStyle)

            if (homePageExtra) {
                //扩展组件
                val compMatch = compRegex.find(line)
                if (compMatch != null) {
                    //组件名标识
                    val indent = compMatch.groups[1]?.value?.length ?: 0
                    val compName = compMatch.groups[2]?.value ?: ""
                    builder.add(lineIndex, SpanFactory.obtainNoExt(indent, componentStyle))
                    val nameEnd = indent + compName.length
                    builder.add(lineIndex, SpanFactory.obtainNoExt(nameEnd, normalStyle))

                    //提取属性文本并解析
                    val rawParams = compMatch.groups[3]?.value ?: ""
                    val paramsText = rawParams.trimStart()
                    val paramsOffset = if (rawParams.isBlank()) {
                        nameEnd
                    } else {
                        nameEnd + rawParams.indexOfFirst { !it.isWhitespace() }
                    }
                    val tokens = parseParams(paramsText, paramsOffset)

                    for (token in tokens) {
                        when (token) {
                            is ParamToken.Key -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, attrNameStyle)
                            )

                            is ParamToken.Equal -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, operatorStyle)
                            )

                            is ParamToken.StringStart, is ParamToken.StringEnd
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, operatorStyle)
                            )

                            is ParamToken.StringContent
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, attrValueStyle)
                            )

                            is ParamToken.ParameterizedStart, is ParamToken.ParameterizedEnd
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, operatorStyle)
                            )

                            is ParamToken.ParameterizedContent
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, componentStyle)
                            )

                            is ParamToken.Number, is ParamToken.Constant
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, attrValueStyle)
                            )

                            is ParamToken.FunStart, is ParamToken.FunEnd, is ParamToken.Comma
                                -> builder.add(
                                lineIndex,
                                SpanFactory.obtainNoExt(token.pos, operatorStyle)
                            )
                        }
                    }
                    continue
                }
            }

            // Markdown 标题
            val headerMatch = headerRegex.find(line)
            if (headerMatch != null) {
                builder.add(lineIndex, SpanFactory.obtainNoExt(0, headerStyle))
                builder.add(lineIndex, SpanFactory.obtainNoExt(headerMatch.range.last + 1, normalStyle))
                continue
            }
            // Markdown 列表项符号
            val listMatch = listRegex.find(line)
            if (listMatch != null) {
                builder.add(lineIndex, SpanFactory.obtainNoExt(listMatch.range.first, operatorStyle))
                builder.add(lineIndex, SpanFactory.obtainNoExt(listMatch.range.last + 1, normalStyle))
            }
            // Markdown 区块引用
            val quoteMatch = quoteRegex.find(line)
            if (quoteMatch != null) {
                builder.add(lineIndex, SpanFactory.obtainNoExt(quoteMatch.range.first, quoteStyle))
                builder.add(lineIndex, SpanFactory.obtainNoExt(quoteMatch.range.last + 1, normalStyle))
            }

            // Markdown 内联样式（粗体、斜体、链接、行内代码）
            inlineRegex.findAll(line).forEach { m ->
                val style = when {
                    m.value.startsWith("**") || m.value.startsWith("__") -> boldStyle
                    m.value.startsWith("*") || m.value.startsWith("_")    -> italicStyle
                    m.value.startsWith("`")                              -> codeStyle
                    m.value.startsWith("[")                              -> linkStyle
                    else -> normalStyle
                }
                builder.add(lineIndex, SpanFactory.obtainNoExt(m.range.first, style))
                builder.add(lineIndex, SpanFactory.obtainNoExt(m.range.last + 1, normalStyle))
            }
        }

        builder.determine(if (lines.isEmpty()) 0 else lines.size - 1)
        return Styles(builder.build())
    }

    private sealed class ParamToken {
        abstract val pos: Int

        /** 参数键 */
        data class Key(override val pos: Int) : ParamToken()
        data class Equal(override val pos: Int) : ParamToken()
        /** 字符串值，双引号字符串，起始 " */
        data class StringStart(override val pos: Int) : ParamToken()
        /** 字符串值，双引号字符串，字符串内容 */
        data class StringContent(override val pos: Int) : ParamToken()
        /** 字符串值，双引号字符串，结束 " */
        data class StringEnd(override val pos: Int) : ParamToken()
        /** 带参字符串，在双引号字符串内，以花括号包裹的参数，起始 { */
        data class ParameterizedStart(override val pos: Int) : ParamToken()
        /** 带参字符串，在双引号字符串内，以花括号包裹的参数，参数内容 */
        data class ParameterizedContent(override val pos: Int) : ParamToken()
        /** 带参字符串，在双引号字符串内，以花括号包裹的参数，结束 } */
        data class ParameterizedEnd(override val pos: Int) : ParamToken()
        /** 数值（可带单位） */
        data class Number(override val pos: Int) : ParamToken()
        /** 常量/枚举类型，以单词形式直接赋值 */
        data class Constant(override val pos: Int) : ParamToken()
        /** 函数赋值类型，可接收多个参数的情况，起始 ( */
        data class FunStart(override val pos: Int) : ParamToken()
        /** 函数赋值类型，可接收多个参数的情况，结束 ) */
        data class FunEnd(override val pos: Int) : ParamToken()
        data class Comma(override val pos: Int) : ParamToken()
    }

    /**
     * 解析属性字符串为 Token 列表
     */
    private fun parseParams(text: String, baseOffset: Int): List<ParamToken> {
        val tokens = mutableListOf<ParamToken>()
        var i = 0
        val len = text.length
        while (i < len) {
            if (text[i].isWhitespace()) { i++; continue }

            //解析键名
            val keyStart = i
            while (i < len && (text[i].isLetterOrDigit() || text[i] == '-')) {
                i++
            }
            tokens.add(ParamToken.Key(baseOffset + keyStart))

            while (i < len && text[i].isWhitespace()) { i++ }
            if (i < len && text[i] == '=') {
                tokens.add(ParamToken.Equal(baseOffset + i))
                i++
            }

            while (i < len && text[i].isWhitespace()) { i++ }
            if (i >= len) break

            //解析值
            when {
                //字符串值 key="value"
                text[i] == '"' -> {
                    tokens.add(ParamToken.StringStart(baseOffset + i))
                    i++
                    while (i < len && text[i] != '"') {
                        //解析带参字符串格式
                        //key="value{data}"
                        if (text[i] == '{') {
                            tokens.add(ParamToken.ParameterizedStart(baseOffset + i))
                            i++
                            while (i < len && text[i] != '}') {
                                tokens.add(ParamToken.ParameterizedContent(baseOffset + i))
                                i++
                            }
                            if (i < len && text[i] == '}') {
                                tokens.add(ParamToken.ParameterizedEnd(baseOffset + i))
                                i++
                            }
                        } else {
                            tokens.add(ParamToken.StringContent(baseOffset + i))
                            i++
                        }
                    }
                    if (i < len && text[i] == '"') {
                        tokens.add(ParamToken.StringEnd(baseOffset + i))
                        i++
                    }
                }
                //函数类型赋值
                text[i] == '(' -> {
                    tokens.add(ParamToken.FunStart(baseOffset + i))
                    i++
                    while (i < len && text[i] != ')') {
                        if (text[i] == ',') {
                            tokens.add(ParamToken.Comma(baseOffset + i))
                            i++
                        } else if (text[i].isWhitespace()) {
                            i++
                        } else if (text[i].isDigit()) {
                            val numStart = i
                            while (i < len && (text[i].isDigit() || text[i] == '.')) {
                                i++
                            }
                            tokens.add(ParamToken.Number(baseOffset + numStart))
                            //包含 dp 或 %
                            if (i < len && text[i] == '%') {
                                tokens.add(ParamToken.Number(baseOffset + i))
                                i++
                            } else if (i+1 < len && text[i] == 'd' && text[i+1] == 'p') {
                                tokens.add(ParamToken.Number(baseOffset + i))
                                i += 2
                            }
                        } else {
                            val constStart = i
                            while (i < len && text[i].isLetter()) {
                                i++
                            }
                            tokens.add(ParamToken.Constant(baseOffset + constStart))
                        }
                    }
                    if (i < len && text[i] == ')') {
                        tokens.add(ParamToken.FunEnd(baseOffset + i))
                        i++
                    }
                }
                //数字值 (可带单位)
                text[i].isDigit() -> {
                    val numStart = i
                    while (i < len && (text[i].isDigit() || text[i] == '.')) {
                        i++
                    }
                    tokens.add(ParamToken.Number(baseOffset + numStart))
                    if (i < len && text[i] == '%') {
                        tokens.add(ParamToken.Number(baseOffset + i))
                        i++
                    } else if (i+1 < len && text[i] == 'd' && text[i+1] == 'p') {
                        tokens.add(ParamToken.Number(baseOffset + i))
                        i += 2
                    }
                }
                //常量/枚举值
                else -> {
                    val constStart = i
                    while (i < len && (text[i].isLetter() || text[i] == '_')) {
                        i++
                    }
                    if (constStart < i) {
                        tokens.add(ParamToken.Constant(baseOffset + constStart))
                    } else {
                        //跳过无法识别的字符，避免无限循环
                        i++
                    }
                }
            }
        }
        return tokens
    }

    override fun destroy() {
        super.destroy()
    }
}
