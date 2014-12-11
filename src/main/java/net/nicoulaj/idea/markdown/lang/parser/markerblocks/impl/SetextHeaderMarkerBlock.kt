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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks.impl

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.InlineStructureHoldingMarkerBlock
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock

public class SetextHeaderMarkerBlock(myConstraints: MarkdownConstraints,
                                     tokensCache: TokensCache,
                                     productionHolder: ProductionHolder)
        : InlineStructureHoldingMarkerBlock(myConstraints, tokensCache, productionHolder, setOf(MarkdownTokenTypes.SETEXT_1,
                                                                                                MarkdownTokenTypes.SETEXT_2)) {

    private var nodeType: IElementType = MarkdownElementTypes.SETEXT_1

    private val startPosition: Int = productionHolder.currentPosition

    override fun getRangesContainingInlineStructure(): Collection<Range<Int>> {
        val endPosition = productionHolder.currentPosition
        return listOf(startPosition..endPosition - 2)
    }

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DROP
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (tokenType == MarkdownTokenTypes.SETEXT_1) {
            nodeType = MarkdownElementTypes.SETEXT_1
        } else {
            nodeType = MarkdownElementTypes.SETEXT_2
        }

        return MarkerBlock.ProcessingResult.DEFAULT.postpone()
    }
}
