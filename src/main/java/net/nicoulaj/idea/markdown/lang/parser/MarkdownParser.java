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
package net.nicoulaj.idea.markdown.lang.parser;

import com.intellij.openapi.util.TextRange;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * Parser implementation for Markdown.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 * @since 0.1
 */
public class MarkdownParser {
    @NotNull
    private final MarkerProcessor markerProcessor;


    public MarkdownParser(@NotNull MarkerProcessor markerProcessor) {
        this.markerProcessor = markerProcessor;
    }

    @NotNull
    public ASTNode parse(IElementType root, TokensCache tokensCache) {
        markerProcessor.setTokensCache(tokensCache);

        final int startOffset = 0;

        TokensCache.Iterator iterator = tokensCache.new Iterator(startOffset);
        while (iterator.getType() != null) {
            final IElementType tokenType = iterator.getType();
            iterator = markerProcessor.processToken(tokenType, iterator);
            iterator = iterator.advance();
        }
        markerProcessor.flushMarkers(iterator);


        final MyBuilder builder = new MyBuilder();

        builder.addNode(new SequentialParser.Node(TextRange.create(0, iterator.getIndex()), root));
        builder.addNodes(markerProcessor.getProduction());

        return builder.buildTree(tokensCache);
    }

}
