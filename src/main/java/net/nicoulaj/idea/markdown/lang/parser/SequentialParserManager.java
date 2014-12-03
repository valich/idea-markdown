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
package net.nicoulaj.idea.markdown.lang.parser;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SequentialParserManager {
    @NotNull
    protected List<SequentialParser> getParserSequence() {
        return ContainerUtil.list(
                new AutolinkParser(),
                new BacktickParser(),
                new LinkDefinitionParser(),
                new InlineLinkParser(),
                new EmphStrongParser()
        );
    }

    @NotNull
    public Collection<SequentialParser.Node> runParsingSequence(@NotNull TokensCache tokensCache,
                                                                @NotNull Collection<TextRange> rangesToParse) {
        final Collection<SequentialParser.Node> result = ContainerUtil.newArrayList();

        Collection<Collection<TextRange>> parsingSpaces = ContainerUtil.newArrayList();
        parsingSpaces.add(rangesToParse);

        for (SequentialParser sequentialParser : getParserSequence()) {
            Collection<Collection<TextRange>> nextLevelSpaces = new ArrayList<Collection<TextRange>>();

            for (Collection<TextRange> parsingSpace : parsingSpaces) {
                SequentialParser.ParsingResult currentResult = sequentialParser.parse(tokensCache, parsingSpace);
                result.addAll(currentResult.parsedNodes);
                nextLevelSpaces.addAll(currentResult.rangesToProcessFurther);
            }

            parsingSpaces = nextLevelSpaces;
        }

        return result;
    }
}
