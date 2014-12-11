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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserManager


public abstract class InlineStructureHoldingMarkerBlock(
        constraints: MarkdownConstraints,
        protected val tokensCache: TokensCache,
        protected val productionHolder: ProductionHolder,
        interestingTypes: Set<IElementType>?)
    : MarkerBlockImpl(constraints, productionHolder.mark(), interestingTypes) {

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        if (action != MarkerBlock.ClosingAction.NOTHING) {
            if (action == MarkerBlock.ClosingAction.DONE || action == MarkerBlock.ClosingAction.DEFAULT && getDefaultAction() == MarkerBlock.ClosingAction.DONE) {
                val results = SequentialParserManager().runParsingSequence(tokensCache, getRangesContainingInlineStructure())

                productionHolder.addProduction(results)
            }
        }

        return super.acceptAction(action)
    }

    public abstract fun getRangesContainingInlineStructure(): Collection<Range<Int>>
}
