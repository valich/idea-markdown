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

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownConstraints {
    public static final MarkdownConstraints BASE = new MarkdownConstraints(new int[0], new char[0], new boolean[0]);

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
            if (types[i] != '>' && isExplicit[i]) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static MarkdownConstraints fromBase(@NotNull PsiBuilder builder, int rawIndex, @NotNull MarkdownConstraints prevLineConstraints) {
        final int myStartOffset = builder.rawTokenTypeStart(rawIndex);

        MarkdownConstraints result = BASE;
        boolean isAlignedWithPrev = true;

        for (int offset = rawIndex;; offset++) {
            final IElementType type = builder.rawLookup(offset);
            if (type != TokenType.WHITE_SPACE
                && !isConstraintType(type)) {
                break;
            }

            // We could jump into code block while scanning "blockquotes", for example.
            if (builder.rawTokenTypeStart(offset) - myStartOffset >= result.getIndent() + 4) {
                break;
            }

            assert type != null;
            if (type == TokenType.WHITE_SPACE) {
                if (isAlignedWithPrev) {
                    result = result.fillImplicitsOnWhiteSpace(builder, offset, myStartOffset, prevLineConstraints);
                }
                // Here we hope that two whitespace tokens would not appear so we would not update isAlignedWithPrev
            } else {
                final MarkdownConstraints newConstraints = result.addModifier(type, builder, offset);
                isAlignedWithPrev = prevLineConstraints.startsWith(newConstraints);

                result = newConstraints;
            }
        }

        return result;
    }

    @NotNull
    public MarkdownConstraints fillImplicitsOnWhiteSpace(@NotNull PsiBuilder builder,
                                                          int rawIndex,
                                                          int startOffset,
                                                          @NotNull MarkdownConstraints prevLineConstraints) {
        MarkdownConstraints result = this;

        int n = indents.length;
        int m = prevLineConstraints.indents.length;
        for (int i = n; i < m; ++i) {
            if (prevLineConstraints.types[i] == '>') {
                break;
            }

            if (prevLineConstraints.indents[i] + 4 <= builder.rawTokenTypeStart(rawIndex + 1) - startOffset) {
                result = new MarkdownConstraints(result, prevLineConstraints.indents[i], prevLineConstraints.types[i], false);
            }
            else {
                break;
            }
        }

        return result;
    }

    @NotNull
    public MarkdownConstraints addModifierIfNeeded(@Nullable IElementType type, @NotNull PsiBuilder builder) {
        MarkdownConstraints result = this;
        if (isConstraintType(type)) {
            result = result.addModifier(type, builder, 0);
        }
        return result;
    }

    @NotNull
    public MarkdownConstraints addModifier(@NotNull IElementType type, @NotNull PsiBuilder builder, int rawOffset) {
        char modifierChar = getModifierCharAtRawIndex(builder, rawOffset);

        if (type == MarkdownTokenTypes.LIST_BULLET
                || type == MarkdownTokenTypes.LIST_NUMBER) {
            int indentAddition = calcIndentAdditionForList(builder, rawOffset);
            return new MarkdownConstraints(this, getIndent() + indentAddition, modifierChar, true);
        }
        else if (type == MarkdownTokenTypes.BLOCK_QUOTE) {
            return new MarkdownConstraints(this, getIndent() + 2, modifierChar, true);
        }

        throw new IllegalArgumentException("modifier must be either a list marker or a blockquote marker");
    }

    private static char getModifierCharAtRawIndex(@NotNull PsiBuilder builder, int index) {
        final IElementType type = builder.rawLookup(index);
        if (type == MarkdownTokenTypes.BLOCK_QUOTE) {
            return '>';
        }
        if (type == MarkdownTokenTypes.LIST_NUMBER) {
            return builder.getOriginalText().charAt(builder.rawTokenTypeStart(index + 1) - 1);
        }
        if (type == MarkdownTokenTypes.LIST_BULLET) {
            return builder.getOriginalText().charAt(builder.rawTokenTypeStart(index));
        }

        throw new IllegalArgumentException("modifier must be either a list marker or a blockquote marker");
    }

    // overridable
    protected int calcIndentAdditionForList(@NotNull PsiBuilder builder, int rawOffset) {
        int lookAhead = 2;
        if (builder.rawLookup(1 + rawOffset) != TokenType.WHITE_SPACE) {
            lookAhead = 1;
        }
        return builder.rawTokenTypeStart(lookAhead + rawOffset) - builder.rawTokenTypeStart(rawOffset);
    }

    @Override public String toString() {
        return "MdConstraints: " + String.valueOf(types) + "(" + getIndent() + ")";
    }

}
