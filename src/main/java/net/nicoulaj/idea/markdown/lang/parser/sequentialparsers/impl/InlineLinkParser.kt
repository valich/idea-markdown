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

public class InlineLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val localDelegates = ArrayList<Int>()
                val resultNodes = ArrayList<SequentialParser.Node>()
                val afterLink = parseInlineLink(resultNodes, localDelegates, iterator)
                if (afterLink != null) {
                    iterator = afterLink.advance()
                    result = result.withNodes(resultNodes).withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(localDelegates))
                    continue
                }
            }

            delegateIndices.add(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    private fun parseInlineLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        val startIndex = iterator.index
        var it = iterator

        val afterText = LinkParserUtil.parseLinkText(result, delegateIndices, it)
        if (afterText == null) {
            return null
        }
        it = afterText
        if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
            return null
        }

        it = it.advance().advance()
        if (it.type == MarkdownTokenTypes.EOL) {
            it = it.advance()
        }
        val afterDestination = LinkParserUtil.parseLinkDestination(result, it)
        if (afterDestination != null) {
            it = afterDestination.advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
        }
        val afterTitle = LinkParserUtil.parseLinkTitle(result, it)
        if (afterTitle != null) {
            it = afterTitle.advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
        }
        if (it.type != MarkdownTokenTypes.RPAREN) {
            return null
        }

        result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.INLINE_LINK))
        return it
    }
}
