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
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TokensCache {
    @NotNull
    private final List<TokenInfo> cachedTokens;
    @NotNull
    private final List<TokenInfo> filteredTokens;
    @NotNull
    private final PsiBuilder builder;

    public TokensCache(@NotNull PsiBuilder builder) {
        this.builder = builder;
        cachedTokens = ContainerUtil.newArrayList();
        filteredTokens = ContainerUtil.newArrayList();

        cacheTokens();
        verify();
    }

    public int calcCurrentBuilderPosition(@NotNull PsiBuilder builder) {
        if (builder.eof()) {
            return filteredTokens.size();
        }

        int currentOffset = builder.getCurrentOffset();
        for (TokenInfo filteredToken : filteredTokens) {
            if (filteredToken.tokenStart == currentOffset) {
                return filteredToken.normIndex;
            }
        }
        throw new IllegalStateException("could not be here");
    }

    public char getRawCharAt(int index) {
        if (index < 0) return 0;
        CharSequence originalText = builder.getOriginalText();
        if (index >= originalText.length()) return 0;
        return originalText.charAt(index);
    }

    private void cacheTokens() {
        PsiBuilder.Marker startMarker = builder.mark();

        for (int i = 0; builder.rawLookup(i) != null; ++i) {
            cachedTokens.add(new TokenInfo(builder.rawLookup(i),
                                           builder.rawTokenTypeStart(i),
                                           builder.rawTokenTypeStart(i + 1),
                                           i,
                                           -1));
        }

        int listIndex = 0;
        int builderIndex = 0;
        while (builder.getTokenType() != null) {
            while (builder.getCurrentOffset() > cachedTokens.get(listIndex).tokenStart) {
                listIndex++;
            }
            assert  builder.getCurrentOffset() == cachedTokens.get(listIndex).tokenStart;
            cachedTokens.get(listIndex).setNormIndex(builderIndex);
            filteredTokens.add(cachedTokens.get(listIndex));

            builder.advanceLexer();
            builderIndex++;
        }

        startMarker.rollbackTo();
    }

    private void verify() {
        for (int i = 0; i < cachedTokens.size(); ++i) {
            assert cachedTokens.get(i).rawIndex == i;
        }
        for (int i = 0; i < filteredTokens.size(); ++i) {
            assert filteredTokens.get(i).normIndex == i;
        }
    }

    private int getIndexForIterator(@NotNull List<Integer> indices, int startIndex) {
        if (startIndex < 0) {
            return -1;
        }
        if (startIndex >= indices.size()) {
            return filteredTokens.size();
        }
        return indices.get(startIndex);
    }


    public class ListIterator extends Iterator {
        @NotNull
        private List<Integer> indices;

        private int listIndex;

        public ListIterator(@NotNull List<Integer> indices, int startIndex) {
            super(getIndexForIterator(indices, startIndex));
            this.indices = indices;
            listIndex = startIndex;
        }

        @Override public Iterator advance() {
            return new ListIterator(indices, listIndex + 1);
        }

        @Override public Iterator rollback() {
            return new ListIterator(indices, listIndex - 1);
        }
    }

    public class Iterator {
        final int index;

        public Iterator(int startIndex) {
            index = startIndex;
        }

        public int getIndex() {
            return index;
        }

        public Iterator advance() {
            return new Iterator(index + 1);
        }

        public Iterator rollback() {
            return new Iterator(index - 1);
        }

        @NotNull
        private TokenInfo info(int rawSteps) {
            if (index < 0) {
                return new TokenInfo(null, 0, 0, 0, 0);
            }
            else if (index >= filteredTokens.size()) {
                return new TokenInfo(null, builder.getOriginalText().length(), 0, 0, 0);
            }

            final int rawIndex = filteredTokens.get(index).rawIndex + rawSteps;
            if (rawIndex < 0) {
                return new TokenInfo(null, 0, 0, 0, 0);
            }
            else if (rawIndex >= cachedTokens.size()) {
                return new TokenInfo(null, builder.getOriginalText().length(), 0, 0, 0);
            }

            return cachedTokens.get(rawIndex);
        }

        @Nullable
        public IElementType getType() {
            return info(0).type;
        }

        @NotNull
        public String getText() {
            return builder.getOriginalText().subSequence(info(0).tokenStart, info(0).tokenEnd).toString();
        }

        public int getStart() {
            return info(0).tokenStart;
        }

        public int getEnd() {
            return info(0).tokenEnd;
        }

        public IElementType rawLookup(int steps) {
            return info(steps).type;
        }

    }

    private static class TokenInfo {
        private final IElementType type;
        private final int tokenStart;
        private final int tokenEnd;
        private final int rawIndex;
        private int normIndex;

        public TokenInfo(IElementType type, int tokenStart, int tokenEnd, int rawIndex, int normIndex) {
            this.type = type;
            this.tokenStart = tokenStart;
            this.tokenEnd = tokenEnd;
            this.rawIndex = rawIndex;
            this.normIndex = normIndex;
        }

        public void setNormIndex(int i) {
            normIndex = i;
        }
    }
}
