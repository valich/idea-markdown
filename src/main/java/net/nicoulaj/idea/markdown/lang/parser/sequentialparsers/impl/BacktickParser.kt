/*
 * Copyright (c) 2011-2014 Julien Nicoulaud <julien.nicoulaud@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.impl

import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParser
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserUtil
import java.util.ArrayList

public class BacktickParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()

        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)
        val delegateIndices = ArrayList<Int>()

        var i = 0
        while (i < indices.size()) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {

                val j = findOfSize(tokens, indices, i + 1, getLength(iterator, true))

                if (j != -1) {
                    result.withNode(SequentialParser.Node(indices.get(i)..indices.get(j) + 1, MarkdownElementTypes.CODE_SPAN))
                    i = j
                    continue
                }
            }
            delegateIndices.add(indices.get(i))
            ++i
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    private fun findOfSize(tokens: TokensCache, indices: List<Int>, from: Int, length: Int): Int {
        for (i in from..indices.size() - 1) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type != MarkdownTokenTypes.BACKTICK && iterator.type != MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                continue
            }

            if (getLength(iterator, false) == length) {
                return i
            }
        }
        return -1
    }


    private fun getLength(info: TokensCache.Iterator, canEscape: Boolean): Int {
        val tokenText = info.text

        var toSubtract = 0
        if (info.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
            if (canEscape) {
                toSubtract = 2
            } else {
                toSubtract = 1
            }
        }

        return tokenText.length() - toSubtract
    }
}
