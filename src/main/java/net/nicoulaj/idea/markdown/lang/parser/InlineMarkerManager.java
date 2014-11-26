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
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InlineMarkerManager {
    Logger LOG = Logger.getInstance(InlineMarkerManager.class);

    @NotNull
    private final Map<InlineHangableMarkerBlock, MarkerInfo> markers = ContainerUtil.newHashMap();
    @NotNull
    private final PsiBuilder builder;

    public InlineMarkerManager(@NotNull PsiBuilder builder) {
        this.builder = builder;
    }

    public void openMarkerBlock(@NotNull InlineHangableMarkerBlock markerBlock) {
        LOG.assertTrue(!markers.containsKey(markerBlock));

        markers.put(markerBlock, new MarkerInfo(markerBlock, builder.getCurrentOffset()));
    }

    public void doneMarker(@NotNull InlineHangableMarkerBlock markerBlock) {
        final MarkerInfo markerInfo = markers.get(markerBlock);
        LOG.assertTrue(markerInfo != null);

        markerInfo.endVirtually(builder);
    }

    public void cancelMarkersInterlappingWith(int start, int end) {

        TextRange range = TextRange.create(start, end);

        Set<InlineHangableMarkerBlock> toDelete = ContainerUtil.newHashSet();
        for (MarkerInfo info : markers.values()) {
            if (range.contains(info.getStart()) ^ range.containsOffset(info.getEnd())) {
                toDelete.add(info.getMarkerBlock());
            }
        }

        for (InlineHangableMarkerBlock key : toDelete) {
            markers.remove(key);
        }
    }

    public Collection<InlineHangableMarkerBlock> getOpenedMarkers() {
        Collection<InlineHangableMarkerBlock> result = ContainerUtil.newArrayList();
        for (MarkerInfo info : markers.values()) {
            if (info.getEnd() == -1) {
                result.add(info.getMarkerBlock());
            }
        }

        return result;
    }

    public void flushAllClosedMarkers() {
        List<Event> processingList = ContainerUtil.newArrayList();
        for (MarkerInfo info : markers.values()) {
            processingList.add(new Event(info.getStart(), info));
            if (info.getEnd() != -1) {
                processingList.add(new Event(info.getEnd(), info));
            }
        }
        Collections.sort(processingList);

        for (int i = processingList.size() - 1; i >= 0; --i) {
            final Event event = processingList.get(i);

            final MarkerInfo info = event.markerInfo;
            if (info.getEnd() == -1) {
                info.getMarkerBlock().acceptAction(MarkerBlock.ClosingAction.DROP);
                info.getMarkerBlock().getMarker().drop();
            } else if (event.isStart()) {
                info.endPhysically();
            } else {
//                info.endMarker.drop();
            }
        }

        markers.clear();
    }

    private static class MarkerInfo {
        private final InlineHangableMarkerBlock markerBlock;
        private final int start;
        private int end;
        private PsiBuilder.Marker endMarker;

        public MarkerInfo(@NotNull InlineHangableMarkerBlock markerBlock, int start) {
            this.markerBlock = markerBlock;
            this.start = start;
            this.end = -1;
            this.endMarker = null;
        }

        public void endVirtually(@NotNull PsiBuilder builder) {
            this.end = builder.getCurrentOffset();
            endMarker = builder.mark();
        }

        public void endPhysically() {
            markerBlock.getMarker().doneBefore(markerBlock.getDefaultNodeType(), endMarker);
            endMarker.drop();
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public InlineHangableMarkerBlock getMarkerBlock() {
            return markerBlock;
        }
    }

    private static class Event implements Comparable<Event> {
        final int position;
        final MarkerInfo markerInfo;

        public Event(int position, MarkerInfo markerInfo) {
            this.position = position;
            this.markerInfo = markerInfo;
        }

        private boolean isStart() {
            return markerInfo.getStart() == position;
        }

        @Override public int compareTo(Event o) {
            if (position != o.position) {
                return position - o.position;
            }
            if (isStart() == o.isStart()) {
                return 0;
            }
            return isStart() ? 1 : -1;
        }
    }
}
