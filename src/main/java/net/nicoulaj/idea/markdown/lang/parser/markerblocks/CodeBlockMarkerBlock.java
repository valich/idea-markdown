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

public class CodeBlockMarkerBlock extends MarkerBlockImpl {
    public CodeBlockMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder.Marker marker) {
        super(myConstraints, marker);
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints) {
        // Eat everything if we're on code line
        if (tokenType != MarkdownTokenTypes.EOL) {
            return ProcessingResult.CANCEL;
        }

        LOG.assertTrue(tokenType == MarkdownTokenTypes.EOL);

        IElementType afterEol = builder.lookAhead(1);
        int nonWhitespaceOffset;
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            final MarkdownConstraints nextLineConstraints = MarkdownConstraints.fromBase(builder, 1, myConstraints);
            // kinda equals
            if (!(nextLineConstraints.upstreamWith(myConstraints) && nextLineConstraints.extendsPrev(myConstraints))) {
                return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
            }

            afterEol = builder.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(builder));
            nonWhitespaceOffset = MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(builder);
        } else {
            nonWhitespaceOffset = MarkdownParserUtil.getFirstNonWhiteSpaceRawIndex(builder);
        }

        if (afterEol == MarkdownTokenTypes.EOL) {
            return ProcessingResult.CANCEL;
        }

        final int indent = builder.rawTokenTypeStart(nonWhitespaceOffset) - builder.rawTokenTypeStart(1);
        if (indent < myConstraints.getIndent() + 4) {
            return new ProcessingResult(ClosingAction.DROP, ClosingAction.DONE, EventAction.PROPAGATE);
        } else {
            return ProcessingResult.CANCEL;
        }
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_BLOCK;
    }
}
