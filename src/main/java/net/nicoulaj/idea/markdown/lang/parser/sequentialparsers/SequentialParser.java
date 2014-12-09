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
package net.nicoulaj.idea.markdown.lang.parser.sequentialparsers;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SequentialParser {

    ParsingResult parse(@NotNull TokensCache tokens, @NotNull Collection<TextRange> rangesToGlue);

    static class ParsingResult {
        @NotNull
        public final Collection<Node> parsedNodes;
        @NotNull
        public final Collection<Collection<TextRange>> rangesToProcessFurther;

        public ParsingResult() {
            this(ContainerUtil.<Node>newArrayList(), ContainerUtil.<Collection<TextRange>>newArrayList());
        }

        private ParsingResult(
                @NotNull Collection<Node> parsedNodes,
                @NotNull
                Collection<Collection<TextRange>> rangesToProcessFurther) {
            this.parsedNodes = parsedNodes;
            this.rangesToProcessFurther = rangesToProcessFurther;
        }

        public ParsingResult withNode(@NotNull Node result) {
            parsedNodes.add(result);
            return this;
        }

        public ParsingResult withNodes(@NotNull Collection<Node> parsedNodes) {
            this.parsedNodes.addAll(parsedNodes);
            return this;
        }

        public ParsingResult withFurtherProcessing(@NotNull Collection<TextRange> ranges) {
            rangesToProcessFurther.add(ranges);
            return this;
        }

    }

    static class Node {
        @NotNull
        public final TextRange range;
        @NotNull
        public final IElementType type;

        public Node(@NotNull TextRange range, @NotNull IElementType type) {
            this.range = range;
            this.type = type;
        }
    }

}