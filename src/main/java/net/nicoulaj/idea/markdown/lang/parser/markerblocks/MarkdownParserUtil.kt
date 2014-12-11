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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks

import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.TokensCache

public class MarkdownParserUtil private() {
    class object {

        public fun calcNumberOfConsequentEols(iterator: TokensCache.Iterator): Int {
            var it = iterator
            var answer = 0
            while (true) {
                val `type` = it.type
                if (`type` != MarkdownTokenTypes.EOL) {
                    return answer
                }
                it = it.advance()
                answer++
            }
        }

        public fun getFirstNextLineNonBlockquoteRawIndex(iterator: TokensCache.Iterator): Int {
            assert(iterator.type == MarkdownTokenTypes.EOL)

            var answer = 1
            while (true) {
                val `type` = iterator.rawLookup(answer)
                if (`type` != MarkdownTokenTypes.WHITE_SPACE && `type` != MarkdownTokenTypes.BLOCK_QUOTE) {
                    return answer
                }
                answer++
            }
        }

        public fun getFirstNonWhiteSpaceRawIndex(iterator: TokensCache.Iterator): Int {
            var answer = 0
            while (true) {
                val `type` = iterator.rawLookup(answer)
                if (`type` != MarkdownTokenTypes.WHITE_SPACE && `type` != MarkdownTokenTypes.EOL) {
                    return answer
                }
                answer++
            }
        }

        public fun getFirstNonWhitespaceLineEolRawIndex(iterator: TokensCache.Iterator): Int {
            assert(iterator.type == MarkdownTokenTypes.EOL)

            val lastIndex = getFirstNonWhiteSpaceRawIndex(iterator)
            var index = lastIndex - 1
            while (index >= 0) {
                if (iterator.rawLookup(index) == MarkdownTokenTypes.EOL) {
                    return index
                }
                --index
            }
            throw AssertionError("Could not be here: 0 is EOL")
        }

        public fun getIndentBeforeRawToken(iterator: TokensCache.Iterator, rawOffset: Int): Int {
            var eolPos = rawOffset - 1
            while (true) {
                val `type` = iterator.rawLookup(eolPos)
                if (`type` == MarkdownTokenTypes.EOL || `type` == null) {
                    break
                }

                eolPos--
            }

            return iterator.rawStart(rawOffset) - iterator.rawStart(eolPos + 1)
        }
    }

}
