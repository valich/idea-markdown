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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks;

import com.intellij.openapi.diagnostic.Logger;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

public class MarkdownParserUtil {
    private final static Logger LOG = Logger.getInstance(MarkdownParserUtil.class);

    private MarkdownParserUtil() {
    }

    public static int calcNumberOfConsequentEols(@NotNull TokensCache.Iterator iterator) {
        for (int answer = 0;; answer++) {
            final IElementType type = iterator.getType();
            if (type != MarkdownTokenTypes.EOL) {
                return answer;
            }
            iterator = iterator.advance();
        }
    }

    public static int getFirstNextLineNonBlockquoteRawIndex(@NotNull TokensCache.Iterator iterator) {
        LOG.assertTrue(iterator.getType() == MarkdownTokenTypes.EOL);
        
        for (int answer = 1;; answer++) {
            final IElementType type = iterator.rawLookup(answer);
            if (type != MarkdownTokenTypes.WHITE_SPACE && type != MarkdownTokenTypes.BLOCK_QUOTE) {
                return answer;
            }
        }
    }

    public static int getFirstNonWhiteSpaceRawIndex(@NotNull TokensCache.Iterator iterator) {
        for (int answer = 0;; answer++) {
            final IElementType type = iterator.rawLookup(answer);
            if (type != MarkdownTokenTypes.WHITE_SPACE && type != MarkdownTokenTypes.EOL) {
                return answer;
            }
        }
    }

    public static int getFirstNonWhitespaceLineEolRawIndex(@NotNull TokensCache.Iterator iterator) {
        LOG.assertTrue(iterator.getType() == MarkdownTokenTypes.EOL);

        final int lastIndex = getFirstNonWhiteSpaceRawIndex(iterator);
        for (int index = lastIndex - 1; index >= 0; --index) {
            if (iterator.rawLookup(index) == MarkdownTokenTypes.EOL) {
                return index;
            }
        }
        throw new AssertionError("Could not be here: 0 is EOL");
    }

    public static int getIndentBeforeRawToken(@NotNull TokensCache.Iterator iterator, int rawOffset) {
        int eolPos = rawOffset - 1;
        while (true) {
            final IElementType type = iterator.rawLookup(eolPos);
            if (type == MarkdownTokenTypes.EOL || type == null) {
                break;
            }

            eolPos--;
        }

        return iterator.rawStart(rawOffset) - iterator.rawStart(eolPos + 1);
    }

}
