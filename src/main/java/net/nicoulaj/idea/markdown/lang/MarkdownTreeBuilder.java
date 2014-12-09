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
package net.nicoulaj.idea.markdown.lang;

import com.sun.istack.internal.NotNull;
import net.nicoulaj.idea.markdown.lang.ast.ASTNode;
import net.nicoulaj.idea.markdown.lang.dialects.commonmark.CommonMarkMarkerProcessor;
import net.nicoulaj.idea.markdown.lang.lexer.MarkdownLexer;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownParser;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;

public class MarkdownTreeBuilder {
    public ASTNode buildMarkdownTreeFromString(@NotNull final String text) {
        TokensCache cache = new TokensCache(new MarkdownLexer(text));
        return new MarkdownParser(new CommonMarkMarkerProcessor()).parse(MarkdownElementTypes.MARKDOWN_FILE, cache);
    }
}
