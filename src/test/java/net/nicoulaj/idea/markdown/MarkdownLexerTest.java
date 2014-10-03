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
package net.nicoulaj.idea.markdown;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;
import net.nicoulaj.idea.markdown.lang.lexer.MarkdownLexer;

public class MarkdownLexerTest extends LexerTestCase {
    @Override protected Lexer createLexer() {
        return new MarkdownLexer();
    }

    @Override protected String getDirPath() {
        return "src/test/data/lexer";
    }

    public void testSimple() {
        defaultTest();
    }

    public void testUnorderedLists() {
        defaultTest();
    }

    public void testOrderedLists() {
        defaultTest();
    }

    public void testHeaders() {
        defaultTest();
    }

    public void testBlockquotes() {
        defaultTest();
    }

    public void testHorizontalRules() {
        defaultTest();
    }

    public void testCodeBlocks() {
        defaultTest();
    }

    public void testCodeFence() {
        defaultTest();
    }

    public void testHtmlBlocks() {
        defaultTest();
    }

    public void testLinkDefinitions() {
        defaultTest();
    }

    public void testCodeSpan() {
        defaultTest();
    }

    public void testLinks() {
        defaultTest();
    }

    public void testAutolinks() {
        defaultTest();
    }

    private void defaultTest() {
        doFileTest("md");
    }
}
