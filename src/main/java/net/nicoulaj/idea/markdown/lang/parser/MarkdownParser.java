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

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Parser implementation for Markdown.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 * @since 0.1
 */
public class MarkdownParser implements PsiParser {

    private final static Logger LOG = Logger.getInstance(MarkdownParser.class);


    @NotNull
    private MarkerProcessor markerProcessor;


    public MarkdownParser(@NotNull MarkerProcessor markerProcessor) {
        this.markerProcessor = markerProcessor;
    }

    /**
     * Parse the contents of the specified PSI builder and returns an AST tree with the
     * specified type of root element.
     *
     * @param root    the type of the root element in the AST tree.
     * @param builder the builder which is used to retrieve the original file tokens and build the AST tree.
     * @return the root of the resulting AST tree.
     */
    @NotNull
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        TokensCache tokensCache = new TokensCache(builder);
        markerProcessor.setTokensCache(tokensCache);

        PsiBuilder.Marker rootMarker = builder.mark();

        while (!builder.eof()) {
            final IElementType tokenType = builder.getTokenType();
            assert tokenType != null : "We have checked it's not eof!";
            markerProcessor.processToken(tokenType, builder);
            builder.advanceLexer();
        }
        markerProcessor.flushMarkers();

        rootMarker.done(root);

        return builder.getTreeBuilt();
    }

}
