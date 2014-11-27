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

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.dialects.FixedPriorityListMarkerProcessor;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.MarkerBlock;
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
                                                                  @NotNull PsiBuilder builder) {
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

        final MarkdownConstraints newConstraints = getCurrentConstraints().addModifierIfNeeded(tokenType, builder);
        final ParagraphMarkerBlock paragraph = getParagraphBlock();

        if (MarkdownParserUtil.getIndentBeforeRawToken(builder, 0) >= newConstraints.getIndent() + 4
            && paragraph == null) {
            result.add(new CodeBlockMarkerBlock(newConstraints, builder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            result.add(new BlockQuoteMarkerBlock(newConstraints, builder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.LIST_NUMBER
            || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            final MarkerBlock topBlock = ContainerUtil.getLastItem(getMarkersStack());
            if (topBlock instanceof ListMarkerBlock) {
                result.add(new ListItemMarkerBlock(newConstraints, builder.mark()));
            }
            else {
                result.add(new ListMarkerBlock(newConstraints, builder.mark(), tokenType));
                result.add(new ListItemMarkerBlock(newConstraints, builder.mark()));
            }
        }
        else if (tokenType == MarkdownTokenTypes.ATX_HEADER && paragraph == null) {
            final String tokenText = builder.getTokenText();
            LOG.assertTrue(tokenText != null, "type is not null but text is!");
            result.add(new AtxHeaderMarkerBlock(newConstraints, builder.mark(), tokenText.length()));
        }
        else if (tokenType == MarkdownTokenTypes.CODE_FENCE_START) {
            result.add(new CodeFenceMarkerBlock(newConstraints, builder.mark()));
        }
        else {
            LOG.assertTrue(tokenType != MarkdownTokenTypes.EOL);
            ParagraphMarkerBlock paragraphToUse = paragraph;

            if (paragraph == null) {
                paragraphToUse = new ParagraphMarkerBlock(newConstraints, builder);
                result.add(paragraphToUse);

                if (isAtLineStart(builder)) {
                    result.add(new SetextHeaderMarkerBlock(newConstraints, builder.mark()));
                }
            }

            addInlineMarkerBlocks(result, paragraphToUse, builder, tokenType);
        }

        return result.toArray(new MarkerBlock[result.size()]);
    }

    protected void addInlineMarkerBlocks(@NotNull List<MarkerBlock> result,
                                         @NotNull ParagraphMarkerBlock paragraphToUse,
                                         @NotNull PsiBuilder builder,
                                         @NotNull IElementType tokenType) {
        if (tokenType == MarkdownTokenTypes.EMPH) {
            final MarkerBlock lastBlock = getLastBlock();
            final EmphStrongMarkerBlock prevBlock = lastBlock instanceof EmphStrongMarkerBlock
                                                    ? ((EmphStrongMarkerBlock) lastBlock)
                                                    : null;
            result.add(new EmphStrongMarkerBlock(getCurrentConstraints(),
                                                 builder,
                                                 paragraphToUse.getInlineMarkerManager(),
                                                 prevBlock));
        }
        else if (tokenType == MarkdownTokenTypes.BACKTICK
                 || tokenType == MarkdownTokenTypes.ESCAPED_BACKTICKS && builder.getTokenText().length() > 2) {
            result.add(new CodeSpanMarkerBlock(getCurrentConstraints(), builder, paragraphToUse.getInlineMarkerManager()));
        }
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

    private static boolean isAtLineStart(@NotNull PsiBuilder builder) {
        for (int index = -1;; --index) {
            final IElementType type = builder.rawLookup(index);
            if (type == null || type == MarkdownTokenTypes.EOL) {
                return true;
            }
            if (type != TokenType.WHITE_SPACE) {
                return false;
            }
        }
    }

}
