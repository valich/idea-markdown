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
import java.util.Stack

public class EmphStrongParser : SequentialParser {

    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()

        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var myType: Char = 0.toChar()
        val openingOnes = Stack<Pair<Int, Int>>()

        var i = 0
        while (i < indices.size()) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type != MarkdownTokenTypes.EMPH) {
                i++
                continue
            }

            var numCanEnd = canEndNumber(tokens, iterator)
            if (numCanEnd != 0 && myType == getType(iterator) && !openingOnes.isEmpty()) {
                while (numCanEnd > 0 && !openingOnes.isEmpty()) {
                    val lastOpening = openingOnes.pop()
                    val toMakeMax = Math.min(lastOpening.second, numCanEnd)
                    val toMake = if (toMakeMax % 2 == 0) 2 else 1
                    val from = lastOpening.first + (lastOpening.second - toMake)
                    val to = i + toMake - 1

                    val nodeType = if (toMake == 2) MarkdownElementTypes.STRONG else MarkdownElementTypes.EMPH
                    result.withNode(SequentialParser.Node(indices.get(from)..indices.get(to) + 1, nodeType))

                    i += toMake
                    numCanEnd -= toMake
                    if (lastOpening.second > toMake) {
                        openingOnes.push(Pair(lastOpening.first, lastOpening.second - toMake))
                    }
                }
                continue
            }

            val numCanStart = canStartNumber(tokens, iterator)
            if (numCanStart != 0) {
                if (myType.toInt() == 0) {
                    myType = getType(iterator)
                } else if (myType != getType(iterator)) {
                    i++
                    continue
                }

                openingOnes.push(Pair(i, numCanStart))
                i += numCanStart
            }
            i++
        }

        return result
    }

    private fun canStartNumber(tokens: TokensCache, iterator: TokensCache.Iterator): Int {
        var it = iterator
        if (getType(it) == ITALIC && it.rawLookup(-1) != null) {
            if (Character.isLetterOrDigit(tokens.getRawCharAt(it.start - 1))) {
                return 0
            }
        }

        for (i in 0..50 - 1) {
            if (SequentialParserUtil.isWhitespace(it, 1)) {
                return 0
            }
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                return i + 1
            }
            it = it.advance()
        }

        return 50
    }

    private fun canEndNumber(tokens: TokensCache, iterator: TokensCache.Iterator): Int {
        var it = iterator
        if (SequentialParserUtil.isWhitespace(it, -1)) {
            return 0
        }

        for (i in 0..50 - 1) {
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                if (getType(it) == ITALIC && Character.isLetterOrDigit(tokens.getRawCharAt(it.end))) {
                    return 0
                }
                return i + 1
            }
            it = it.advance()
        }

        return 50
    }

    private fun getType(info: TokensCache.Iterator): Char {
        return info.text.charAt(0)
    }

    class object {

        val ITALIC: Char = '_'

        val BOLD: Char = '*'
    }
}
