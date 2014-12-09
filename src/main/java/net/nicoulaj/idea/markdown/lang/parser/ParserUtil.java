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
package net.nicoulaj.idea.markdown.lang.parser;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ParserUtil {
    @NotNull
    public static List<Integer> textRangesToIndices(@NotNull Collection<TextRange> ranges) {
        List<Integer> result = ContainerUtil.newArrayList();
        for (TextRange range : ranges) {
            for (int i = range.getStartOffset(); i < range.getEndOffset(); ++i) {
                result.add(i);
            }
        }
        ContainerUtil.sort(result);
        return result;
    }

    @NotNull
    public static Collection<TextRange> indicesToTextRanges(@NotNull List<Integer> indices) {
        Collection<TextRange> result = ContainerUtil.newArrayList();

        int starting = 0;
        for (int i = 0; i < indices.size(); ++i) {
            if (i + 1 == indices.size() || indices.get(i) + 1 != indices.get(i + 1)) {
                result.add(TextRange.create(indices.get(starting), indices.get(i) + 1));
                starting = i + 1;
            }
        }

        return result;
    }

    public static boolean isWhitespace(TokensCache.Iterator info, int lookup) {
        IElementType type = info.rawLookup(lookup);
        if (type == null) {
            return false;
        }
        if (type == MarkdownTokenTypes.EOL || type == MarkdownTokenTypes.WHITE_SPACE) {
            return true;
        }
        if (lookup == -1) {
            return StringUtil.endsWithChar(info.rollback().getText(), ' ');
        }
        else {
            return StringUtil.startsWithChar(info.advance().getText(), ' ');
        }
    }


    @NotNull
    public static Collection<TextRange> filterBlockquotes(@NotNull TokensCache tokensCache, @NotNull TextRange textRange) {
        Collection<TextRange> result = ContainerUtil.newArrayList();
        int lastStart = textRange.getStartOffset();

        final int R = textRange.getEndOffset();
        for (int i = lastStart; i < R; ++i) {
            if (tokensCache.new Iterator(i).getType() == MarkdownTokenTypes.BLOCK_QUOTE) {
                if (lastStart < i) {
                    result.add(TextRange.create(lastStart, i));
                }
                lastStart = i + 1;
            }
        }
        if (lastStart < R) {
            result.add(TextRange.create(lastStart, R));
        }
        return result;
    }

}
