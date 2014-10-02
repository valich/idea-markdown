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

    /** Bold text token type. */
    IElementType BOLD = new MarkdownElementType("BOLD");

    /** Italic token type. */
    IElementType ITALIC = new MarkdownElementType("ITALIC");

    /** Header of level 1 token type. */
    IElementType HEADER_LEVEL_1 = new MarkdownElementType("HEADER_LEVEL_1");

    /** Header of level 2 token type. */
    IElementType HEADER_LEVEL_2 = new MarkdownElementType("HEADER_LEVEL_2");

    /** Header of level 3 token type. */
    IElementType HEADER_LEVEL_3 = new MarkdownElementType("HEADER_LEVEL_3");

    /** Header of level 4 token type. */
    IElementType HEADER_LEVEL_4 = new MarkdownElementType("HEADER_LEVEL_4");

    /** Header of level 5 token type. */
    IElementType HEADER_LEVEL_5 = new MarkdownElementType("HEADER_LEVEL_5");

    /** Header of level 6 token type. */
    IElementType HEADER_LEVEL_6 = new MarkdownElementType("HEADER_LEVEL_6");

    /** Code token type. */
    IElementType CODE = new MarkdownElementType("CODE");

    /** Quote token type. */
    IElementType QUOTE = new MarkdownElementType("QUOTE");

    /** Table token type. */
    IElementType TABLE = new MarkdownElementType("TABLE");

    /** HRule token type. */
    IElementType HRULE = new MarkdownElementType("HRULE");

    /** Special text token type. */
    IElementType SPECIAL_TEXT = new MarkdownElementType("SPECIAL_TEXT");

    /** Strikethrough token type. */
    IElementType STRIKETHROUGH = new MarkdownElementType("STRIKETHROUGH");

    /** Link token type. */
    IElementType EXPLICIT_LINK = new MarkdownElementType("EXPLICIT_LINK");

    /** Image token type. */
    IElementType IMAGE = new MarkdownElementType("IMAGE");

    /** Reference image token type. */
    IElementType REFERENCE_IMAGE = new MarkdownElementType("REFERENCE_IMAGE");

    /** Reference link token type. */
    IElementType REFERENCE_LINK = new MarkdownElementType("REFERENCE_LINK");

    /** Wiki link token type. */
    IElementType WIKI_LINK = new MarkdownElementType("WIKI_LINK");

    /** Auto link token type. */
    IElementType AUTO_LINK = new MarkdownElementType("AUTO_LINK");

    /** Mail link token type. */
    IElementType MAIL_LINK = new MarkdownElementType("MAIL_LINK");

    /** Verbatim token type. */
    IElementType VERBATIM = new MarkdownElementType("VERBATIM");

    /** Block quote token type. */
    IElementType BLOCK_QUOTE = new MarkdownElementType("BLOCK_QUOTE");

    /** Bullet list token type. */
    IElementType BULLET_LIST = new MarkdownElementType("BULLET_LIST");

    /** Ordered list token type. */
    IElementType ORDERED_LIST = new MarkdownElementType("ORDERED_LIST");

    /** List item token type. */
    IElementType LIST_ITEM = new MarkdownElementType("LIST_ITEM");

    /** Definition list token type. */
    IElementType DEFINITION_LIST = new MarkdownElementType("DEFINITION_LIST");

    /** Definition token type. */
    IElementType DEFINITION = new MarkdownElementType("DEFINITION");

    /** Definition term token type. */
    IElementType DEFINITION_TERM = new MarkdownElementType("DEFINITION_TERM");

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

    /** Reference token type. */
    IElementType REFERENCE = new MarkdownElementType("REFERENCE");

    /** Abbreviation token type. */
    IElementType ABBREVIATION = new MarkdownElementType("ABBREVIATION");

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

    IElementType EOL = new MarkdownElementType("EOL");

    IElementType LINK_ID = new MarkdownElementType("LINK_ID");
    IElementType ATX_HEADER = new MarkdownElementType("ATX_HEADER");
    IElementType EMPH = new MarkdownElementType("EMPH");

    IElementType BACKTICK = new MarkdownElementType("BACKTICK");

    IElementType TAG_NAME = new MarkdownElementType("TAG_NAME");
    IElementType STRONG = new MarkdownElementType("STRONG");
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



    IElementType BAD_CHARACTER = new MarkdownElementType("BAD_CHARACTER");
}
