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

import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.ParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.SequentialParser;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class EmphStrongParser implements SequentialParser {

    protected static final char ITALIC = '_';

    protected static final char BOLD = '*';

    @Override public Collection<Node> parse(@NotNull TokensCache tokens,
                                            @NotNull Collection<TextRange> rangesToGlue) {
        Collection<Node> result = ContainerUtil.newArrayList();

        List<Integer> indices = ParserUtil.textRangesToIndices(rangesToGlue);
        List<Integer> delegateIndices = ContainerUtil.newArrayList();

        char myType = 0;
        Stack<Couple<Integer>> openingOnes = ContainerUtil.newStack();

        for (int i = 0; i < indices.size(); ++i) {
            int index = indices.get(i);

            TokensCache.Iterator iterator = tokens.getIterator(index);
            if (iterator.getType() != MarkdownTokenTypes.EMPH) {
                continue;
            }

            int numCanEnd = canEndNumber(iterator);
            if (numCanEnd != 0 && myType == getType(iterator) && !openingOnes.isEmpty()) {
                while (numCanEnd > 0 && !openingOnes.isEmpty()) {
                    final Couple<Integer> lastOpening = openingOnes.pop();
                    final int toMakeMax = Math.min(lastOpening.second, numCanEnd);
                    final int toMake = toMakeMax % 2 == 0 ? 2 : 1;
                    final int from = lastOpening.first + (lastOpening.second - toMake);
                    final int to = i + toMake - 1;

                    result.add(new Node(TextRange.create(indices.get(from), indices.get(to) + 1),
                                        toMake == 2 ? MarkdownElementTypes.STRONG : MarkdownElementTypes.EMPH));

                    i += toMake;
                    numCanEnd -= toMake;
                    if (lastOpening.second > toMake) {
                        openingOnes.push(Couple.of(lastOpening.first, lastOpening.second - toMake));
                    }
                }
                i--;
                continue;
            }

            int numCanStart = canStartNumber(iterator);
            if (numCanStart != 0) {
                if (myType == 0) {
                    myType = getType(iterator);
                } else if (myType != getType(iterator)) {
                    continue;
                }

                openingOnes.push(Couple.of(i, numCanStart));
                i += numCanStart;
            }
        }

        if (!delegateIndices.isEmpty()) {
            result.addAll(parse(tokens, ParserUtil.indicesToTextRanges(delegateIndices)));
        }

        return result;
    }

    private int findNotEmph(TokensCache tokens, List<Integer> indices, int from) {
        for (int i = from; i < indices.size(); ++i) {
            int index = indices.get(i);
            if (tokens.getIterator(index).getType() != MarkdownTokenTypes.EMPH) {
                return i;
            }
        }
        return indices.size();
    }

    private int findClosing(TokensCache tokens, List<Integer> indices, int from, int num, char type) {
        for (int i = from; i < indices.size(); ++i) {
            int index = indices.get(i);

            TokensCache.Iterator iterator = tokens.getIterator(index);
            if (iterator.getType() != MarkdownTokenTypes.EMPH) {
                continue;
            }

            if (getType(iterator) != type) {
                continue;
            }

            int endNumber = canEndNumber(iterator);
            if (endNumber == 2 && (i + 1 == indices.size() || indices.get(i + 1) != index + 1)) {
                endNumber = 1;
            }

            if (endNumber >= num) {
                while (i + 2 < indices.size() &&
                       indices.get(i + 2) == index + 2 &&
                       canEndNumber(tokens.getIterator(index + 1)) == 2) {
                    i++;
                    index++;
                }

                return i;
            }
        }

        return -1;
    }

    private int canStartNumber(@NotNull TokensCache.Iterator info) {
        if (getType(info) == ITALIC && info.rawLookup(-1) != null) {
            String text = info.rollback().getText();
            if (Character.isLetterOrDigit(text.charAt(text.length() - 1))) {
                return 0;
            }
        }

        for (int i = 0; i < 50; ++i) {
            if (isWhitespace(info, 1)) {
                return 0;
            }
            if (info.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(info) != getType(info.advance())) {
                return i + 1;
            }
            info = info.advance();
        }

        return 50;
    }

    private int canEndNumber(@NotNull TokensCache.Iterator info) {
        if (isWhitespace(info, -1)) {
            return 0;
        }

        for (int i = 0; i < 50; ++i) {
            if (info.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(info) != getType(info.advance())) {
                if (getType(info) == ITALIC && info.rawLookup(1) != null && Character.isLetterOrDigit(info.advance().getText().charAt(0))) {
                    return 0;
                }
                return i + 1;
            }
            info = info.advance();
        }

        return 50;
    }

    private boolean isWhitespace(TokensCache.Iterator info, int lookup) {
        IElementType type = info.rawLookup(lookup);
        if (type == null) {
            return false;
        }
        if (type == MarkdownTokenTypes.EOL || type == TokenType.WHITE_SPACE) {
            return true;
        }
        if (lookup == -1) {
            return StringUtil.endsWithChar(info.rollback().getText(), ' ');
        }
        else {
            return StringUtil.startsWithChar(info.advance().getText(), ' ');
        }
    }

    private char getType(@NotNull TokensCache.Iterator info) {
        return info.getText().charAt(0);
    }
}
