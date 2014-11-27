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
import net.nicoulaj.idea.markdown.lang.MarkdownElementTypes;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import net.nicoulaj.idea.markdown.lang.parser.InlineHangableMarkerBlock;
import net.nicoulaj.idea.markdown.lang.parser.InlineMarkerManager;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmphStrongMarkerBlock extends InlineHangableMarkerBlock {

    protected static final char ITALIC = '_';

    protected static final char BOLD = '*';

    private final char myType;

    private final int myGroupEnd;

    @Nullable
    private final EmphStrongMarkerBlock neighborOfMyType;

    private final int numPrevOpened;

    private IElementType myNodeType = MarkdownElementTypes.EMPH;

    public EmphStrongMarkerBlock(@NotNull MarkdownConstraints myConstraints,
                                 @NotNull PsiBuilder builder,
                                 @NotNull InlineMarkerManager markerManager,
                                 @Nullable EmphStrongMarkerBlock prevEmph) {
        super(myConstraints, builder.mark(), MarkdownTokenTypes.EMPH, markerManager);

        final String tokenText = builder.getTokenText();
        assert tokenText != null : "type is not null so text is also?";

        myType = tokenText.charAt(0);
        if (prevEmph != null && prevEmph.myType == myType) {
            neighborOfMyType = prevEmph;
            numPrevOpened = neighborOfMyType.numPrevOpened + 1;
        } else {
            neighborOfMyType = null;
            numPrevOpened = 1;
        }

        if (canOpenEmph(builder, 0, myType == ITALIC)) {
            int numOpened = calcNextOfEqualType(builder, 0);
            myGroupEnd = builder.rawTokenTypeStart(numOpened);
        } else {
            myGroupEnd = -1;
        }
    }

    protected void setStrong() {
        myNodeType = MarkdownElementTypes.STRONG;
    }

    @NotNull @Override protected ProcessingResult doProcessToken(@NotNull IElementType tokenType,
                                                                 @NotNull PsiBuilder builder,
                                                                 @NotNull MarkdownConstraints currentConstraints) {
        LOG.assertTrue(tokenType == MarkdownTokenTypes.EMPH);

        // Invalid marker block
        if (myGroupEnd == -1) {
            return new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DROP, EventAction.PROPAGATE);
        }

        // restricts empty emphs
        if (builder.getCurrentOffset() < myGroupEnd) {
            return ProcessingResult.PASS;
        }

        if (getType(builder, 0) != myType) {
            return ProcessingResult.PASS;
        }
        if (!canCloseEmph(builder, 0, myType == ITALIC)) {
            return ProcessingResult.PASS;
        }

        int numCanBeClosed = calcNumOfClosing(builder);

        final int matching = Math.min(numPrevOpened, numCanBeClosed);
        if (matching % 2 == 0) {
            assert neighborOfMyType != null;
            neighborOfMyType.setStrong();

            return new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DROP, EventAction.CANCEL);
        }
        else {
            return new ProcessingResult(ClosingAction.NOTHING, ClosingAction.DONE, EventAction.CANCEL).postpone();
        }

    }

    private int calcNumOfClosing(PsiBuilder builder) {
        int numCanBeClosed = 1;
        final int nextEqual = calcNextOfEqualType(builder, 0);
        for (int i = 1; i < Math.min(numPrevOpened, nextEqual); ++i) {
            if (canCloseEmph(builder, i, myType == ITALIC)) {
                numCanBeClosed++;
            }
        }
        return numCanBeClosed;
    }

    @NotNull @Override public IElementType getDefaultNodeType() {
        return myNodeType;
    }

    protected static boolean canOpenEmph(@NotNull PsiBuilder builder, int rawIndex, boolean checkPrev) {
        if (builder.rawLookup(rawIndex) != MarkdownTokenTypes.EMPH) {
            return false;
        }

        if (isWhiteSpaceAt(builder, builder.rawTokenTypeStart(rawIndex + 1))) {
            return false;
        }
        if (checkPrev) {
            final int startOffset = builder.rawTokenTypeStart(rawIndex);
            if (startOffset > 0 && Character.isLetterOrDigit(builder.getOriginalText().charAt(startOffset - 1))) {
                return false;
            }
        }
        return builder.rawLookup(rawIndex + 1) != MarkdownTokenTypes.EMPH
               || canOpenEmph(builder, rawIndex + 1, getType(builder, rawIndex + 1) == ITALIC);
    }

    protected static boolean canCloseEmph(@NotNull PsiBuilder builder, int rawIndex, boolean checkNext) {
        if (checkNext) {
            final int nextOffset = builder.rawTokenTypeStart(rawIndex + 1);
            final CharSequence originalText = builder.getOriginalText();

            if (nextOffset < originalText.length() && Character.isLetterOrDigit(originalText.charAt(nextOffset))) {
                return false;
            }
        }

        return !isWhiteSpaceAt(builder, builder.rawTokenTypeStart(rawIndex) - 1);
    }

    protected static boolean isWhiteSpaceAt(@NotNull PsiBuilder builder, int index) {
        final CharSequence originalText = builder.getOriginalText();
        //noinspection SimplifiableIfStatement
        if (index < 0 || index >= originalText.length()) {
            return false;
        }
        return Character.isWhitespace(originalText.charAt(index));
    }

    private static int calcNextOfEqualType(@NotNull PsiBuilder builder, int rawIndex) {
        final char type = getType(builder, rawIndex);
        int answer = 0;

        for (int i = rawIndex; answer < 4; ++i) {
            if (builder.rawLookup(i) != MarkdownTokenTypes.EMPH || getType(builder, i) != type) {
                break;
            }
            answer++;
        }

        return answer;
    }

    protected static int calcSubsequentOfEqualType(@NotNull PsiBuilder builder, int rawIndex) {
        final char type = getType(builder, rawIndex);
        int answer = 0;

        for (int i = rawIndex; answer < 4; ++i) {
            if (builder.rawLookup(i) != MarkdownTokenTypes.EMPH || getType(builder, i) != type) {
                break;
            }
            answer++;
        }
        for (int i = rawIndex - 1; answer < 4; --i) {
            if (builder.rawLookup(i) != MarkdownTokenTypes.EMPH || getType(builder, i) != type) {
                break;
            }
            answer++;
        }
        return answer;
    }

    protected static char getType(@NotNull PsiBuilder builder, int rawIndex) {
        return builder.getOriginalText().charAt(builder.rawTokenTypeStart(rawIndex));
    }
}
