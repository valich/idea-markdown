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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ReferenceLinkParser implements SequentialParser {
    @Override public ParsingResult parse(@NotNull TokensCache tokens,
                                         @NotNull Collection<TextRange> rangesToGlue) {
        ParsingResult result = new ParsingResult();
        final List<Integer> delegateIndices = ContainerUtil.newArrayList();
        final List<Integer> indices = ParserUtil.textRangesToIndices(rangesToGlue);

        TokensCache.Iterator iterator = tokens.new ListIterator(indices, 0);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                final List<Integer> localDelegates = ContainerUtil.newArrayList();
                final Collection<Node> resultNodes = ContainerUtil.newArrayList();
                TokensCache.Iterator afterLink = parseReferenceLink(resultNodes, localDelegates, iterator);
                if (afterLink != null) {
                    iterator = afterLink.advance();
                    result = result.withNodes(resultNodes)
                                   .withFurtherProcessing(ParserUtil.indicesToTextRanges(localDelegates));
                    continue;
                }
            }

            delegateIndices.add(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(ParserUtil.indicesToTextRanges(delegateIndices));
    }

    private TokensCache.Iterator parseReferenceLink(@NotNull Collection<Node> resultNodes,
                                                    @NotNull List<Integer> localDelegates,
                                                    @Nullable TokensCache.Iterator iterator) {
        TokensCache.Iterator result;

        result = parseFullReferenceLink(resultNodes, localDelegates, iterator);
        if (result != null) {
            return result;
        }
        resultNodes.clear();
        localDelegates.clear();
        result = parseShortReferenceLink(resultNodes, localDelegates, iterator);
        if (result != null) {
            return result;
        }
        return null;
    }

    @Nullable
    private TokensCache.Iterator parseFullReferenceLink(@NotNull Collection<Node> result,
                                                        @NotNull List<Integer> delegateIndices,
                                                        @Nullable TokensCache.Iterator iterator) {
        if (iterator == null) {
            return null;
        }

        final int startIndex = iterator.getIndex();

        if ((iterator = LinkParserUtil.parseLinkText(result, delegateIndices, iterator)) == null) {
            return null;
        }
        iterator = iterator.advance();

        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            iterator = iterator.advance();
        }

        if ((iterator = LinkParserUtil.parseLinkLabel(result, delegateIndices, iterator)) == null) {
            return null;
        }

        result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.FULL_REFERENCE_LINK));
        return iterator;
    }

    @Nullable
    private TokensCache.Iterator parseShortReferenceLink(@NotNull Collection<Node> result,
                                                        @NotNull List<Integer> delegateIndices,
                                                        @Nullable TokensCache.Iterator iterator) {
        if (iterator == null) {
            return null;
        }

        final int startIndex = iterator.getIndex();

        if ((iterator = LinkParserUtil.parseLinkLabel(result, delegateIndices, iterator)) == null) {
            return null;
        }

        final TokensCache.Iterator shortcutLinkEnd = iterator;

        iterator = iterator.advance();
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            iterator = iterator.advance();
        }

        if (iterator.getType() == MarkdownTokenTypes.LBRACKET && iterator.rawLookup(1) == MarkdownTokenTypes.RBRACKET) {
            iterator = iterator.advance();
        }
        else {
            iterator = shortcutLinkEnd;
        }

        result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.SHORT_REFERENCE_LINK));
        return iterator;
    }

}
