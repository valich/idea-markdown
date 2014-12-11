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

public class AutolinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var i = 0
        while (i < indices.size()) {
            var iterator: TokensCache.Iterator = tokens.ListIterator(indices, i)

            if (iterator.type == MarkdownTokenTypes.LT && iterator.rawLookup(1) == MarkdownTokenTypes.AUTOLINK) {
                val start = i
                while (iterator.type != MarkdownTokenTypes.GT) {
                    iterator = iterator.advance()
                    i++
                }
                result.withNode(SequentialParser.Node(indices.get(start)..indices.get(i) + 1, MarkdownElementTypes.AUTOLINK))
            } else {
                delegateIndices.add(indices.get(i))
            }
            ++i
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }
}
