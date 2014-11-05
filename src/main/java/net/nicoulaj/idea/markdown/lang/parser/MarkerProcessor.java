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

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class MarkerProcessor {

    private final static Logger LOG = Logger.getInstance(MarkerProcessor.class);

    protected MarkerBlock[] NO_BLOCKS = new MarkerBlock[0];

    @NotNull
    private final List<MarkerBlock> markersStack = new ArrayList<MarkerBlock>();

    @NotNull
    private final TreeMap<Integer, MarkerBlock.ProcessingResult> postponedActions = ContainerUtil.newTreeMap();

    @Nullable
    private List<Integer> cachedPermutation = null;

    @NotNull
    private final MarkdownConstraints startConstraints;

    @NotNull
    private MarkdownConstraints topBlockConstraints;

    @NotNull
    private MarkdownConstraints currentConstraints;

    public MarkerProcessor(@NotNull MarkdownConstraints startingConstraints) {
        this.startConstraints = this.topBlockConstraints = this.currentConstraints = startingConstraints;
    }

    @NotNull
    protected abstract List<Integer> getPrioritizedMarkerPermutation();

    @NotNull
    public abstract MarkerBlock[] createNewMarkerBlocks(@NotNull IElementType tokenType, @NotNull PsiBuilder builder, @NotNull MarkerProcessor markerProcessor);

    @NotNull public List<MarkerBlock> getMarkersStack() {
        return markersStack;
    }

    @NotNull public MarkdownConstraints getCurrentConstraints() {
        return currentConstraints;
    }

    public void processToken(@NotNull IElementType tokenType, @NotNull PsiBuilder builder) {
        processPostponedActions();

        final boolean someoneHasCancelledEvent = processMarkers(tokenType, builder);
        if (!someoneHasCancelledEvent) {
            final MarkerBlock[] newMarkerBlocks = createNewMarkerBlocks(tokenType, builder, this);
            for (MarkerBlock newMarkerBlock : newMarkerBlocks) {
                markersStack.add(newMarkerBlock);
                topBlockConstraints = newMarkerBlock.getBlockConstraints();
                cachedPermutation = null;
            }
            currentConstraints = topBlockConstraints;
        }

        if (tokenType == MarkdownTokenTypes.EOL) {
            // Eat "duplicating" block tokens (blockquote, lists..)
            // Since ended blocks are dead after EOL, top block's constraints are prefix of the new one.
            currentConstraints = passDuplicatingTokensAndGetCurrentConstraints(builder);
        }
    }

    private void processPostponedActions() {
        while (!postponedActions.isEmpty()) {
            final Map.Entry<Integer, MarkerBlock.ProcessingResult> lastEntry = postponedActions.pollLastEntry();

            final Integer stackIndex = lastEntry.getKey();
            applyProcessingResult(stackIndex, markersStack.get(stackIndex), lastEntry.getValue());
        }
    }


    public void flushMarkers() {
        closeChildren(-1, MarkerBlock.ClosingAction.DEFAULT);
    }

    private MarkdownConstraints passDuplicatingTokensAndGetCurrentConstraints(@NotNull PsiBuilder builder) {
        LOG.assertTrue(builder.getTokenType() == MarkdownTokenTypes.EOL);

        MarkdownConstraints constraints = startConstraints;
        int toSkip = 0;

        for (int rawIndex = 1;; rawIndex++) {
            final IElementType type = builder.rawLookup(rawIndex);
            if (type == null) {
                break;
            }

            final MarkdownConstraints next;
            if (type == MarkdownTokenTypes.WHITE_SPACE) {
                next = constraints.fillImplicitsOnWhiteSpace(builder, rawIndex, topBlockConstraints);
            }
            else if (MarkdownConstraints.isConstraintType(type)) {
                next = constraints.addModifier(type, builder, rawIndex);
            }
            else {
                break;
            }

            if (next.upstreamWith(topBlockConstraints)) {
                constraints = next;
                if (type != MarkdownTokenTypes.WHITE_SPACE) {
                    toSkip++;
                }
            }
            else {
                break;
            }
        }

        for (int i = 0; i < toSkip; ++i) {
            builder.advanceLexer();
        }
        return constraints;
    }

    /**
     * @return true if some markerBlock has canceled the event, false otherwise
     */
    private boolean processMarkers(IElementType tokenType, PsiBuilder builder) {
        if (cachedPermutation == null) {
            cachedPermutation = getPrioritizedMarkerPermutation();
        }

        try {
            for (Integer index : cachedPermutation) {
                if (index >= markersStack.size()) {
                    continue;
                }

                final MarkerBlock markerBlock = markersStack.get(index);
                final MarkerBlock.ProcessingResult processingResult = markerBlock.processToken(tokenType, builder, topBlockConstraints);

                if (processingResult.isPostponed()) {
                    postponedActions.put(index, processingResult);
                }
                else {
                    if (processingResult == MarkerBlock.ProcessingResult.PASS) {
                        continue;
                    }

                    applyProcessingResult(index, markerBlock, processingResult);
                }

                if (processingResult.eventAction == MarkerBlock.EventAction.CANCEL) {
                    return true;
                }
            }

            return false;
        }
        finally {
            // Stack was changed
            if (markersStack.size() != cachedPermutation.size()) {
                cachedPermutation = null;
                //noinspection ConstantConditions
                topBlockConstraints = markersStack.isEmpty()
                                        ? startConstraints
                                        : ContainerUtil.getLastItem(markersStack).getBlockConstraints();
            }
        }

    }

    private void applyProcessingResult(Integer index, MarkerBlock markerBlock, MarkerBlock.ProcessingResult processingResult) {
        closeChildren(index, processingResult.childrenAction);

        // process self
        if (markerBlock.acceptAction(processingResult.selfAction)) {
            markersStack.remove((int) index);
        }
    }

    private void closeChildren(int index, @NotNull MarkerBlock.ClosingAction childrenAction) {
        if (childrenAction != MarkerBlock.ClosingAction.NOTHING) {
            for (int latterIndex = markersStack.size() - 1; latterIndex > index; --latterIndex) {
                if (postponedActions.containsKey(latterIndex)) {
                    LOG.warn("Processing postponed marker block :(");
                    postponedActions.remove(latterIndex);
                }

                final boolean result = markersStack.get(latterIndex).acceptAction(childrenAction);
                assert result : "If closing action is not NOTHING, marker should be gone";

                markersStack.remove(latterIndex);
            }
        }
    }
}
