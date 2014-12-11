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
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkdownParserUtil
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParserUtil
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock

public class ParagraphMarkerBlock(myConstraints: MarkdownConstraints,
                                  productionHolder: ProductionHolder,
                                  tokensCache: TokensCache)
        : InlineStructureHoldingMarkerBlock(myConstraints, tokensCache, productionHolder, setOf(MarkdownTokenTypes.EOL)) {
    private val startPosition: Int

    {
        startPosition = productionHolder.currentPosition
    }

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        assert(tokenType == MarkdownTokenTypes.EOL)

        if (MarkdownParserUtil.calcNumberOfConsequentEols(iterator) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        var afterEol: IElementType? = iterator.advance().type
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            if (!MarkdownConstraints.fromBase(iterator, 1, constraints).upstreamWith(constraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            afterEol = iterator.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(iterator))
        }

        if (afterEol == MarkdownTokenTypes.SETEXT_1 || afterEol == MarkdownTokenTypes.SETEXT_2) {
            return MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.NOTHING, MarkerBlock.ClosingAction.DROP, MarkerBlock.EventAction.PROPAGATE)
        }

        // Something breaks paragraph
        if (afterEol == MarkdownTokenTypes.EOL || afterEol == MarkdownTokenTypes.HORIZONTAL_RULE || afterEol == MarkdownTokenTypes.CODE_FENCE_START || afterEol == MarkdownTokenTypes.LIST_BULLET || afterEol == MarkdownTokenTypes.LIST_NUMBER || afterEol == MarkdownTokenTypes.ATX_HEADER || afterEol == MarkdownTokenTypes.BLOCK_QUOTE || afterEol == MarkdownTokenTypes.HTML_BLOCK) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.PARAGRAPH
    }

    override fun getRangesContainingInlineStructure(): Collection<Range<Int>> {
        val endPosition = productionHolder.currentPosition
        return SequentialParserUtil.filterBlockquotes(tokensCache, startPosition..endPosition)
    }
}
