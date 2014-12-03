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

import com.intellij.psi.tree.IElementType;

public interface MarkdownElementTypes {
    IElementType UNORDERED_LIST = new MarkdownElementType("UNORDERED_LIST");

    IElementType ORDERED_LIST = new MarkdownElementType("ORDERED_LIST");

    IElementType LIST_ITEM = new MarkdownElementType("LIST_ITEM");

    IElementType BLOCK_QUOTE = new MarkdownElementType("BLOCK_QUOTE");

    IElementType CODE_FENCE = new MarkdownElementType("CODE_FENCE");

    IElementType CODE_BLOCK = new MarkdownElementType("CODE_BLOCK");

    IElementType CODE_SPAN = new MarkdownElementType("CODE_SPAN");

    IElementType PARAGRAPH = new MarkdownElementType("PARAGRAPH");

    IElementType EMPH = new MarkdownElementType("EMPH");

    IElementType STRONG = new MarkdownElementType("STRONG");

    IElementType LINK_DEFINITION = new MarkdownElementType("LINK_DEFINITION");
    IElementType LINK_LABEL = new MarkdownElementType("LINK_LABEL");
    IElementType LINK_DESTINATION = new MarkdownElementType("LINK_DESTINATION");
    IElementType LINK_TITLE = new MarkdownElementType("LINK_TITLE");
    IElementType LINK_TEXT = new MarkdownElementType("LINK_TEXT");
    IElementType INLINE_LINK = new MarkdownElementType("INLINE_LINK");

    IElementType AUTOLINK = new MarkdownElementType("AUTOLINK");

    IElementType SETEXT_1 = new MarkdownElementType("SETEXT_1");
    IElementType SETEXT_2 = new MarkdownElementType("SETEXT_2");

    IElementType ATX_1 = new MarkdownElementType("ATX_1");
    IElementType ATX_2 = new MarkdownElementType("ATX_2");
    IElementType ATX_3 = new MarkdownElementType("ATX_3");
    IElementType ATX_4 = new MarkdownElementType("ATX_4");
    IElementType ATX_5 = new MarkdownElementType("ATX_5");
    IElementType ATX_6 = new MarkdownElementType("ATX_6");


}
