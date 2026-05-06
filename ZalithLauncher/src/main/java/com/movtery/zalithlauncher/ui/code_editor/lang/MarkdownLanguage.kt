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

package com.movtery.zalithlauncher.ui.code_editor.lang

import android.os.Bundle
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.QuickQuoteHandler
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.text.TextRange
import io.github.rosemoe.sora.widget.SymbolPairMatch

class MarkdownLanguage(
    homePageExtra: Boolean
) : Language {
    private val analyzeManager = MarkdownAnalyzeManager(homePageExtra)

    override fun getAnalyzeManager(): AnalyzeManager = analyzeManager

    override fun getInterruptionLevel(): Int = Language.INTERRUPTION_LEVEL_SLIGHT

    @Throws(CompletionCancelledException::class)
    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {}

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int = 0

    override fun useTab(): Boolean = false

    override fun getFormatter(): Formatter = object : Formatter {
        override fun format(text: Content, cursorRange: TextRange) {}
        override fun formatRegion(text: Content, rangeToFormat: TextRange, cursorRange: TextRange) {}
        override fun setReceiver(receiver: Formatter.FormatResultReceiver?) {}
        override fun isRunning(): Boolean = false
        override fun destroy() {}
    }

    override fun getSymbolPairs(): SymbolPairMatch {
        return SymbolPairMatch.DefaultSymbolPairs().apply {
            putPair('`', SymbolPairMatch.SymbolPair("`", "`"))
        }
    }

    override fun getNewlineHandlers(): Array<NewlineHandler>? = null

    override fun getQuickQuoteHandler(): QuickQuoteHandler? = null

    override fun destroy() {
        analyzeManager.destroy()
    }
}