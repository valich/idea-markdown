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

public class ReferenceLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val localDelegates = ArrayList<Int>()
                val resultNodes = ArrayList<SequentialParser.Node>()
                val afterLink = parseReferenceLink(resultNodes, localDelegates, iterator)
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

    private fun parseReferenceLink(resultNodes: MutableCollection<SequentialParser.Node>, localDelegates: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        var result: TokensCache.Iterator?

        result = parseFullReferenceLink(resultNodes, localDelegates, iterator)
        if (result != null) {
            return result
        }
        resultNodes.clear()
        localDelegates.clear()
        result = parseShortReferenceLink(resultNodes, localDelegates, iterator)
        if (result != null) {
            return result
        }
        return null
    }

    private fun parseFullReferenceLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        val startIndex = iterator.index
        var it : TokensCache.Iterator? = iterator

        it = LinkParserUtil.parseLinkText(result, delegateIndices, it!!)
        if (it == null) {
            return null
        }
        it = it!!.advance()

        if (it!!.type == MarkdownTokenTypes.EOL) {
            it = it!!.advance()
        }

        it = LinkParserUtil.parseLinkLabel(result, delegateIndices, it!!)
        if (it == null) {
            return null
        }

        result.add(SequentialParser.Node(startIndex..it!!.index + 1, MarkdownElementTypes.FULL_REFERENCE_LINK))
        return it
    }

    private fun parseShortReferenceLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        val startIndex = iterator.index
        var it : TokensCache.Iterator? = iterator

        it = LinkParserUtil.parseLinkLabel(result, delegateIndices, it!!)
        if (it == null) {
            return null
        }

        val shortcutLinkEnd = it

        it = it!!.advance()
        if (it!!.type == MarkdownTokenTypes.EOL) {
            it = it!!.advance()
        }

        if (it!!.type == MarkdownTokenTypes.LBRACKET && it!!.rawLookup(1) == MarkdownTokenTypes.RBRACKET) {
            it = it!!.advance()
        } else {
            it = shortcutLinkEnd
        }

        result.add(SequentialParser.Node(startIndex..it!!.index + 1, MarkdownElementTypes.SHORT_REFERENCE_LINK))
        return it
    }

}
