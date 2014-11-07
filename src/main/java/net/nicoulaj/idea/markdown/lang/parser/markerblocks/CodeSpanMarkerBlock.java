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
import com.intellij.psi.tree.TokenSet;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkerBlockImpl;
import org.jetbrains.annotations.NotNull;

public class CodeSpanMarkerBlock extends MarkerBlockImpl {
    private final int length;

    public CodeSpanMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull PsiBuilder builder) {
        super(myConstraints, builder.mark(), TokenSet.create(
                MarkdownTokenTypes.BACKTICK, MarkdownTokenTypes.EMPH, MarkdownTokenTypes.INLINE_HTML,
                MarkdownTokenTypes.AUTOLINK, MarkdownTokenTypes.EMAIL_AUTOLINK, MarkdownTokenTypes.LT
        ));

        final String tokenText = builder.getTokenText();
        assert tokenText != null;
        length = tokenText.length();
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DROP;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull PsiBuilder builder,
                                                                 @NotNull
                                                                 MarkdownConstraints currentConstraints) {
        if (tokenType == MarkdownTokenTypes.BACKTICK) {
            final String tokenText = builder.getTokenText();
            if (tokenText != null && tokenText.length() == length) {
                return ProcessingResult.DEFAULT.postpone();
            }
            else {
                return ProcessingResult.CANCEL;
            }
        }

        if (tokenType == MarkdownTokenTypes.EMPH) {
            return ProcessingResult.CANCEL;
        }
        return ProcessingResult.PASS;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_SPAN;
    }
}
