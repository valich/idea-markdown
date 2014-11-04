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

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.MarkerBlockImpl;
import org.jetbrains.annotations.NotNull;

public class ParagraphMarkerBlock extends MarkerBlockImpl {
    public ParagraphMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder.Marker marker) {
        super(myConstraints, marker, MarkdownTokenTypes.EOL);
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints) {
        LOG.assertTrue(tokenType == MarkdownTokenTypes.EOL);

        if (MarkdownParserUtil.calcNumberOfConsequentEols(builder) >= 2) {
            return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
        }

        IElementType afterEol = builder.lookAhead(1);
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            if (!MarkdownConstraints.fromBase(builder, 1, myConstraints).upstreamWith(myConstraints)) {
                return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
            }

            afterEol = builder.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(builder));
        }

        // Something breaks paragraph
        if (afterEol == MarkdownTokenTypes.EOL
            || afterEol == MarkdownTokenTypes.HORIZONTAL_RULE
            || afterEol == MarkdownTokenTypes.CODE_FENCE_START
            || afterEol == MarkdownTokenTypes.LIST_BULLET
            || afterEol == MarkdownTokenTypes.LIST_NUMBER
            || afterEol == MarkdownTokenTypes.ATX_HEADER
            || afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
        }

        return ProcessingResult.CANCEL;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.PARAGRAPH;
    }

}
