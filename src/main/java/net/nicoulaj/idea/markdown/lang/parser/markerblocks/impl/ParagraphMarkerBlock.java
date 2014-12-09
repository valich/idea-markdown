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
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkdownParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ParagraphMarkerBlock extends InlineStructureHoldingMarkerBlock {
    private final int startPosition;

    public ParagraphMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                @NotNull ProductionHolder productionHolder,
                                @NotNull TokensCache tokensCache) {
        super(myConstraints, tokensCache, productionHolder, MarkdownTokenTypes.EOL);
        startPosition = productionHolder.getCurrentPosition();
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator builder, @NotNull MarkdownConstraints currentConstraints) {
        LOG.assertTrue(tokenType == MarkdownTokenTypes.EOL);

        if (MarkdownParserUtil.calcNumberOfConsequentEols(builder) >= 2) {
            return ProcessingResult.DEFAULT;
        }

        IElementType afterEol = builder.advance().getType();
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            if (!MarkdownConstraints.fromBase(builder, 1, myConstraints).upstreamWith(myConstraints)) {
                return ProcessingResult.DEFAULT;
            }

            afterEol = builder.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(builder));
        }

        if (afterEol == MarkdownTokenTypes.SETEXT_1 || afterEol == MarkdownTokenTypes.SETEXT_2) {
            return new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DROP, EventAction.PROPAGATE);
        }

        // Something breaks paragraph
        if (afterEol == MarkdownTokenTypes.EOL
            || afterEol == MarkdownTokenTypes.HORIZONTAL_RULE
            || afterEol == MarkdownTokenTypes.CODE_FENCE_START
            || afterEol == MarkdownTokenTypes.LIST_BULLET
            || afterEol == MarkdownTokenTypes.LIST_NUMBER
            || afterEol == MarkdownTokenTypes.ATX_HEADER
            || afterEol == MarkdownTokenTypes.BLOCK_QUOTE
            || afterEol == MarkdownTokenTypes.HTML_BLOCK) {
            return ProcessingResult.DEFAULT;
        }

        return ProcessingResult.CANCEL;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.PARAGRAPH;
    }

    @NotNull @Override public Collection<TextRange> getRangesContainingInlineStructure() {
        final int endPosition = productionHolder.getCurrentPosition();
        return SequentialParserUtil.filterBlockquotes(tokensCache, TextRange.create(startPosition, endPosition));
    }
}