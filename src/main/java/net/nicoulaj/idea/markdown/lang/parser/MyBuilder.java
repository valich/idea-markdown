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

import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.ast.ASTNode;
import net.nicoulaj.idea.markdown.lang.ast.CompositeASTNode;
import net.nicoulaj.idea.markdown.lang.ast.LeafASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MyBuilder {
    @NotNull
    private final Collection<SequentialParser.Node> production;

    public MyBuilder() {
        production = new ArrayList<SequentialParser.Node>();
    }

    public void addNode(@NotNull SequentialParser.Node node) {
        production.add(node);
    }

    public void addNodes(@NotNull Collection<SequentialParser.Node> nodes) {
        production.addAll(nodes);
    }

    @NotNull
    public ASTNode buildTree(@NotNull TokensCache tokensCache) {
        final List<MyEvent> events = constructEvents();
        final Stack<List<MyASTNodeWrapper>> markersStack = new Stack<List<MyASTNodeWrapper>>();

        assert !events.isEmpty() : "nonsense";
        assert events.get(0).info == events.get(events.size() - 1).info : "more than one root?";

        int currentTokenPosition = events.get(0).position;

        for (int i = 0; i < events.size(); i++) {
            final MyEvent event = events.get(i);

            while (currentTokenPosition < event.position) {
                flushOneTokenToTree(tokensCache, markersStack, currentTokenPosition);
                currentTokenPosition++;
            }

            if (event.isStart()) {
                markersStack.push(new ArrayList<MyASTNodeWrapper>());
            } else {
                final List<MyASTNodeWrapper> currentNodeChildren = markersStack.pop();
                final boolean isTopmostNode = markersStack.isEmpty();

                final MyASTNodeWrapper newNode = createASTNodeOnClosingEvent(tokensCache, event, currentNodeChildren, isTopmostNode);

                if (isTopmostNode) {
                    assert i + 1 == events.size();
                    return newNode.getAstNode();
                }
                else {
                    markersStack.peek().add(newNode);
                }
            }
        }

        throw new AssertionError("markers stack should close some time thus would not be here!");
    }

    private void flushOneTokenToTree(TokensCache tokensCache, Stack<List<MyASTNodeWrapper>> markersStack, int currentTokenPosition) {
        TokensCache.Iterator iterator = tokensCache.new Iterator(currentTokenPosition);
        assert iterator.getType() != null;
        final LeafASTNode node = new LeafASTNode(iterator.getType(), iterator.getStart(), iterator.getEnd());
        markersStack.peek().add(new MyASTNodeWrapper(node, iterator.getIndex(), iterator.getIndex() + 1));
    }

    private List<MyEvent> constructEvents() {
        List<MyEvent> events = new ArrayList<MyEvent>();
        for (SequentialParser.Node result : production) {
            final int startTokenId = result.range.getStartOffset();
            final int endTokenId = result.range.getEndOffset();

            events.add(new MyEvent(startTokenId, result));
            events.add(new MyEvent(endTokenId, result));
        }
        Collections.sort(events);
        return events;
    }

    @NotNull
    private static MyASTNodeWrapper createASTNodeOnClosingEvent(@NotNull TokensCache tokensCache,
                                                                @NotNull MyEvent event,
                                                                @NotNull List<MyASTNodeWrapper> currentNodeChildren,
                                                                boolean isTopmostNode) {
        final ASTNode newNode;

        final IElementType type = event.info.type;
        final int startTokenId = event.info.range.getStartOffset();
        final int endTokenId = event.info.range.getEndOffset();

        final List<ASTNode> childrenWithWhitespaces = new ArrayList<ASTNode>(currentNodeChildren.size());

        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, startTokenId, -1, -1);
        }
        for (int i = 1; i < currentNodeChildren.size(); ++i) {
            final MyASTNodeWrapper prev = currentNodeChildren.get(i - 1);
            final MyASTNodeWrapper next = currentNodeChildren.get(i);

            childrenWithWhitespaces.add(prev.getAstNode());

            addRawTokens(tokensCache, childrenWithWhitespaces, prev.getEndTokenIndex() - 1, +1,
                         tokensCache.new Iterator(next.getStartTokenIndex()).getStart());
        }
        childrenWithWhitespaces.add(currentNodeChildren.get(currentNodeChildren.size() - 1).getAstNode());
        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, endTokenId, +1, -1);
        }

        newNode = new CompositeASTNode(type, childrenWithWhitespaces.toArray(new ASTNode[childrenWithWhitespaces.size()]));
        return new MyASTNodeWrapper(newNode, startTokenId, endTokenId);
    }

    private static void addRawTokens(TokensCache tokensCache, List<ASTNode> childrenWithWhitespaces, int from, int dx, int exitOffset) {
        final TokensCache.Iterator iterator = tokensCache.new Iterator(from);
        int rawIdx = 0;
        while (iterator.rawLookup(rawIdx + dx) != null && iterator.rawStart(rawIdx + dx) != exitOffset) {
            rawIdx = rawIdx + dx;
        }
        while (rawIdx != 0) {
            final IElementType rawType = iterator.rawLookup(rawIdx);
            assert rawType != null;
            childrenWithWhitespaces.add(new LeafASTNode(rawType, iterator.rawStart(rawIdx), iterator.rawStart(rawIdx + 1)));
            rawIdx = rawIdx - dx;
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

    private static class MyASTNodeWrapper {
        private final ASTNode astNode;
        private final int startTokenIndex;
        private final int endTokenIndex;

        public MyASTNodeWrapper(ASTNode astNode, int startTokenIndex, int endTokenIndex) {
            this.astNode = astNode;
            this.startTokenIndex = startTokenIndex;
            this.endTokenIndex = endTokenIndex;
        }

        public ASTNode getAstNode() {
            return astNode;
        }

        public int getStartTokenIndex() {
            return startTokenIndex;
        }

        public int getEndTokenIndex() {
            return endTokenIndex;
        }
    }

}
