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

    IElementType PARAGRAPH = new MarkdownElementType("PARAGRAPH");

    IElementType EMPH = new MarkdownElementType("EMPH");

    IElementType STRONG = new MarkdownElementType("STRONG");

    IElementType SETEXT_1 = new MarkdownElementType("SETEXT_1");

    IElementType SETEXT_2 = new MarkdownElementType("SETEXT_2");

}
