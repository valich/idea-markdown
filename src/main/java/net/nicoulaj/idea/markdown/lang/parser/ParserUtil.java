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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ParserUtil {
    @NotNull
    public static List<Integer> textRangesToIndices(@NotNull Collection<TextRange> ranges) {
        List<Integer> result = ContainerUtil.newArrayList();
        for (TextRange range : ranges) {
            for (int i = range.getStartOffset(); i < range.getEndOffset(); ++i) {
                result.add(i);
            }
        }
        ContainerUtil.sort(result);
        return result;
    }

    @NotNull
    public static Collection<TextRange> indicesToTextRanges(@NotNull List<Integer> indices) {
        Collection<TextRange> result = ContainerUtil.newArrayList();

        int starting = 0;
        for (int i = 0; i < indices.size(); ++i) {
            if (i + 1 == indices.size() || indices.get(i) + 1 != indices.get(i + 1)) {
                result.add(TextRange.create(indices.get(starting), indices.get(i) + 1));
                starting = i + 1;
            }
        }

        return result;
    }

    public static boolean isWhitespace(TokensCache.Iterator info, int lookup) {
        IElementType type = info.rawLookup(lookup);
        if (type == null) {
            return false;
        }
        if (type == MarkdownTokenTypes.EOL || type == TokenType.WHITE_SPACE) {
            return true;
        }
        if (lookup == -1) {
            return StringUtil.endsWithChar(info.rollback().getText(), ' ');
        }
        else {
            return StringUtil.startsWithChar(info.advance().getText(), ' ');
        }
    }


    public static void flushMarkers(PsiBuilder builder, Collection<SequentialParser.Node> results, int startPosition, int endPosition) {
        // builder at the startPosition
        List<MyEvent> events = ContainerUtil.newArrayList();
        Stack<PsiBuilder.Marker> markersStack = ContainerUtil.newStack();

        for (SequentialParser.Node result : results) {
            events.add(new MyEvent(result.range.getStartOffset(), result));
            events.add(new MyEvent(result.range.getEndOffset(), result));
        }
        ContainerUtil.sort(events);

        int currentPosition = startPosition;
        for (MyEvent event : events) {
            while (event.position > currentPosition) {
                builder.advanceLexer();
                currentPosition++;
            }

            if (event.isStart()) {
                markersStack.push(builder.mark());
            }
            else {
                markersStack.pop().done(event.info.type);
            }
        }

        while (currentPosition < endPosition) {
            builder.advanceLexer();
            currentPosition++;
        }
    }

    private static class MyEvent implements Comparable<MyEvent> {
        final int position;
        final SequentialParser.Node info;

        public MyEvent(int position, SequentialParser.Node info) {
            this.position = position;
            this.info = info;
        }

        private boolean isStart() {
            return info.range.getStartOffset() == position;
        }

        @Override public int compareTo(@NotNull MyEvent o) {
            if (position != o.position) {
                return position - o.position;
            }
            if (isStart() == o.isStart()) {
                return -(info.range.getStartOffset() + info.range.getEndOffset()
                         -o.info.range.getStartOffset() - o.info.range.getEndOffset());
            }
            return isStart() ? 1 : -1;
        }
    }
}
