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

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

/**
 * Lexer tokens for the Markdown language.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 * @since 0.1
 */
public interface MarkdownTokenTypes extends TokenType {

    /** Plain text token type. */
    IElementType TEXT = new MarkdownElementType("TEXT");

    /** Code token type. */
    IElementType CODE = new MarkdownElementType("CODE");

    /** Table token type. */
    IElementType TABLE = new MarkdownElementType("TABLE");

    /** Block quote token type. */
    IElementType BLOCK_QUOTE = new MarkdownElementType("BLOCK_QUOTE");

    /** Table body token type. */
    IElementType TABLE_BODY = new MarkdownElementType("TABLE_BODY");

    /** Table cell token type. */
    IElementType TABLE_CELL = new MarkdownElementType("TABLE_CELL");

    /** Table column token type. */
    IElementType TABLE_COLUMN = new MarkdownElementType("TABLE_COLUMN");

    /** Table header token type. */
    IElementType TABLE_HEADER = new MarkdownElementType("TABLE_HEADER");

    /** Table row token type. */
    IElementType TABLE_ROW = new MarkdownElementType("TABLE_ROW");

    /** Table caption token type. */
    IElementType TABLE_CAPTION = new MarkdownElementType("TABLE_CAPTION");

    /** HTML block token type. */
    IElementType HTML_BLOCK = new MarkdownElementType("HTML_BLOCK");

    /** Inline HTML token type. */
    IElementType INLINE_HTML = new MarkdownElementType("INLINE_HTML");

    IElementType SINGLE_QUOTE = new MarkdownElementType("'");
    IElementType DOUBLE_QUOTE = new MarkdownElementType("\"");
    IElementType LPAREN = new MarkdownElementType("(");
    IElementType RPAREN = new MarkdownElementType(")");
    IElementType LBRACKET = new MarkdownElementType("[");
    IElementType RBRACKET = new MarkdownElementType("]");
    IElementType LT = new MarkdownElementType("<");
    IElementType GT = new MarkdownElementType(">");

    IElementType COLON = new MarkdownElementType(":");
    IElementType EXCLAMATION_MARK = new MarkdownElementType("!");


    IElementType HARD_LINE_BREAK = new MarkdownElementType("BR");
    IElementType EOL = new MarkdownElementType("EOL");

    IElementType LINK_ID = new MarkdownElementType("LINK_ID");
    IElementType ATX_HEADER = new MarkdownElementType("ATX_HEADER");
    IElementType EMPH = new MarkdownElementType("EMPH");

    IElementType BACKTICK = new MarkdownElementType("BACKTICK");

    IElementType TAG_NAME = new MarkdownElementType("TAG_NAME");
    IElementType LIST_BULLET = new MarkdownElementType("LIST_BULLET");
    IElementType URL = new MarkdownElementType("URL");
    IElementType HORIZONTAL_RULE = new MarkdownElementType("HORIZONTAL_RULE");
    IElementType SETEXT_1 = new MarkdownElementType("SETEXT_1");
    IElementType SETEXT_2 = new MarkdownElementType("SETEXT_2");
    IElementType LIST_NUMBER = new MarkdownElementType("LIST_NUMBER");
    IElementType FENCE_LANG = new MarkdownElementType("FENCE_LANG");
    IElementType CODE_FENCE_START = new MarkdownElementType("CODE_FENCE_START");
    IElementType CODE_FENCE_END = new MarkdownElementType("CODE_FENCE_END");
    IElementType LINK_TITLE = new MarkdownElementType("LINK_TITLE");

    IElementType AUTOLINK = new MarkdownElementType("AUTOLINK");
    IElementType EMAIL_AUTOLINK = new MarkdownElementType("EMAIL_AUTOLINK");
    IElementType HTML_TAG = new MarkdownElementType("HTML_TAG");




    IElementType BAD_CHARACTER = new MarkdownElementType("BAD_CHARACTER");
}
