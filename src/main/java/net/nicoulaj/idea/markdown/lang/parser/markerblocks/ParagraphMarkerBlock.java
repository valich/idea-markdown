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
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypeSets;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.*;
import org.jetbrains.annotations.NotNull;

public class ParagraphMarkerBlock extends MarkerBlockImpl implements InlineStructureHolder {
    @NotNull
    private final PsiBuilder builder;

    private InlineMarkerManager myManager = null;

    public ParagraphMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder builder) {
        super(myConstraints, builder.mark(), MarkdownTokenTypes.EOL);
        this.builder = builder;
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkdownConstraints currentConstraints) {
        LOG.assertTrue(tokenType == MarkdownTokenTypes.EOL);

        if (MarkdownParserUtil.calcNumberOfConsequentEols(builder) >= 2) {
            return ProcessingResult.DEFAULT;
        }

        IElementType afterEol = builder.lookAhead(1);
        if (afterEol == MarkdownTokenTypes.BLOCK_QUOTE) {
            if (!MarkdownConstraints.fromBase(builder, 1, myConstraints).upstreamWith(myConstraints)) {
                return ProcessingResult.DEFAULT;
            }

            afterEol = builder.rawLookup(MarkdownParserUtil.getFirstNextLineNonBlockquoteRawIndex(builder));
        }

        if (MarkdownTokenTypeSets.SETEXT.contains(afterEol)) {
            return new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DROP, EventAction.PROPAGATE);
        }

        // Something breaks paragraph
        if (afterEol == MarkdownTokenTypes.EOL
            || afterEol == MarkdownTokenTypes.HORIZONTAL_RULE
            || afterEol == MarkdownTokenTypes.CODE_FENCE_START
            || afterEol == MarkdownTokenTypes.LIST_BULLET
            || afterEol == MarkdownTokenTypes.LIST_NUMBER
            || afterEol == MarkdownTokenTypes.ATX_HEADER
            || afterEol == MarkdownTokenTypes.BLOCK_QUOTE
            || afterEol == MarkdownTokenTypes.HTML_BLOCK) {
            return ProcessingResult.DEFAULT;
        }

        return ProcessingResult.CANCEL;
    }

    @Override public boolean acceptAction(@NotNull ClosingAction action) {
        if (action != ClosingAction.NOTHING && myManager != null) {
            if (action == ClosingAction.DONE || action == ClosingAction.DEFAULT) {
                myManager.flushAllClosedMarkers();
            }
            else {
//                assert false;
                // Actually, this practically means that a setext header is here,
                // and inline structure should be closed and put into the tree
                // anyway. So it's ok to flush markers in this way, too.
                // TODO (to be fixed, of course)
                myManager.flushAllClosedMarkers();
            }
        }

        return super.acceptAction(action);
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.PARAGRAPH;
    }

    @NotNull
    @Override public InlineMarkerManager getInlineMarkerManager() {
        if (myManager == null) {
            myManager = new InlineMarkerManager(builder);
        }
        return myManager;
    }
}
