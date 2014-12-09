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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks.impl;

import com.intellij.openapi.util.TextRange;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.InlineStructureHoldingMarkerBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class AtxHeaderMarkerBlock extends InlineStructureHoldingMarkerBlock {
    @NotNull
    private final IElementType myNodeType;

    private final int startPosition;

    public AtxHeaderMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                @NotNull TokensCache tokensCache,
                                @NotNull ProductionHolder productionHolder,
                                int headerSize) {
        super(myConstraints, tokensCache, productionHolder, MarkdownTokenTypes.EOL);

        myNodeType = calcNodeType(headerSize);
        startPosition = productionHolder.getCurrentPosition();
    }

    private static IElementType calcNodeType(int headerSize) {
        switch (headerSize) {
        case 1:
            return MarkdownElementTypes.ATX_1;
        case 2:
            return MarkdownElementTypes.ATX_2;
        case 3:
            return MarkdownElementTypes.ATX_3;
        case 4:
            return MarkdownElementTypes.ATX_4;
        case 5:
            return MarkdownElementTypes.ATX_5;
        case 6:
            return MarkdownElementTypes.ATX_6;
        default:
            return MarkdownElementTypes.ATX_6;
        }
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator iterator, @NotNull MarkdownConstraints currentConstraints) {
        return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return myNodeType;
    }

    @NotNull @Override public Collection<TextRange> getRangesContainingInlineStructure() {
        final int endPosition = productionHolder.getCurrentPosition();
        return Collections.singletonList(TextRange.create(startPosition + 1, endPosition));
    }
}
