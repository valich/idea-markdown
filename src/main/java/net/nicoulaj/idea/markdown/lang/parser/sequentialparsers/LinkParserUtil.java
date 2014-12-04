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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class LinkParserUtil {
    @Nullable
    static TokensCache.Iterator parseLinkDestination(@NotNull Collection<SequentialParser.Node> result,
                                                     @NotNull TokensCache.Iterator iterator) {
        if (iterator.getType() == MarkdownTokenTypes.EOL || iterator.getType() == MarkdownTokenTypes.RPAREN) {
            return null;
        }

        final int startIndex = iterator.getIndex();
        final boolean withBraces = iterator.getType() == MarkdownTokenTypes.LT;
        if (withBraces) {
            iterator = iterator.advance();
        }

        boolean hasOpenedParentheses = false;
        while (iterator.getType() != null) {
            if (withBraces && iterator.getType() == MarkdownTokenTypes.GT) {
                break;
            }
            else if (!withBraces) {
                if (iterator.getType() == MarkdownTokenTypes.LPAREN) {
                    if (hasOpenedParentheses) {
                        break;
                    }
                    hasOpenedParentheses = true;
                }

                final IElementType next = iterator.rawLookup(1);
                if (ParserUtil.isWhitespace(iterator, 1) || next == null) {
                    break;
                }
                else if (next == MarkdownTokenTypes.RPAREN) {
                    if (!hasOpenedParentheses) {
                        break;
                    }
                    hasOpenedParentheses = false;
                }
            }

            iterator = iterator.advance();
        }

        if (iterator.getType() != null && !hasOpenedParentheses) {
            result.add(new SequentialParser.Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_DESTINATION));
            return iterator;
        }
        return null;
    }

    @Nullable
    static TokensCache.Iterator parseLinkLabel(@NotNull Collection<SequentialParser.Node> result,
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
            int endIndex = iterator.getIndex();
            if (endIndex == startIndex + 1) {
                return null;
            }

            result.add(new SequentialParser.Node(TextRange.create(startIndex, endIndex + 1), MarkdownElementTypes.LINK_LABEL));
            delegateIndices.addAll(indicesToDelegate);
            return iterator;
        }
        return null;
    }

    @Nullable
    static TokensCache.Iterator parseLinkText(@NotNull Collection<SequentialParser.Node> result,
                                               @NotNull List<Integer> delegateIndices,
                                               @NotNull TokensCache.Iterator iterator) {

        if (iterator.getType() != MarkdownTokenTypes.LBRACKET) {
            return null;
        }

        final int startIndex = iterator.getIndex();
        final List<Integer> indicesToDelegate = ContainerUtil.newArrayList();

        int bracketDepth = 1;

        iterator = iterator.advance();
        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.RBRACKET) {
                if (--bracketDepth == 0) {
                    break;
                }
            }

            indicesToDelegate.add(iterator.getIndex());
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                bracketDepth++;
            }
            iterator = iterator.advance();
        }

        if (iterator.getType() == MarkdownTokenTypes.RBRACKET) {
            result.add(new SequentialParser.Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_TEXT));
            delegateIndices.addAll(indicesToDelegate);
            return iterator;
        }
        return null;
    }

    @Nullable
    static TokensCache.Iterator parseLinkTitle(Collection<SequentialParser.Node> result, TokensCache.Iterator iterator) {
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
            closingType = MarkdownTokenTypes.RPAREN;
        }
        else {
            return null;
        }

        iterator = iterator.advance();
        while (iterator.getType() != null && iterator.getType() != closingType) {
            iterator = iterator.advance();
        }

        if (iterator.getType() != null) {
            result.add(new SequentialParser.Node(TextRange.create(startIndex, iterator.getIndex() + 1), MarkdownElementTypes.LINK_TITLE));
            return iterator;
        }
        return null;
    }
}
