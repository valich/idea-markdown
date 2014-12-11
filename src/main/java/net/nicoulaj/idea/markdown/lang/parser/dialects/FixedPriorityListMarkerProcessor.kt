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
package net.nicoulaj.idea.markdown.lang.parser.dialects

import com.intellij.openapi.util.Pair
import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.MarkerProcessor
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlockImpl

import java.util.*

public abstract class FixedPriorityListMarkerProcessor(startingConstraints: MarkdownConstraints) : MarkerProcessor(startingConstraints) {
    private val priorityMap: Map<IElementType, Int>

    {
        val priorityList = getPriorityList()

        val _priorityMap = IdentityHashMap<IElementType, Int>()
        for (pair in priorityList) {
            _priorityMap.put(pair.first, pair.second)
        }

        priorityMap = _priorityMap
    }

    protected abstract fun getPriorityList(): List<Pair<IElementType, Int>>

    override fun getPrioritizedMarkerPermutation(): List<Int> {
        val result = ArrayList<Int>(markersStack.size())
        for (i in markersStack.indices) {
            result.add(i)
        }

        Collections.sort<Int>(result, object : Comparator<Int> {
            override fun compare(o1: Int, o2: Int): Int {
                if (priorityMap.isEmpty()) {
                    return o2 - o1
                }

                val block1 = markersStack.get(o1)
                val block2 = markersStack.get(o2)

                val diff = getPriority(block1)!! - getPriority(block2)!!
                if (diff != 0) {
                    return -diff
                }
                return o2 - o1
            }
        })
        return result
    }

    private fun getPriority(block: MarkerBlock): Int? {
        if (block !is MarkerBlockImpl) {
            return 0
        }

        val `type` = (block : MarkerBlockImpl).getDefaultNodeType()
        if (priorityMap.containsKey(`type`)) {
            return priorityMap.get(`type`)
        }

        return 0
    }

}
