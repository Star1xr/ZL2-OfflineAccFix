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

import com.star1xr.treelauncher.ui.screens.game.elements.log_parser.LogParseCore
import io.github.rosemoe.sora.lang.analysis.SimpleAnalyzeManager
import io.github.rosemoe.sora.lang.styling.MappedSpans
import io.github.rosemoe.sora.lang.styling.SpanFactory
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * 共用核心逻辑: [LogParseCore]
 */
class LogAnalyzeManager : SimpleAnalyzeManager<Any?>() {
    private val timeStyle        = TextStyle.makeStyle(EditorColorScheme.COMMENT,        0, false, false, false)
    private val stringStyle      = TextStyle.makeStyle(EditorColorScheme.LITERAL,        0, false, false, false)
    private val numberStyle      = TextStyle.makeStyle(EditorColorScheme.IDENTIFIER_VAR,  0, false, false, false)
    private val packageStyle     = TextStyle.makeStyle(EditorColorScheme.IDENTIFIER_VAR,  0, false, false, false)
    private val linkStyle        = TextStyle.makeStyle(EditorColorScheme.FUNCTION_NAME,  0, false, false, false)
    private val normalStyle      = TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL,    0, false, false, false)
    
    private val logLevelStyle    = TextStyle.makeStyle(EditorColorScheme.KEYWORD,       0, false, false, false)
    private val stackTraceStyle  = TextStyle.makeStyle(EditorColorScheme.ANNOTATION,    0, false, false, false)

    override fun analyze(text: StringBuilder, delegate: Delegate<Any?>): Styles {
        val builder = MappedSpans.Builder()
        val lines = text.toString().split("\n")

        var inStackTrace = false

        for (lineIndex in lines.indices) {
            if (delegate.isCancelled) break
            val line = lines[lineIndex]

            builder.add(lineIndex, SpanFactory.obtainNoExt(0, normalStyle))

            //Java 异常堆栈
            if (LogParseCore.isStackTraceStart(line)) {
                inStackTrace = true
            }

            if (inStackTrace) {
                builder.add(lineIndex, SpanFactory.obtainNoExt(0, stackTraceStyle))
                if (lineIndex + 1 < lines.size &&
                    !LogParseCore.isStackTraceLine(lines[lineIndex + 1])
                ) {
                    inStackTrace = false
                }
                continue
            }

            analyzeLogLine(line, lineIndex, builder)
        }

        builder.determine(if (lines.isEmpty()) 0 else lines.size - 1)
        return Styles(builder.build())
    }

    private fun addSpan(
        builder: MappedSpans.Builder,
        line: Int,
        start: Int,
        length: Int,
        style: Long
    ) {
        val end = start + length
        builder.add(line, SpanFactory.obtainNoExt(start, style))
        builder.add(line, SpanFactory.obtainNoExt(end, normalStyle))
    }

    private fun analyzeLogLine(line: String, lineIndex: Int, builder: MappedSpans.Builder) {
        var i = 0
        val n = line.length

        while (i < n) {
            //字符串
            if (line[i] == '"' || line[i] == '\'') {

                val quote = line[i]

                if (quote == '\'' && i > 0 && line[i - 1] != ' ') {
                    //单引号必须前面是空格
                } else {
                    val end = LogParseCore.findClosingQuote(line, i, quote)
                    if (end != -1) {
                        addSpan(builder, lineIndex, i, end - i + 1, stringStyle)
                        i = end + 1
                        continue
                    }
                }
            }

            //链接
            val linkMatch = LogParseCore.matchWebLink(line, i)
            if (linkMatch != null) {
                addSpan(builder, lineIndex, i, linkMatch.length, linkStyle)
                i += linkMatch.length
                continue
            }

            //时间戳
            val timeMatch = LogParseCore.matchTime(line, i)
            if (timeMatch != null) {
                addSpan(builder, lineIndex, i, timeMatch.length, timeStyle)
                i += timeMatch.length
                continue
            }

            //日志等级
            if (LogParseCore.isLogLevel(line, i)) {
                val level = LogParseCore.matchLogLevel(line, i)
                if (level != null) {
                    val end = i + level.length

                    if (end >= n || !line[end].isLetterOrDigit()) {
                        addSpan(builder, lineIndex, i, level.length, logLevelStyle)
                        i += level.length
                        continue
                    }
                }
            }

            //数字
            if (line[i].isDigit()) {
                val segment = LogParseCore.scanNumericSegment(line, i)
                if (segment != null) {
                    if (LogParseCore.isIndependentNumber(line, i, segment.length)) {
                        addSpan(builder, lineIndex, i, segment.length, numberStyle)
                    }
                    i += segment.length
                    continue
                }
            }

            //包名
            val packageName = LogParseCore.scanPackageName(line, i)
            if (packageName != null) {
                addSpan(builder, lineIndex, i, packageName.length, packageStyle)
                i += packageName.length
                continue
            }

            i++
        }
    }

    override fun destroy() {
        super.destroy()
    }
}
