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
package net.nicoulaj.idea.markdown.lang.parser.dialects;

import com.intellij.openapi.util.Pair;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkerProcessor;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlockImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class FixedPriorityListMarkerProcessor extends MarkerProcessor {
    @NotNull
    private final Map<IElementType, Integer> priorityMap;

    public FixedPriorityListMarkerProcessor(@NotNull MarkdownConstraints startingConstraints) {
        super(startingConstraints);

        final List<Pair<IElementType, Integer>> priorityList = getPriorityList();

        priorityMap = new IdentityHashMap<IElementType, Integer>();
        for (Pair<IElementType, Integer> pair : priorityList) {
            priorityMap.put(pair.first, pair.second);
        }
    }

    protected abstract List<Pair<IElementType, Integer>> getPriorityList();

    @NotNull @Override protected List<Integer> getPrioritizedMarkerPermutation() {
        final List<MarkerBlock> markersStack = getMarkersStack();

        List<Integer> result = new ArrayList<Integer>(markersStack.size());
        for (int i = 0; i < markersStack.size(); ++i) {
            result.add(i);
        }

        Collections.sort(result, new Comparator<Integer>() {
            @Override public int compare(@NotNull Integer o1, @NotNull Integer o2) {
                if (priorityMap.isEmpty()) {
                    return o2 - o1;
                }

                final MarkerBlock block1 = markersStack.get(o1);
                final MarkerBlock block2 = markersStack.get(o2);

                final int diff = getPriority(block1) - getPriority(block2);
                if (diff != 0) {
                    return -diff;
                }
                return o2 - o1;
            }
        });
        return result;
    }

    private Integer getPriority(MarkerBlock block) {
        if (!(block instanceof MarkerBlockImpl)) {
            return 0;
        }

        final IElementType type = ((MarkerBlockImpl) block).getDefaultNodeType();
        if (priorityMap.containsKey(type)) {
            return priorityMap.get(type);
        }

        return 0;
    }

}
