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

import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkerBlockImpl;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

public class CodeFenceMarkerBlock extends MarkerBlockImpl {
    public CodeFenceMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull ProductionHolder.Marker marker) {
        super(myConstraints, marker);
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull TokensCache.Iterator builder,
                                                                 @NotNull
                                                                 MarkdownConstraints currentConstraints) {
        if (tokenType == MarkdownTokenTypes.CODE_FENCE_END) {
            return new ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.CANCEL)
                    .postpone();
        }
        // Allow top-level blocks to interrupt this one
        if (tokenType == MarkdownTokenTypes.EOL) {
            return ProcessingResult.PASS;
        }

        return ProcessingResult.CANCEL;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_FENCE;
    }
}
