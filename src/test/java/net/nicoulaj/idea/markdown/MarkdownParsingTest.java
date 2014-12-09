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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.UsefulTestCase;
import net.nicoulaj.idea.markdown.lang.MarkdownTreeBuilder;
import net.nicoulaj.idea.markdown.lang.ast.ASTNode;
import net.nicoulaj.idea.markdown.lang.ast.LeafASTNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class MarkdownParsingTest extends UsefulTestCase {

    private void defaultTest() {
        String src;
        try {
            src = FileUtil.loadFile(new File(getTestDataPath() + "/" + getTestName(true) + ".md")).trim();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("failed to read src");
        }

        ASTNode tree = new MarkdownTreeBuilder().buildMarkdownTreeFromString(src);
        String result = treeToStr(src, tree);

        assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(false) + ".txt", result);

        new MarkdownTreeBuilder();
    }

    private String treeToStr(String src, @NotNull ASTNode tree) {
        return treeToStr(src, tree, new StringBuilder(), 0).toString();
    }

    private StringBuilder treeToStr(String src, @NotNull ASTNode tree, @NotNull StringBuilder sb, int depth) {
        if (sb.length() > 0) {
            sb.append('\n');
        }
        for (int i = 0; i < depth * 2; ++i) {
            sb.append(' ');
        }

        sb.append(tree.getType().toString());
        if (tree instanceof LeafASTNode) {
            final String str = src.substring(tree.getStartOffset(), tree.getEndOffset());
            sb.append("('").append(str.replaceAll("\\n", "\\\\n")).append("')");
        }
        for (ASTNode child : tree.getChildren()) {
            treeToStr(src, child, sb, depth + 1);
        }

        return sb;
    }

    public void testSimple() {
        defaultTest();
    }

    public void testCodeBlocks() {
        defaultTest();
    }

    public void testUnorderedLists() {
        defaultTest();
    }

    public void testOrderedLists() {
        defaultTest();
    }

    public void testBlockquotes() {
        defaultTest();
    }

    public void testHeaders() {
        defaultTest();
    }

    public void testHtmlBlocks() {
        defaultTest();
    }

    public void testEmphStrong() {
        defaultTest();
    }

    public void testCodeFence() {
        defaultTest();
    }

    public void testCodeSpan() {
        defaultTest();
    }

    public void testLinkDefinitions() {
        defaultTest();
    }

    public void testInlineLinks() {
        defaultTest();
    }

    public void testReferenceLinks() {
        defaultTest();
    }

    protected String getTestDataPath() {
        return new File("src/test/data/parser").getAbsolutePath();
    }
}
