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

public abstract class InlineHangableMarkerBlock extends MarkerBlockImpl {

    @NotNull
    protected final static ProcessingResult DROP_ACTION =
            new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DROP, EventAction.PROPAGATE);
    @NotNull
    protected final static ProcessingResult DONE_ACTION =
            new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DONE, EventAction.PROPAGATE);
    @NotNull
    protected final InlineMarkerManager markerManager;
    @NotNull
    private State state = State.PARSING;

    public InlineHangableMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                     @NotNull PsiBuilder.Marker marker,
                                     @Nullable TokenSet interestingTypes, @NotNull InlineMarkerManager markerManager) {
        super(myConstraints, marker, interestingTypes);
        this.markerManager = markerManager;
        markerManager.openMarkerBlock(this);
    }

    public InlineHangableMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                     @NotNull PsiBuilder.Marker marker,
                                     @NotNull IElementType interestingType,
                                     @NotNull InlineMarkerManager markerManager) {
        super(myConstraints, marker, interestingType);
        this.markerManager = markerManager;
        markerManager.openMarkerBlock(this);
    }

    public InlineHangableMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                     @NotNull PsiBuilder.Marker marker,
                                     @NotNull InlineMarkerManager markerManager) {
        super(myConstraints, marker);
        this.markerManager = markerManager;
        markerManager.openMarkerBlock(this);
    }

    @NotNull public State getState() {
        return state;
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action == ClosingAction.DONE
            || action == ClosingAction.DEFAULT && getDefaultAction() == ClosingAction.DONE) {
            markerManager.doneMarker(this);
            state = State.DONE;
        }
        else if (action != ClosingAction.NOTHING) {
            markerManager.cancelMarker(this);
            state = State.DROPPED;
        }

        return action != ClosingAction.NOTHING;
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DROP;
    }

    public static enum State {
        PARSING,
        DROPPED,
        DONE
    }
}
