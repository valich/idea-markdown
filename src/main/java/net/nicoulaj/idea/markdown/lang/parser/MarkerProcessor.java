package net.nicoulaj.idea.markdown.lang.parser;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.ContainerUtil;
import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MarkerProcessor {

    private final static Logger LOG = Logger.getInstance(MarkerProcessor.class);

    protected MarkerBlock[] NO_BLOCKS = new MarkerBlock[0];

    @NotNull
    private final List<MarkerBlock> markersStack = new ArrayList<MarkerBlock>();

    @NotNull
    private final TreeMap<Integer, MarkerBlock.ProcessingResult> postponedActions = ContainerUtil.newTreeMap();

    @NotNull
    private final ProductionHolder productionHolder = new ProductionHolder();

    protected TokensCache tokensCache;

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
    public abstract MarkerBlock[] createNewMarkerBlocks(@NotNull IElementType tokenType,
                                                        @NotNull TokensCache.Iterator iterator,
                                                        @NotNull ProductionHolder productionHolder);

    @NotNull public List<MarkerBlock> getMarkersStack() {
        return markersStack;
    }

    @NotNull public MarkdownConstraints getCurrentConstraints() {
        return currentConstraints;
    }

    public Collection<SequentialParser.Node> getProduction() {
        return productionHolder.getProduction();
    }

    public TokensCache.Iterator processToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator iterator) {
        productionHolder.updatePosition(iterator.getIndex());
        processPostponedActions();

        final boolean someoneHasCancelledEvent = processMarkers(tokenType, iterator);
        if (!someoneHasCancelledEvent) {
            final MarkerBlock[] newMarkerBlocks = createNewMarkerBlocks(tokenType, iterator, productionHolder);
            for (MarkerBlock newMarkerBlock : newMarkerBlocks) {
                addNewMarkerBlock(newMarkerBlock);
            }
        }

        if (tokenType == MarkdownTokenTypes.EOL) {
            // Eat "duplicating" block tokens (blockquote, lists..)
            // Since ended blocks are dead after EOL, top block's constraints are prefix of the new one.
            iterator = passDuplicatingTokens(iterator);
        }
        return iterator;
    }

    public void setTokensCache(@NotNull TokensCache tokensCache) {
        this.tokensCache = tokensCache;
    }

    private void processPostponedActions() {
        while (!postponedActions.isEmpty()) {
            final Map.Entry<Integer, MarkerBlock.ProcessingResult> lastEntry = postponedActions.pollLastEntry();

            final Integer stackIndex = lastEntry.getKey();
            applyProcessingResult(stackIndex, markersStack.get(stackIndex), lastEntry.getValue());
        }
    }

    public void addNewMarkerBlock(MarkerBlock newMarkerBlock) {
        markersStack.add(newMarkerBlock);
        topBlockConstraints = newMarkerBlock.getBlockConstraints();
        cachedPermutation = null;
        currentConstraints = topBlockConstraints;
    }

    public void flushMarkers(@NotNull TokensCache.Iterator iterator) {
        productionHolder.updatePosition(iterator.getIndex());
        processPostponedActions();

        closeChildren(-1, MarkerBlock.ClosingAction.DEFAULT);
    }

    private TokensCache.Iterator passDuplicatingTokens(@NotNull TokensCache.Iterator iterator) {
        LOG.assertTrue(iterator.getType() == MarkdownTokenTypes.EOL);

        MarkdownConstraints constraints = startConstraints;
        int toSkip = 0;

        for (int rawIndex = 1;; rawIndex++) {
            final IElementType type = iterator.rawLookup(rawIndex);
            if (type == null) {
                break;
            }

            final MarkdownConstraints next;
            if (type == MarkdownTokenTypes.WHITE_SPACE) {
                next = constraints.fillImplicitsOnWhiteSpace(iterator, rawIndex, topBlockConstraints);
            }
            else if (MarkdownConstraints.isConstraintType(type)) {
                next = constraints.addModifier(type, iterator, rawIndex);
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

        currentConstraints = constraints;
        for (int i = 0; i < toSkip; ++i) {
            iterator = iterator.advance();
        }
        return iterator;
    }

    /**
     * @return true if some markerBlock has canceled the event, false otherwise
     */
    private boolean processMarkers(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator iterator) {
        if (cachedPermutation == null) {
            cachedPermutation = getPrioritizedMarkerPermutation();
        }

        try {
            List<Integer> currentPermutation = cachedPermutation;
            for (Integer index : currentPermutation) {
                if (index >= markersStack.size()) {
                    continue;
                }

                final MarkerBlock markerBlock = markersStack.get(index);
                final MarkerBlock.ProcessingResult processingResult = markerBlock.processToken(tokenType, iterator, topBlockConstraints);

                if (processingResult.isPostponed) {
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
            if (cachedPermutation == null || markersStack.size() != cachedPermutation.size()) {
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
