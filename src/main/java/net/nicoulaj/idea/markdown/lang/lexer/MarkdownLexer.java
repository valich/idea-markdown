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
package net.nicoulaj.idea.markdown.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

import static net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes.*;

import java.io.Reader;

public class MarkdownLexer extends MergingLexerAdapter {
    private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(TEXT, WHITE_SPACE, CODE, HTML_BLOCK, LINK_ID, LINK_TITLE, URL, BAD_CHARACTER);

    public MarkdownLexer() {
        super(new FlexAdapter(new _MarkdownLexer((Reader)null)), TOKENS_TO_MERGE);
    }
}
