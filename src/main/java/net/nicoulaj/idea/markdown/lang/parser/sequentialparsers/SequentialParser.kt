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

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.TokensCache
import java.util.ArrayList

public trait SequentialParser {

    public fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): ParsingResult

    class object {

        public class Node(public val range: Range<Int>, public val type: IElementType)

        public class ParsingResult {

            private val _parsedNodes : MutableCollection<Node> = ArrayList()
            public val parsedNodes : Collection<Node>
                get() = _parsedNodes

            private val _rangesToProcessFurther : MutableCollection<Collection<Range<Int>>> = ArrayList()
            public val rangesToProcessFurther : Collection<Collection<Range<Int>>>
                get() = _rangesToProcessFurther

            public fun withNode(result: Node): ParsingResult {
                _parsedNodes.add(result)
                return this
            }

            public fun withNodes(parsedNodes: Collection<Node>): ParsingResult {
                _parsedNodes.addAll(parsedNodes)
                return this
            }

            public fun withFurtherProcessing(ranges: Collection<Range<Int>>): ParsingResult {
                _rangesToProcessFurther.add(ranges)
                return this
            }

        }

    }

}
