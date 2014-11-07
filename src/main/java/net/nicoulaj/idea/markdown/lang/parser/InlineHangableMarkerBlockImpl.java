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
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InlineHangableMarkerBlockImpl extends MarkerBlockImpl {

    @NotNull
    private final PsiBuilder builder;

    @Nullable
    private PsiBuilder.Marker myPositionMarker = null;

    public InlineHangableMarkerBlockImpl(@NotNull MarkdownConstraints myConstraints,
                                         @NotNull PsiBuilder builder,
                                         @Nullable TokenSet interestingTypes) {
        super(myConstraints, builder.mark(), interestingTypes);
        this.builder = builder;
    }

    public InlineHangableMarkerBlockImpl(@NotNull MarkdownConstraints myConstraints,
                                         @NotNull PsiBuilder builder,
                                         @NotNull IElementType interestingType) {
        super(myConstraints, builder.mark(), interestingType);
        this.builder = builder;
    }

    public InlineHangableMarkerBlockImpl(@NotNull MarkdownConstraints myConstraints,
                                         @NotNull PsiBuilder builder) {
        super(myConstraints, builder.mark());
        this.builder = builder;
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action == ClosingAction.DONE) {
            myPositionMarker = builder.mark();
            return false;
        }

        return super.acceptAction(action);
    }
}
