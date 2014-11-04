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

        final MarkdownConstraints newConstraints = markerProcessor.getCurrentConstraints().addModifierIfNeeded(tokenType, builder);

        if (MarkdownParserUtil.getIndentBeforeRawToken(builder, 0) >= newConstraints.getIndent() + 4
                && !hasParagraphBlock(markerProcessor)) {
            return oneBlock(new CodeBlockMarkerBlock(newConstraints, builder.mark()));
        }
        if (tokenType == MarkdownTokenTypes.BLOCK_QUOTE) {
            return oneBlock(new BlockQuoteMarkerBlock(newConstraints, builder.mark()));
        }
        if (tokenType == MarkdownTokenTypes.LIST_NUMBER
            || tokenType == MarkdownTokenTypes.LIST_BULLET) {
            final MarkerBlock topBlock = ContainerUtil.getLastItem(markerProcessor.getMarkersStack());
            if (topBlock instanceof ListMarkerBlock) {
                return oneBlock(new ListItemMarkerBlock(newConstraints, builder.mark()));
            }
            else {
                return new MarkerBlock[]{new ListMarkerBlock(newConstraints, builder.mark(), tokenType),
                                         new ListItemMarkerBlock(newConstraints, builder.mark())};
            }
        }
        if (tokenType != MarkdownTokenTypes.EOL) {
            if (hasParagraphBlock(markerProcessor)) {
                return NO_BLOCKS;
            }
            return oneBlock(new ParagraphMarkerBlock(newConstraints, builder.mark()));
        }

        return NO_BLOCKS;
    }

    private static boolean hasParagraphBlock(MarkerProcessor markerProcessor) {
        return ContainerUtil.findInstance(markerProcessor.getMarkersStack(), ParagraphMarkerBlock.class) != null;
    }

    @NotNull
    private static MarkerBlock[] oneBlock(MarkerBlock item) {
        return new MarkerBlock[]{item};
    }
}
