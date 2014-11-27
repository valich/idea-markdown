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
package net.nicoulaj.idea.markdown.lang.parser.markerblocks.inline;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Consumer;
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.InlineHangableMarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.InlineMarkerManager;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.MarkerProcessor;
import org.jetbrains.annotations.NotNull;

public class LinkDefinitionMarkerBlock extends InlineHangableMarkerBlock {

    private final LinkLabelMarkerBlock linkLabel;
    private LinkDestinationMarkerBlock linkDestination = null;
    private LinkTitleMarkerBlock linkTitle = null;

    @NotNull
    private MyState state;

    public LinkDefinitionMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                     @NotNull PsiBuilder.Marker marker,
                                     @NotNull InlineMarkerManager markerManager,
                                     @NotNull LinkLabelMarkerBlock linkLabel) {
        super(myConstraints, marker, markerManager);
        this.linkLabel = linkLabel;
        state = MyState.PARSE_LABEL;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull final PsiBuilder builder,
                                                                 @NotNull MarkdownConstraints currentConstraints) {
        if (state == MyState.PARSE_LABEL && linkLabel.getState() != State.PARSING) {
            if (linkLabel.getState() == State.DROPPED
                || tokenType != MarkdownTokenTypes.COLON) {
                return DROP_ACTION;
            }

            state = MyState.PARSE_DESTINATION;
        }
        else if (state == MyState.PARSE_DESTINATION) {
            if (linkDestination == null) {
                final ProcessingResult result =
                        ProcessingResult.PASS.withCustomAction(new Consumer<MarkerProcessor>() {
                            @Override public void consume(MarkerProcessor markerProcessor) {
                                linkDestination = new LinkDestinationMarkerBlock(myConstraints, builder, markerManager);
                                markerProcessor.addNewMarkerBlock(linkDestination);
                            }
                        });

                if (tokenType == MarkdownTokenTypes.EOL) {
                    if (builder.lookAhead(1) == MarkdownTokenTypes.EOL) {
                        return DROP_ACTION;
                    }

                    return result.postpone();
                } else {
                    return result;
                }
            }
            else if (linkDestination.getState() != State.PARSING) {
                if (linkDestination.getState() == State.DROPPED) {
                    return DROP_ACTION;
                }
                state = MyState.PARSE_TITLE;
            }
        }
        if (state == MyState.PARSE_TITLE) {
            if (linkTitle == null) {
                final ProcessingResult result =
                        ProcessingResult.PASS.withCustomAction(new Consumer<MarkerProcessor>() {
                            @Override public void consume(MarkerProcessor markerProcessor) {
                                linkTitle = new LinkTitleMarkerBlock(myConstraints, builder, markerManager);
                                markerProcessor.addNewMarkerBlock(linkTitle);
                            }
                        });

                if (tokenType == MarkdownTokenTypes.EOL) {
                    if (builder.lookAhead(1) == MarkdownTokenTypes.EOL) {
                        markerManager.cancelMarker(linkDestination);
                        return DROP_ACTION;
                    }

                    return result.postpone();
                } else {
                    return result;
                }
            }
            else if (linkTitle.getState() != State.PARSING) {
                if (linkTitle.getState() == State.DROPPED) {
                    markerManager.cancelMarker(linkDestination);
                    return DROP_ACTION;
                }
                final IElementType nextToken = builder.getTokenType();
                if (nextToken != null && nextToken != MarkdownTokenTypes.EOL) {
                    markerManager.cancelMarker(linkDestination);
                    markerManager.cancelMarker(linkTitle);
                    return DROP_ACTION;
                }

                return DONE_ACTION;
            }
        }

        return ProcessingResult.PASS;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.LINK_DEFINITION;
    }

    private static enum MyState {
        PARSE_LABEL,
        PARSE_DESTINATION,
        PARSE_TITLE
    }
}
