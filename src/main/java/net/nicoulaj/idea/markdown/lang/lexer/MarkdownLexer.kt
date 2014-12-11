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
package net.nicoulaj.idea.markdown.lang.lexer

import net.nicoulaj.idea.markdown.lang.IElementType

import java.io.IOException
import java.io.Reader

import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes

public class MarkdownLexer(public val originalText: String) {

    private val baseLexer: _MarkdownLexer

    public var type: IElementType? = null
        private set
    private var nextType: IElementType? = null

    public var tokenStart: Int = 0
        private set
    public var tokenEnd: Int = 0
        private set

    {
        baseLexer = _MarkdownLexer(null : Reader?)
        baseLexer.reset(originalText, 0, originalText.length(), 0)

        init()
    }

    public fun advance(): Boolean {
        return locateToken()
    }

    private fun init() {
        type = advanceBase()
        tokenStart = baseLexer.getTokenStart()

        calcNextType()
    }

    private fun locateToken(): Boolean {
        `type` = nextType
        tokenStart = tokenEnd
        if (`type` == null) {
            return false
        }

        calcNextType()
        return true
    }

    private fun calcNextType() {
        do {
            tokenEnd = baseLexer.getTokenEnd()
            nextType = advanceBase()
        } while (nextType == `type` && TOKENS_TO_MERGE.contains(`type`))
    }

    private fun advanceBase(): IElementType? {
        try {
            return baseLexer.advance()
        } catch (e: IOException) {
            e.printStackTrace()
            throw AssertionError("This could not be!")
        }

    }

    class object {
        private val TOKENS_TO_MERGE = setOf(
                MarkdownTokenTypes.TEXT,
                MarkdownTokenTypes.WHITE_SPACE,
                MarkdownTokenTypes.CODE,
                MarkdownTokenTypes.HTML_BLOCK,
                MarkdownTokenTypes.LINK_ID,
                MarkdownTokenTypes.LINK_TITLE,
                MarkdownTokenTypes.URL,
                MarkdownTokenTypes.AUTOLINK,
                MarkdownTokenTypes.EMAIL_AUTOLINK,
                MarkdownTokenTypes.BAD_CHARACTER)
    }
}
