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
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers

import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.impl.*

import java.util.ArrayList

public class SequentialParserManager {
    protected fun getParserSequence(): List<SequentialParser> {
        return listOf(AutolinkParser(),
                      BacktickParser(),
                      LinkDefinitionParser(),
                      InlineLinkParser(),
                      ReferenceLinkParser(),
                      EmphStrongParser())
    }

    public fun runParsingSequence(tokensCache: TokensCache, rangesToParse: Collection<Range<Int>>): Collection<SequentialParser.Node> {
        val result = ArrayList<SequentialParser.Node>()

        var parsingSpaces = ArrayList<Collection<Range<Int>>>()
        parsingSpaces.add(rangesToParse)

        for (sequentialParser in getParserSequence()) {
            val nextLevelSpaces = ArrayList<Collection<Range<Int>>>()

            for (parsingSpace in parsingSpaces) {
                val currentResult = sequentialParser.parse(tokensCache, parsingSpace)
                result.addAll(currentResult.parsedNodes)
                nextLevelSpaces.addAll(currentResult.rangesToProcessFurther)
            }

            parsingSpaces = nextLevelSpaces
        }

        return result
    }
}
