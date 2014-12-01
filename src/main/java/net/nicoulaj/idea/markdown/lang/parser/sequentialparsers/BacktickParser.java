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
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.ParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.SequentialParser;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BacktickParser implements SequentialParser {
    @Override public Collection<Node> parse(@NotNull TokensCache tokens,
                                            @NotNull Collection<TextRange> rangesToGlue) {
        Collection<Node> result = ContainerUtil.newArrayList();

        List<Integer> indices = ParserUtil.textRangesToIndices(rangesToGlue);
        List<Integer> delegateIndices = ContainerUtil.newArrayList();

        for (int i = 0; i < indices.size(); ++i) {
            int index = indices.get(i);
            TokensCache.Iterator iterator = tokens.getIterator(index);
            if (iterator.getType() == MarkdownTokenTypes.BACKTICK || iterator.getType() ==
                                                                     MarkdownTokenTypes.ESCAPED_BACKTICKS) {

                int j = findOfSize(tokens, indices, i + 1, getLength(iterator, true));

                if (j != -1) {
                    result.add(new Node(TextRange.create(index, indices.get(j) + 1), MarkdownElementTypes.CODE_SPAN));
                    i = j;
                    continue;
                }
            }
            delegateIndices.add(index);
        }

        if (!delegateIndices.isEmpty()) {
            result.addAll(new EmphStrongParser().parse(tokens, ParserUtil.indicesToTextRanges(delegateIndices)));
        }

        return result;
    }

    private int findOfSize(@NotNull TokensCache tokens, @NotNull List<Integer> indices, int from, int length) {
        for (int i = from; i < indices.size(); ++i) {
            int index = indices.get(i);
            TokensCache.Iterator iterator = tokens.getIterator(index);
            if (iterator.getType() != MarkdownTokenTypes.BACKTICK && iterator.getType() !=
                                                                     MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                continue;
            }

            if (getLength(iterator, false) == length) {
                return i;
            }
        }
        return -1;
    }


    private int getLength(@NotNull TokensCache.Iterator info, boolean canEscape) {
        final String tokenText = info.getText();

        int toSubtract = 0;
        if (info.getType() == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
            if (canEscape) {
                toSubtract = 2;
            } else {
                toSubtract = 1;
            }
        }

        return tokenText.length() - toSubtract;
    }
}
