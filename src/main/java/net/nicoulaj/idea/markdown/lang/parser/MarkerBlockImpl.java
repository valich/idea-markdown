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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MarkerBlockImpl implements MarkerBlock {
    protected static final Logger LOG = Logger.getInstance(MarkerBlockImpl.class);

    @NotNull
    private final PsiBuilder.Marker marker;

    @Nullable
    private final TokenSet interestingTypes;

    @NotNull
    protected final MarkdownConstraints myConstraints;

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder.Marker marker, @Nullable TokenSet interestingTypes) {
        this.myConstraints = myConstraints;
        this.marker = marker;
        this.interestingTypes = interestingTypes;
    }

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder.Marker marker, @NotNull IElementType interestingType) {
        this(myConstraints, marker, TokenSet.create(interestingType));
    }

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder.Marker marker) {
        this(myConstraints, marker, (TokenSet)null);
    }

    @NotNull public PsiBuilder.Marker getMarker() {
        return marker;
    }

    @NotNull @Override public MarkdownConstraints getBlockConstraints() {
        return myConstraints;
    }

    @NotNull @Override public ProcessingResult processToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints) {
        if (interestingTypes != null && !interestingTypes.contains(tokenType)) {
            return ProcessingResult.PASS;
        }
        return doProcessToken(tokenType, builder, currentConstraints);
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action == ClosingAction.NOTHING) {
            return false;
        }

        if (action == ClosingAction.DONE) {
            getMarker().done(getDefaultNodeType());
        }
        else if (action == ClosingAction.DROP) {
            getMarker().drop();
        }
        return true;
    }

    @NotNull
    protected abstract ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints);

    @NotNull
    public abstract IElementType getDefaultNodeType();
}
