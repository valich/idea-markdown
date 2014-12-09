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

import com.intellij.openapi.util.TextRange;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParser;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public abstract class InlineStructureHoldingMarkerBlock extends MarkerBlockImpl {

    @NotNull
    protected final TokensCache tokensCache;

    @NotNull
    protected final ProductionHolder productionHolder;

    public InlineStructureHoldingMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                             @NotNull TokensCache tokensCache,
                                             @NotNull ProductionHolder productionHolder,
                                             @Nullable Set<IElementType> interestingTypes) {
        super(myConstraints, productionHolder.mark(), interestingTypes);
        this.tokensCache = tokensCache;
        this.productionHolder = productionHolder;
    }

    public InlineStructureHoldingMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                             @NotNull TokensCache tokensCache,
                                             @NotNull ProductionHolder productionHolder,
                                             @NotNull IElementType interestingType) {
        super(myConstraints, productionHolder.mark(), interestingType);
        this.tokensCache = tokensCache;
        this.productionHolder = productionHolder;
    }

    public InlineStructureHoldingMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                             @NotNull TokensCache tokensCache,
                                             @NotNull ProductionHolder productionHolder) {
        super(myConstraints, productionHolder.mark());
        this.tokensCache = tokensCache;
        this.productionHolder = productionHolder;
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action != ClosingAction.NOTHING) {
            if (action == ClosingAction.DONE || action == ClosingAction.DEFAULT && getDefaultAction() == ClosingAction.DONE) {
                final Collection<SequentialParser.Node> results =
                        new SequentialParserManager().runParsingSequence(tokensCache, getRangesContainingInlineStructure());

                productionHolder.addProduction(results);
            }
        }

        return super.acceptAction(action);
    }

    @NotNull
    public abstract Collection<TextRange> getRangesContainingInlineStructure();
}
