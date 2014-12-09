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
package net.nicoulaj.idea.markdown.lang.dialects.commonmark;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.dialects.FixedPriorityListMarkerProcessor;
import net.nicoulaj.idea.markdown.lang.parser.*;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.nicoulaj.idea.markdown.lang.MarkdownElementTypes.*;


public class CommonMarkMarkerProcessor extends FixedPriorityListMarkerProcessor {
    private final static Logger LOG = Logger.getInstance(CommonMarkMarkerProcessor.class);

    public CommonMarkMarkerProcessor() {
        super(MarkdownConstraints.BASE);
    }

    @Override protected List<Pair<IElementType, Integer>> getPriorityList() {
        final List<Pair<IElementType, Integer>> result = new ArrayList<Pair<IElementType, Integer>>();
        final List<List<IElementType>> itemsByPriority = new ArrayList<List<IElementType>>();

        itemsByPriority.add(ContainerUtil.list(ATX_1, ATX_2, ATX_3, ATX_4, ATX_5, ATX_6));

        for (int i = 0; i < itemsByPriority.size(); ++i) {
            final List<IElementType> types = itemsByPriority.get(i);
            for (IElementType type : types) {
                result.add(Pair.create(type, i + 1));
            }
        }

        return result;
    }

    @NotNull @Override public MarkerBlock[] createNewMarkerBlocks(@NotNull final IElementType tokenType,
                                                                  @NotNull TokensCache.Iterator iterator,
                                                                  @NotNull ProductionHolder productionHolder) {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return NO_BLOCKS;
        }
        if (tokenType == MarkdownTokenTypes.HORIZONTAL_RULE
            || tokenType == MarkdownTokenTypes.SETEXT_1
            || tokenType == MarkdownTokenTypes.SETEXT_2
            || tokenType == MarkdownTokenTypes.HTML_BLOCK) {
            return NO_BLOCKS;
        }

        List<MarkerBlock> result = new ArrayList<MarkerBlock>(1);

        final MarkdownConstraints newConstraints = getCurrentConstraints().addModifierIfNeeded(tokenType, iterator);
        final ParagraphMarkerBlock paragraph = getParagraphBlock();

        if (MarkdownParserUtil.getIndentBeforeRawToken(iterator, 0) >= newConstraints.getIndent() + 4
            && paragraph == null) {
            result.add(new CodeBlockMarkerBlock(newConstraints, productionHolder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            result.add(new BlockQuoteMarkerBlock(newConstraints, productionHolder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.LIST_NUMBER
            || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            if (getLastBlock() instanceof ListMarkerBlock) {
                result.add(new ListItemMarkerBlock(newConstraints, productionHolder.mark()));
            }
            else {
                result.add(new ListMarkerBlock(newConstraints, productionHolder.mark(), tokenType));
                result.add(new ListItemMarkerBlock(newConstraints, productionHolder.mark()));
            }
        }
        else if (tokenType == MarkdownTokenTypes.ATX_HEADER && paragraph == null) {
            final String tokenText = iterator.getText();
            result.add(new AtxHeaderMarkerBlock(newConstraints, productionHolder.mark(), tokenText.length()));
        }
        else if (tokenType == MarkdownTokenTypes.CODE_FENCE_START) {
            result.add(new CodeFenceMarkerBlock(newConstraints, productionHolder.mark()));
        }
        else {
            LOG.assertTrue(tokenType != MarkdownTokenTypes.EOL);
            ParagraphMarkerBlock paragraphToUse;

            if (paragraph == null) {
                paragraphToUse = new ParagraphMarkerBlock(newConstraints, productionHolder, tokensCache);
                result.add(paragraphToUse);

                if (isAtLineStart(iterator)) {
                    result.add(new SetextHeaderMarkerBlock(newConstraints, productionHolder.mark()));

                }
//                addLinkDefinitionIfAny(iterator, result, newConstraints, paragraphToUse);
            }

//            addInlineMarkerBlocks(result, paragraphToUse, iterator, tokenType);
        }

        return result.toArray(new MarkerBlock[result.size()]);
    }

    @Contract(pure=true)
    @Nullable
    private ParagraphMarkerBlock getParagraphBlock() {
        return ContainerUtil.findInstance(getMarkersStack(), ParagraphMarkerBlock.class);
    }

    @Nullable
    private MarkerBlock getLastBlock() {
        return ContainerUtil.getLastItem(getMarkersStack());
    }

    private static boolean isAtLineStart(@NotNull TokensCache.Iterator iterator) {
        for (int index = -1;; --index) {
            final IElementType type = iterator.rawLookup(index);
            if (type == null || type == MarkdownTokenTypes.EOL) {
                return true;
            }
            if (type != MarkdownTokenTypes.WHITE_SPACE) {
                return false;
            }
        }
    }

}
