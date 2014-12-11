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
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers

import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import java.util.ArrayList

public class SequentialParserUtil {
    class object {
        public fun textRangesToIndices(ranges: Collection<Range<Int>>): List<Int> {
            val result = ArrayList<Int>()
            for (range in ranges) {
                for (i in range.start..range.end - 1) {
                    result.add(i)
                }
            }
            result.sort()
            return result
        }

        public fun indicesToTextRanges(indices: List<Int>): Collection<Range<Int>> {
            val result = ArrayList<Range<Int>>()

            var starting = 0
            for (i in indices.indices) {
                if (i + 1 == indices.size() || indices.get(i) + 1 != indices.get(i + 1)) {
                    result.add(indices.get(starting)..indices.get(i) + 1)
                    starting = i + 1
                }
            }

            return result
        }

        public fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            val `type` = info.rawLookup(lookup)
            if (`type` == null) {
                return false
            }
            if (`type` == MarkdownTokenTypes.EOL || `type` == MarkdownTokenTypes.WHITE_SPACE) {
                return true
            }
            if (lookup == -1) {
                return info.rollback().text.endsWith(' ')
            } else {
                return info.advance().text.startsWith(' ')
            }
        }


        public fun filterBlockquotes(tokensCache: TokensCache, textRange: Range<Int>): Collection<Range<Int>> {
            val result = ArrayList<Range<Int>>()
            var lastStart = textRange.start

            val R = textRange.end
            for (i in lastStart..R - 1) {
                if (tokensCache.Iterator(i).type == MarkdownTokenTypes.BLOCK_QUOTE) {
                    if (lastStart < i) {
                        result.add(lastStart..i)
                    }
                    lastStart = i + 1
                }
            }
            if (lastStart < R) {
                result.add(lastStart..R)
            }
            return result
        }
    }

}
