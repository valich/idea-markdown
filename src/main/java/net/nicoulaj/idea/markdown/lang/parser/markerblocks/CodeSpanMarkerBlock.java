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
import com.intellij.openapi.util.Condition;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.InlineHangableMarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.InlineMarkerManager;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import org.jetbrains.annotations.NotNull;

public class CodeSpanMarkerBlock extends InlineHangableMarkerBlock {
    private final int length;

    public CodeSpanMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                               @NotNull PsiBuilder builder,
                               @NotNull InlineMarkerManager markerManager) {
        super(myConstraints, builder, TokenSet.create(
                MarkdownTokenTypes.BACKTICK, MarkdownTokenTypes.ESCAPED_BACKTICKS //, MarkdownTokenTypes.EMPH, MarkdownTokenTypes.INLINE_HTML,
//                MarkdownTokenTypes.AUTOLINK, MarkdownTokenTypes.EMAIL_AUTOLINK, MarkdownTokenTypes.LT
        ), markerManager);


        length = getLength(builder, true);
    }

    private int getLength(@NotNull PsiBuilder builder, boolean canEscape) {
        final String tokenText = builder.getTokenText();
        assert tokenText != null;

        int toSubtract = 0;
        if (builder.getTokenType() == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
            if (canEscape) {
                toSubtract = 2;
            } else {
                toSubtract = 1;
            }
        }

        return tokenText.length() - toSubtract;
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DROP;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull PsiBuilder builder,
                                                                 @NotNull
                                                                 MarkdownConstraints currentConstraints) {
        if (getLength(builder, false) == length) {
            markerManager.cancelMarkersInterlappingWith(this, new Condition<InlineHangableMarkerBlock>() {
                @Override public boolean value(InlineHangableMarkerBlock markerBlock) {
                    return markerBlock instanceof EmphStrongMarkerBlock;
                }
            });
            return ProcessingResult.DEFAULT.postpone();
        } else {
            return ProcessingResult.CANCEL;
        }
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_SPAN;
    }
}
