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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks;

import com.intellij.openapi.diagnostic.Logger;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public abstract class MarkerBlockImpl implements MarkerBlock {
    protected static final Logger LOG = Logger.getInstance(MarkerBlockImpl.class);

    @NotNull
    private final ProductionHolder.Marker marker;

    @Nullable
    private final Set<IElementType> interestingTypes;

    @NotNull
    protected final MarkdownConstraints myConstraints;

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull ProductionHolder.Marker marker, @Nullable Set<IElementType> interestingTypes) {
        this.myConstraints = myConstraints;
        this.marker = marker;
        this.interestingTypes = interestingTypes;
    }

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull ProductionHolder.Marker marker, @NotNull IElementType interestingType) {
        this(myConstraints, marker, Collections.singleton(interestingType));
    }

    public MarkerBlockImpl(@NotNull MarkdownConstraints myConstraints, @NotNull ProductionHolder.Marker marker) {
        this(myConstraints, marker, (Set<IElementType>)null);
    }

    @NotNull public ProductionHolder.Marker getMarker() {
        return marker;
    }

    @NotNull @Override public MarkdownConstraints getBlockConstraints() {
        return myConstraints;
    }

    @NotNull @Override public ProcessingResult processToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator builder, @NotNull MarkdownConstraints currentConstraints) {
        if (interestingTypes != null && !interestingTypes.contains(tokenType)) {
            return ProcessingResult.PASS;
        }
        return doProcessToken(tokenType, builder, currentConstraints);
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action == ClosingAction.DEFAULT) {
            action = getDefaultAction();
        }

        action.doAction(getMarker(), getDefaultNodeType());

        return action != ClosingAction.NOTHING;

    }

    @NotNull
    protected abstract ClosingAction getDefaultAction();

    @NotNull
    protected abstract ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator iterator, @NotNull MarkdownConstraints currentConstraints);

    @NotNull
    public abstract IElementType getDefaultNodeType();
}
