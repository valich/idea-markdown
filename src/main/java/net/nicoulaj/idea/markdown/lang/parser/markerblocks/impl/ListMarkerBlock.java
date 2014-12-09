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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks.impl;

import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkdownParserUtil;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlockImpl;
import org.jetbrains.annotations.NotNull;

public class ListMarkerBlock extends MarkerBlockImpl {

    @NotNull
    protected final IElementType listType;

    public ListMarkerBlock(@NotNull MarkdownConstraints myConstraints, @NotNull ProductionHolder.Marker marker, @NotNull IElementType listType) {
        super(myConstraints, marker, MarkdownTokenTypes.EOL);

        this.listType = listType;
    }

    @NotNull @Override protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator iterator, @NotNull MarkdownConstraints currentConstraints) {
        LOG.assertTrue(tokenType == MarkdownTokenTypes.EOL);

        final int eolN = MarkdownParserUtil.calcNumberOfConsequentEols(iterator);
        if (eolN >= 3) {
            return ProcessingResult.DEFAULT;
        }

        final int eolIndex = MarkdownParserUtil.getFirstNonWhitespaceLineEolRawIndex(iterator);
        final MarkdownConstraints nextLineConstraints = MarkdownConstraints.fromBase(iterator, eolIndex + 1, myConstraints);

        if (!nextLineConstraints.extendsList(myConstraints)) {
            return ProcessingResult.DEFAULT;
        }

        return ProcessingResult.PASS;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return listType == MarkdownTokenTypes.LIST_BULLET
                ? MarkdownElementTypes.UNORDERED_LIST
                : MarkdownElementTypes.ORDERED_LIST;
    }
}