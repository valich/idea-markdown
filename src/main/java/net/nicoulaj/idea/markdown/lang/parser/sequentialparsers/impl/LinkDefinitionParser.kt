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

public class LinkDefinitionParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val resultNodes = ArrayList<SequentialParser.Node>()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        val iterator = tokens.ListIterator(indices, 0)

        if (parseLinkDefinition(resultNodes, delegateIndices, iterator) != null) {
            return SequentialParser.ParsingResult().withNodes(resultNodes).withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
        }

        return SequentialParser.ParsingResult().withFurtherProcessing(rangesToGlue)
    }

    private fun parseLinkDefinition(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
        val startIndex = iterator.index
        var it = iterator
        var testingIt : TokensCache.Iterator?

        testingIt = LinkParserUtil.parseLinkLabel(result, delegateIndices, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt!!
        if (it.rawLookup(1) != MarkdownTokenTypes.COLON) {
            return null
        }
        it = it.advance().advance()
        if (it.type == MarkdownTokenTypes.EOL) {
            it = it.advance()
        }

        testingIt = LinkParserUtil.parseLinkDestination(result, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt!!
        it = it.advance()
        if (it.type == MarkdownTokenTypes.EOL) {
            it = it.advance()
        }

        testingIt = LinkParserUtil.parseLinkTitle(result, it)
        if (testingIt == null) {
            return null
        }
        it = testingIt!!

        val nextType = it.advance().type
        if (nextType != null && nextType != MarkdownTokenTypes.EOL) {
            return null
        }

        result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.LINK_DEFINITION))
        return it
    }

}
