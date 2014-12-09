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

import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import static net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes.*;

public class MarkdownLexer {
    private static final Set<IElementType> TOKENS_TO_MERGE = ContainerUtil.set(TEXT,
                                                                               WHITE_SPACE,
                                                                               CODE,
                                                                               HTML_BLOCK,
                                                                               LINK_ID,
                                                                               LINK_TITLE,
                                                                               URL,
                                                                               AUTOLINK,
                                                                               EMAIL_AUTOLINK,
                                                                               BAD_CHARACTER);

    @NotNull
    private final _MarkdownLexer baseLexer;

    @NotNull
    private final String originalText;

    @Nullable
    private IElementType currentType;
    @Nullable
    private IElementType nextType;

    private int tokenStart;
    private int tokenEnd;

    public MarkdownLexer(@NotNull final String text) {
        originalText = text;
        baseLexer = new _MarkdownLexer((Reader) null);
        baseLexer.reset(text, 0, text.length(), 0);

        init();
    }

    @NotNull
    public String getOriginalText() {
        return originalText;
    }

    public int getTokenStart() {
        return tokenStart;
    }

    public int getTokenEnd() {
        return tokenEnd;
    }

    @Nullable
    public IElementType getType() {
        return currentType;
    }

    public boolean advance() {
        return locateToken();
    }

    private void init() {
        currentType = advanceBase();
        tokenStart = baseLexer.getTokenStart();

        calcNextType();
    }

    private boolean locateToken() {
        currentType = nextType;
        tokenStart = tokenEnd;
        if (currentType == null) {
            return false;
        }

        calcNextType();
        return true;
    }

    private void calcNextType() {
        do {
            tokenEnd = baseLexer.getTokenEnd();
            nextType = advanceBase();
        } while (nextType == currentType && TOKENS_TO_MERGE.contains(currentType));
    }

    private IElementType advanceBase() {
        try {
            return baseLexer.advance();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("This could not be!");
        }
    }
}
