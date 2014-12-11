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
package net.nicoulaj.idea.markdown.lang.parser.dialects.commonmark

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import net.nicoulaj.idea.markdown.lang.parser.dialects.FixedPriorityListMarkerProcessor
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkdownParserUtil
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.impl.*

import java.util.ArrayList
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes

public class CommonMarkMarkerProcessor : FixedPriorityListMarkerProcessor(MarkdownConstraints.BASE) {

    override fun getPriorityList(): List<Pair<IElementType, Int>> {
        val result = ArrayList<Pair<IElementType, Int>>()
        val itemsByPriority = ArrayList<List<IElementType>>()

        itemsByPriority.add(listOf(
                MarkdownElementTypes.ATX_1,
                MarkdownElementTypes.ATX_2,
                MarkdownElementTypes.ATX_3,
                MarkdownElementTypes.ATX_4,
                MarkdownElementTypes.ATX_5,
                MarkdownElementTypes.ATX_6))

        for (i in itemsByPriority.indices) {
            val types = itemsByPriority.get(i)
            for (`type` in types) {
                result.add(Pair(`type`, i + 1))
            }
        }

        return result
    }

    override fun createNewMarkerBlocks(tokenType: IElementType, iterator: TokensCache.Iterator, productionHolder: ProductionHolder): Array<MarkerBlock> {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return NO_BLOCKS
        }
        if (tokenType == MarkdownTokenTypes.HORIZONTAL_RULE || tokenType == MarkdownTokenTypes.SETEXT_1 || tokenType == MarkdownTokenTypes.SETEXT_2 || tokenType == MarkdownTokenTypes.HTML_BLOCK) {
            return NO_BLOCKS
        }

        val result = ArrayList<MarkerBlock>(1)

        val newConstraints = currentConstraints.addModifierIfNeeded(tokenType, iterator)
        val paragraph = getParagraphBlock()

        if (MarkdownParserUtil.getIndentBeforeRawToken(iterator, 0) >= newConstraints.getIndent() + 4 && paragraph == null) {
            result.add(CodeBlockMarkerBlock(newConstraints, productionHolder.mark()))
        } else if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            result.add(BlockQuoteMarkerBlock(newConstraints, productionHolder.mark()))
        } else if (tokenType == MarkdownTokenTypes.LIST_NUMBER || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            if (getLastBlock() is ListMarkerBlock) {
                result.add(ListItemMarkerBlock(newConstraints, productionHolder.mark()))
            } else {
                result.add(ListMarkerBlock(newConstraints, productionHolder.mark(), tokenType))
                result.add(ListItemMarkerBlock(newConstraints, productionHolder.mark()))
            }
        } else if (tokenType == MarkdownTokenTypes.ATX_HEADER && paragraph == null) {
            val tokenText = iterator.text
            result.add(AtxHeaderMarkerBlock(newConstraints, tokensCache!!, productionHolder, tokenText.length()))
        } else if (tokenType == MarkdownTokenTypes.CODE_FENCE_START) {
            result.add(CodeFenceMarkerBlock(newConstraints, productionHolder.mark()))
        } else {
            assert(tokenType != MarkdownTokenTypes.EOL)
            val paragraphToUse: ParagraphMarkerBlock

            if (paragraph == null) {
                paragraphToUse = ParagraphMarkerBlock(newConstraints, productionHolder, tokensCache!!)
                result.add(paragraphToUse)

                if (isAtLineStart(iterator)) {
                    result.add(SetextHeaderMarkerBlock(newConstraints, tokensCache!!, productionHolder))

                }
                //                addLinkDefinitionIfAny(iterator, result, newConstraints, paragraphToUse);
            }

            //            addInlineMarkerBlocks(result, paragraphToUse, iterator, tokenType);
        }

        return result.toArray<MarkerBlock>(arrayOfNulls<MarkerBlock>(result.size()))
    }

    private fun getParagraphBlock(): ParagraphMarkerBlock? {
        return markersStack.firstOrNull { block -> block is ParagraphMarkerBlock } as ParagraphMarkerBlock?
    }

    private fun getLastBlock(): MarkerBlock? {
        return markersStack.lastOrNull()
    }

    class object {
        private fun isAtLineStart(iterator: TokensCache.Iterator): Boolean {
            var index = -1
            while (true) {
                val `type` = iterator.rawLookup(index)
                if (`type` == null || `type` == MarkdownTokenTypes.EOL) {
                    return true
                }
                if (`type` != MarkdownTokenTypes.WHITE_SPACE) {
                    return false
                }
                --index
            }
        }
    }

}
