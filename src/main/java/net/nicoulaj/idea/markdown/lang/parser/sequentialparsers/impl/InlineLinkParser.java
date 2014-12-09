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
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParser;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class InlineLinkParser implements SequentialParser {
    @Override public ParsingResult parse(@NotNull TokensCache tokens,
                                         @NotNull Collection<TextRange> rangesToGlue) {
        ParsingResult result = new ParsingResult();
        final List<Integer> delegateIndices = ContainerUtil.newArrayList();
        final List<Integer> indices = SequentialParserUtil.textRangesToIndices(rangesToGlue);

        TokensCache.Iterator iterator = tokens.new ListIterator(indices, 0);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                final List<Integer> localDelegates = ContainerUtil.newArrayList();
                final Collection<Node> resultNodes = ContainerUtil.newArrayList();
                TokensCache.Iterator afterLink = parseInlineLink(resultNodes, localDelegates, iterator);
                if (afterLink != null) {
                    iterator = afterLink.advance();
                    result = result.withNodes(resultNodes)
                                   .withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(localDelegates));
                    continue;
                }
            }

            delegateIndices.add(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices));
    }

    @Nullable
    private TokensCache.Iterator parseInlineLink(@NotNull Collection<Node> result,
                                                 @NotNull List<Integer> delegateIndices,
                                                 @Nullable TokensCache.Iterator iterator) {
        if (iterator == null) {
            return null;
        }

        final int startIndex = iterator.getIndex();

        if ((iterator = LinkParserUtil.parseLinkText(result, delegateIndices, iterator)) == null) {
            return null;
        }
        if (iterator.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
            return null;
        }

        iterator = iterator.advance().advance();
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            iterator = iterator.advance();
        }
        TokensCache.Iterator afterDestination = LinkParserUtil.parseLinkDestination(result, iterator);
        if (afterDestination != null) {
            iterator = afterDestination.advance();
            if (iterator.getType() == MarkdownTokenTypes.EOL) {
                iterator = iterator.advance();
            }
        }
        TokensCache.Iterator afterTitle = LinkParserUtil.parseLinkTitle(result, iterator);
        if (afterTitle != null) {
            iterator = afterTitle.advance();
            if (iterator.getType() == MarkdownTokenTypes.EOL) {
                iterator = iterator.advance();
            }
        }
        if (iterator.getType() != MarkdownTokenTypes.RPAREN) {
            return null;
        }

        result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.INLINE_LINK));
        return iterator;
    }
}
