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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks.inline;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.InlineHangableMarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.InlineMarkerManager;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import org.jetbrains.annotations.NotNull;

public class LinkDestinationMarkerBlock extends InlineHangableMarkerBlock {
    final boolean withBraces;
    final boolean oneShot;
    final int creationPos;

    public LinkDestinationMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                      @NotNull PsiBuilder builder,
                                      @NotNull InlineMarkerManager markerManager) {
        super(myConstraints, builder.mark(), markerManager);
        withBraces = builder.getTokenType() == MarkdownTokenTypes.LT;
        oneShot = !withBraces &&
                  (builder.rawLookup(1) == TokenType.WHITE_SPACE || builder.rawLookup(1) == MarkdownTokenTypes.EOL);
        creationPos = builder.getCurrentOffset();
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull PsiBuilder builder,
                                                                 @NotNull
                                                                 MarkdownConstraints currentConstraints) {
        if (tokenType == MarkdownTokenTypes.EOL) {
            return DROP_ACTION;
        }

        if (oneShot && creationPos != builder.getCurrentOffset()) {
            return DONE_ACTION;
        }
        if (withBraces && tokenType == MarkdownTokenTypes.GT) {
            return DONE_ACTION.postpone();
        }
        else if (!withBraces &&
                 (builder.rawLookup(1) == TokenType.WHITE_SPACE || builder.rawLookup(1) == MarkdownTokenTypes.EOL)) {
            return DONE_ACTION.postpone();
        }

        return ProcessingResult.PASS;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.LINK_DESTINATION;
    }
}
