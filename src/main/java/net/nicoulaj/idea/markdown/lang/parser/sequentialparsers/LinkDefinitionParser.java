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
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.ParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.SequentialParser;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class LinkDefinitionParser implements SequentialParser {
    @Override public ParsingResult parse(@NotNull TokensCache tokens,
                                            @NotNull Collection<TextRange> rangesToGlue) {
        Collection<Node> resultNodes = ContainerUtil.newArrayList();
        List<Integer> delegateIndices = ContainerUtil.newArrayList();
        List<Integer> indices = ParserUtil.textRangesToIndices(rangesToGlue);

        TokensCache.Iterator iterator = tokens.new ListIterator(indices, 0);

        if (parseLinkDefinition(resultNodes, delegateIndices, iterator) != null) {
            return new ParsingResult().withNodes(resultNodes)
                                      .withFurtherProcessing(ParserUtil.indicesToTextRanges(delegateIndices));
        }

        return new ParsingResult().withFurtherProcessing(rangesToGlue);
    }

    private TokensCache.Iterator parseLinkDefinition(Collection<Node> result,
                                        List<Integer> delegateIndices,
                                        TokensCache.Iterator iterator) {
        final int startIndex = iterator.getIndex();


        if ((iterator = parseLinkLabel(result, delegateIndices, iterator)) == null) {
            return null;
        }
        if (iterator.rawLookup(1) != MarkdownTokenTypes.COLON) {
            return null;
        }
        iterator = iterator.advance().advance();
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            iterator = iterator.advance();
        }
        if ((iterator = parseLinkDestination(result, iterator)) == null) {
            return null;
        }
        iterator = iterator.advance();
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            iterator = iterator.advance();
        }
        if ((iterator = parseLinkTitle(result, iterator)) == null) {
            return null;
        }

        IElementType nextType = iterator.advance().getType();
        if (nextType != null && nextType != MarkdownTokenTypes.EOL) {
            return null;
        }

        result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_DEFINITION));
        return iterator;
    }

    private TokensCache.Iterator parseLinkTitle(Collection<Node> result, TokensCache.Iterator iterator) {
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            return null;
        }

        final int startIndex = iterator.getIndex();
        final IElementType closingType;

        if (iterator.getType() == MarkdownTokenTypes.SINGLE_QUOTE
                || iterator.getType() == MarkdownTokenTypes.DOUBLE_QUOTE) {
            closingType = iterator.getType();
        }
        else if (iterator.getType() == MarkdownTokenTypes.LPAREN) {
            closingType = iterator.getType();
        }
        else {
            return null;
        }

        iterator = iterator.advance();
        while (iterator.getType() != null && iterator.getType() != closingType) {
            iterator = iterator.advance();
        }

        if (iterator.getType() != null) {
            result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_TITLE));
            return iterator;
        }
        return null;
    }

    private TokensCache.Iterator parseLinkLabel(@NotNull Collection<Node> result,
                                   @NotNull List<Integer> delegateIndices,
                                   @NotNull TokensCache.Iterator iterator) {

        if (iterator.getType() != MarkdownTokenTypes.LBRACKET) {
            return null;
        }

        final int startIndex = iterator.getIndex();

        List<Integer> indicesToDelegate = ContainerUtil.newArrayList();

        iterator = iterator.advance();
        while (iterator.getType() != MarkdownTokenTypes.RBRACKET && iterator.getType() != null) {
            indicesToDelegate.add(iterator.getIndex());
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                break;
            }
            iterator = iterator.advance();
        }

        if (iterator.getType() == MarkdownTokenTypes.RBRACKET) {
            result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_LABEL));
            delegateIndices.addAll(indicesToDelegate);
            return iterator;
        }
        return null;
    }

    private TokensCache.Iterator parseLinkDestination(@NotNull Collection<Node> result,
                                                      @NotNull TokensCache.Iterator iterator) {
        if (iterator.getType() == MarkdownTokenTypes.EOL) {
            return null;
        }

        final int startIndex = iterator.getIndex();
        final boolean withBraces = iterator.getType() == MarkdownTokenTypes.LT;
        if (withBraces) {
            iterator = iterator.advance();
        }

        iterator.advance();
        while (iterator.getType() != null &&
               (withBraces && iterator.getType() != MarkdownTokenTypes.GT
                || !withBraces && !ParserUtil.isWhitespace(iterator, 1))) {
            iterator = iterator.advance();
        }

        if (iterator.getType() != null) {
            result.add(new Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_DESTINATION));
            return iterator;
        }
        return null;
    }
}
