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

import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownConstraints {
    public static final MarkdownConstraints BASE = new MarkdownConstraints(new int[0], new char[0], new boolean[0]);

    public static final char BQ_CHAR = '>';

    private int[] indents;
    private char[] types;
    private boolean[] isExplicit;

    private MarkdownConstraints(MarkdownConstraints parent, int newIndent, char newType, boolean newExplicit) {
        int n = parent.indents.length;
        indents = new int[n + 1];
        types = new char[n + 1];
        isExplicit = new boolean[n + 1];
        System.arraycopy(parent.indents, 0, indents, 0, n);
        System.arraycopy(parent.types, 0, types, 0, n);
        System.arraycopy(parent.isExplicit, 0, isExplicit, 0, n);

        indents[n] = newIndent;
        types[n] = newType;
        isExplicit[n] = newExplicit;
    }

    private MarkdownConstraints(int[] indents, char[] types, boolean[] isExplicit) {
        this.indents = indents;
        this.types = types;
        this.isExplicit = isExplicit;
    }

    public int getIndent() {
        if (indents.length == 0) {
            return 0;
        }

        return indents[indents.length - 1];
    }

    @Contract("null -> false")
    public static boolean isConstraintType(@Nullable IElementType type) {
        return type == MarkdownTokenTypes.LIST_NUMBER
               || type == MarkdownTokenTypes.LIST_BULLET
               || type == MarkdownTokenTypes.BLOCK_QUOTE;
    }

    public boolean upstreamWith(@NotNull MarkdownConstraints other) {
        return other.startsWith(this) && !containsListMarkers();
    }

    public boolean extendsPrev(@NotNull MarkdownConstraints other) {
        return startsWith(other) && !containsListMarkers(other.types.length);
    }

    public boolean extendsList(@NotNull MarkdownConstraints other) {
        if (other.types.length == 0) {
            throw new IllegalArgumentException("List constraints should contain at least one item");
        }
        return startsWith(other) && !containsListMarkers(other.types.length - 1);
    }

    private boolean startsWith(@NotNull MarkdownConstraints other) {
        int n = indents.length;
        int m = other.indents.length;

        if (n < m) {
            return false;
        }
        for (int i = 0; i < m; ++i) {
            if (types[i] != other.types[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean containsListMarkers() {
        return containsListMarkers(types.length);
    }

    private boolean containsListMarkers(int upToIndex) {
        for (int i = 0; i < upToIndex; ++i) {
            if (types[i] != BQ_CHAR && isExplicit[i]) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static MarkdownConstraints fromBase(@NotNull TokensCache.Iterator iterator, int rawIndex, @NotNull MarkdownConstraints prevLineConstraints) {
        final int myStartOffset = iterator.rawStart(rawIndex);

        MarkdownConstraints result = BASE;
        boolean isAlignedWithPrev = true;

        for (int offset = rawIndex;; offset++) {
            final IElementType type = iterator.rawLookup(offset);
            if (type != MarkdownTokenTypes.WHITE_SPACE
                && !isConstraintType(type)) {
                break;
            }

            // We could jump into code block while scanning "blockquotes", for example.
            if (iterator.rawStart(offset) - myStartOffset >= result.getIndent() + 4) {
                break;
            }

            assert type != null;
            if (type == MarkdownTokenTypes.WHITE_SPACE) {
                if (isAlignedWithPrev) {
                    result = result.fillImplicitsOnWhiteSpace(iterator, offset, prevLineConstraints);
                }
                // Here we hope that two whitespace tokens would not appear so we would not update isAlignedWithPrev
            } else {
                final MarkdownConstraints newConstraints = result.addModifier(type, iterator, offset);
                isAlignedWithPrev = prevLineConstraints.startsWith(newConstraints);

                result = newConstraints;
            }
        }

        return result;
    }

    @NotNull
    public MarkdownConstraints fillImplicitsOnWhiteSpace(@NotNull TokensCache.Iterator iterator,
                                                          int rawIndex,
                                                          @NotNull MarkdownConstraints prevLineConstraints) {
        MarkdownConstraints result = this;
        final int whitespaceLen = iterator.rawStart(rawIndex + 1) - iterator.rawStart(rawIndex);
        assert whitespaceLen > 0 : "Token of zero length?";

        int eaten = 0;

        int n = indents.length;
        int m = prevLineConstraints.indents.length;

        if (n > 0 && types[n - 1] == BQ_CHAR) {
            eaten++;
        }

        for (int i = n; i < m; ++i) {
            if (prevLineConstraints.types[i] == BQ_CHAR) {
                break;
            }
            // Else it's list marker

            final int indentDelta = prevLineConstraints.indents[i] - (i == 0 ? 0 : prevLineConstraints.indents[i - 1]);
            if (eaten + indentDelta <= whitespaceLen) {
                eaten += indentDelta;
                result = new MarkdownConstraints(result, result.getIndent() + indentDelta, prevLineConstraints.types[i], false);
            }
            else {
                break;
            }
        }

        return result;
    }

    @NotNull
    public MarkdownConstraints addModifierIfNeeded(@Nullable IElementType type, @NotNull TokensCache.Iterator iterator) {
        MarkdownConstraints result = this;
        if (isConstraintType(type)) {
            result = result.addModifier(type, iterator, 0);
        }
        return result;
    }

    @NotNull
    public MarkdownConstraints addModifier(@NotNull IElementType type, @NotNull TokensCache.Iterator iterator, int rawOffset) {
        char modifierChar = getModifierCharAtRawIndex(iterator, rawOffset);

        final int lineStartOffset = calcLineStartOffset(iterator, rawOffset);
        final int currentIndent = getIndent();
        final int markerStartOffset = iterator.rawStart(rawOffset) - lineStartOffset;
        final int whiteSpaceBefore = markerStartOffset - currentIndent;
        assert whiteSpaceBefore == 0 || iterator.rawLookup(rawOffset - 1) == MarkdownTokenTypes.WHITE_SPACE : "If some indent is present, it should have been whitespace";
        assert whiteSpaceBefore < 4 : "Should not add modifier: indent of 4 is a code block";

        if (type == MarkdownTokenTypes.LIST_BULLET
                || type == MarkdownTokenTypes.LIST_NUMBER) {
            int indentAddition = calcIndentAdditionForList(iterator, rawOffset);
            return new MarkdownConstraints(this, currentIndent + whiteSpaceBefore + indentAddition, modifierChar, true);
        }
        else if (type == MarkdownTokenTypes.BLOCK_QUOTE) {
            return new MarkdownConstraints(this, currentIndent + whiteSpaceBefore + 2, modifierChar, true);
        }

        throw new IllegalArgumentException("modifier must be either a list marker or a blockquote marker");
    }

    private static int calcLineStartOffset(@NotNull TokensCache.Iterator iterator, int rawOffset) {
        for (int index = rawOffset - 1;; --index) {
            final IElementType type = iterator.rawLookup(index);
            if (type == null) {
                return 0;
            }
            if (type == MarkdownTokenTypes.EOL) {
                return iterator.rawStart(index + 1);
            }
        }
    }

    private static char getModifierCharAtRawIndex(@NotNull TokensCache.Iterator iterator, int index) {
        final IElementType type = iterator.rawLookup(index);
        if (type == MarkdownTokenTypes.BLOCK_QUOTE) {
            return BQ_CHAR;
        }

        String s = iterator.rawText(index);
        assert s != null;
        if (type == MarkdownTokenTypes.LIST_NUMBER) {
            return s.charAt(s.length() - 1);
        }
        if (type == MarkdownTokenTypes.LIST_BULLET) {
            return s.charAt(0);
        }

        throw new IllegalArgumentException("modifier must be either a list marker or a blockquote marker");
    }

    // overridable
    protected int calcIndentAdditionForList(@NotNull TokensCache.Iterator iterator, int rawOffset) {
        int markerWidth = iterator.rawStart(rawOffset + 1) - iterator.rawStart(rawOffset);

        if (iterator.rawLookup(1 + rawOffset) != MarkdownTokenTypes.WHITE_SPACE) {
            return markerWidth;
        } else {
            final int whitespaceAfterLength = iterator.rawStart(2 + rawOffset) - iterator.rawStart(1 + rawOffset);
            if (whitespaceAfterLength >= 4) {
                return markerWidth + 1;
            } else {
                return markerWidth + whitespaceAfterLength;
            }
        }
    }

    @Override public String toString() {
        return "MdConstraints: " + String.valueOf(types) + "(" + getIndent() + ")";
    }

}
