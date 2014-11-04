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

public interface MarkerBlock {

    @NotNull
    ProcessingResult processToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints);

    @NotNull
    MarkdownConstraints getBlockConstraints();

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    boolean acceptAction(@NotNull ClosingAction action);

    enum ClosingAction {
        DONE {
            @Override public void doAction(@NotNull PsiBuilder.Marker marker, @NotNull IElementType type) {
                marker.done(type);
            }
        },
        DROP {
            @Override public void doAction(@NotNull PsiBuilder.Marker marker, @NotNull IElementType type) {
                marker.drop();
            }
        },
        NOTHING {
            @Override public void doAction(@NotNull PsiBuilder.Marker marker, @NotNull IElementType type) {
            }
        };

        public abstract void doAction(@NotNull PsiBuilder.Marker marker, @NotNull IElementType type);
    }

    enum EventAction {
        PROPAGATE,
        CANCEL
    }

    public static class ProcessingResult {
        public static final ProcessingResult PASS = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE);
        public static final ProcessingResult CANCEL = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL);

        public final ClosingAction childrenAction;
        public final ClosingAction selfAction;
        public final EventAction eventAction;

        public ProcessingResult(@NotNull ClosingAction childrenAction, @NotNull ClosingAction selfAction, @NotNull EventAction eventAction) {
            this.childrenAction = childrenAction;
            this.selfAction = selfAction;
            this.eventAction = eventAction;
        }
    }

}
