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
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.dialects.FixedPriorityListMarkerProcessor;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.MarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.MarkerProcessor;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class CommonMarkMarkerProcessor extends FixedPriorityListMarkerProcessor {
    public CommonMarkMarkerProcessor() {
        super(MarkdownConstraints.BASE);
    }

    @Override protected List<IElementType> getPriorityList() {
        return ContainerUtil.list(
//            CODE_FENCE, BLOCK_QUOTE, UNORDERED_LIST, ORDERED_LIST, PARAGRAPH, STRONG, EMPH
        );
    }

    @NotNull @Override public MarkerBlock[] createNewMarkerBlocks(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkerProcessor markerProcessor) {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return NO_BLOCKS;
        }
        if (tokenType == MarkdownTokenTypes.HORIZONTAL_RULE
            || tokenType == MarkdownTokenTypes.SETEXT_1
            || tokenType == MarkdownTokenTypes.SETEXT_2) {
            return NO_BLOCKS;
        }

        List<MarkerBlock> result = new ArrayList<MarkerBlock>(1);

        final MarkdownConstraints newConstraints = markerProcessor.getCurrentConstraints().addModifierIfNeeded(tokenType, builder);

        if (MarkdownParserUtil.getIndentBeforeRawToken(builder, 0) >= newConstraints.getIndent() + 4
                && !hasParagraphBlock(markerProcessor)) {
            result.add(new CodeBlockMarkerBlock(newConstraints, builder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            result.add(new BlockQuoteMarkerBlock(newConstraints, builder.mark()));
        }
        else if (tokenType == MarkdownTokenTypes.LIST_NUMBER
            || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            final MarkerBlock topBlock = ContainerUtil.getLastItem(markerProcessor.getMarkersStack());
            if (topBlock instanceof ListMarkerBlock) {
                result.add(new ListItemMarkerBlock(newConstraints, builder.mark()));
            }
            else {
                result.add(new ListMarkerBlock(newConstraints, builder.mark(), tokenType));
                result.add(new ListItemMarkerBlock(newConstraints, builder.mark()));
            }
        }
        else if (tokenType != MarkdownTokenTypes.EOL) {
            if (!hasParagraphBlock(markerProcessor)) {
                final ParagraphMarkerBlock paragraphBlock = new ParagraphMarkerBlock(newConstraints, builder.mark());
                result.add(paragraphBlock);

                if (isAtLineStart(builder)) {
                    result.add(new SetextHeaderMarkerBlock(newConstraints, builder.mark()));
                }

            }
        }

        return result.toArray(new MarkerBlock[result.size()]);
    }


    private static boolean hasParagraphBlock(@NotNull MarkerProcessor markerProcessor) {
        return ContainerUtil.findInstance(markerProcessor.getMarkersStack(), ParagraphMarkerBlock.class) != null;
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
