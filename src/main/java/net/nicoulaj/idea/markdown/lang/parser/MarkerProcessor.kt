package net.nicoulaj.idea.markdown.lang.parser

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes
import net.nicoulaj.idea.markdown.lang.parser.markerblocks.MarkerBlock
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParser

import java.util.*

public abstract class MarkerProcessor(startingConstraints: MarkdownConstraints) {

    protected var NO_BLOCKS: Array<MarkerBlock> = array()

    protected val markersStack: MutableList<MarkerBlock> = ArrayList()

    private val postponedActions = TreeMap<Int, MarkerBlock.ProcessingResult>()

    private val productionHolder = ProductionHolder()

    public var tokensCache: TokensCache? = null

    private var cachedPermutation: List<Int>? = null

    private val startConstraints: MarkdownConstraints

    private var topBlockConstraints: MarkdownConstraints

    public var currentConstraints: MarkdownConstraints
        private set

    {
        startConstraints = startingConstraints
        topBlockConstraints = startingConstraints
        $currentConstraints = startingConstraints
    }

    protected abstract fun getPrioritizedMarkerPermutation(): List<Int>

    public abstract fun createNewMarkerBlocks(tokenType: IElementType, iterator: TokensCache.Iterator, productionHolder: ProductionHolder): Array<MarkerBlock>

    public fun getProduction(): Collection<SequentialParser.Node> {
        return productionHolder.production
    }

    public fun processToken(tokenType: IElementType, iterator: TokensCache.Iterator): TokensCache.Iterator {
        var it = iterator
        productionHolder.updatePosition(it.index)
        processPostponedActions()

        val someoneHasCancelledEvent = processMarkers(tokenType, it)
        if (!someoneHasCancelledEvent) {
            val newMarkerBlocks = createNewMarkerBlocks(tokenType, it, productionHolder)
            for (newMarkerBlock in newMarkerBlocks) {
                addNewMarkerBlock(newMarkerBlock)
            }
        }

        if (tokenType == MarkdownTokenTypes.EOL) {
            // Eat "duplicating" block tokens (blockquote, lists..)
            // Since ended blocks are dead after EOL, top block's constraints are prefix of the new one.
            it = passDuplicatingTokens(it)
        }
        return it
    }

    private fun processPostponedActions() {
        while (!postponedActions.isEmpty()) {
            val lastEntry = postponedActions.pollLastEntry()

            val stackIndex = lastEntry.getKey()
            applyProcessingResult(stackIndex, markersStack.get(stackIndex!!), lastEntry.getValue())
        }
    }

    public fun addNewMarkerBlock(newMarkerBlock: MarkerBlock) {
        markersStack.add(newMarkerBlock)
        topBlockConstraints = newMarkerBlock.getBlockConstraints()
        cachedPermutation = null
        currentConstraints = topBlockConstraints
    }

    public fun flushMarkers(iterator: TokensCache.Iterator) {
        productionHolder.updatePosition(iterator.index)
        processPostponedActions()

        closeChildren(-1, MarkerBlock.ClosingAction.DEFAULT)
    }

    private fun passDuplicatingTokens(iterator: TokensCache.Iterator): TokensCache.Iterator {
        var it = iterator
        assert(it.type == MarkdownTokenTypes.EOL)

        var constraints = startConstraints
        var toSkip = 0

        var rawIndex = 1
        while (true) {
            val `type` = it.rawLookup(rawIndex)
            if (`type` == null) {
                break
            }

            val next: MarkdownConstraints
            if (`type` == MarkdownTokenTypes.WHITE_SPACE) {
                next = constraints.fillImplicitsOnWhiteSpace(it, rawIndex, topBlockConstraints)
            } else if (MarkdownConstraints.isConstraintType(`type`)) {
                next = constraints.addModifier(`type`, it, rawIndex)
            } else {
                break
            }

            if (next.upstreamWith(topBlockConstraints)) {
                constraints = next
                if (`type` != MarkdownTokenTypes.WHITE_SPACE) {
                    toSkip++
                }
            } else {
                break
            }
            rawIndex++
        }

        currentConstraints = constraints
        for (i in 0..toSkip - 1) {
            it = it.advance()
        }
        return it
    }

    /**
     * @return true if some markerBlock has canceled the event, false otherwise
     */
    private fun processMarkers(tokenType: IElementType, iterator: TokensCache.Iterator): Boolean {
        if (cachedPermutation == null) {
            cachedPermutation = getPrioritizedMarkerPermutation()
        }

        try {
            val currentPermutation = cachedPermutation!!
            for (index in currentPermutation) {
                if (index >= markersStack.size()) {
                    continue
                }

                val markerBlock = markersStack.get(index)
                val processingResult = markerBlock.processToken(tokenType, iterator, topBlockConstraints)

                if (processingResult.isPostponed) {
                    postponedActions.put(index, processingResult)
                } else {
                    if (processingResult == MarkerBlock.ProcessingResult.PASS) {
                        continue
                    }

                    applyProcessingResult(index, markerBlock, processingResult)
                }

                if (processingResult.eventAction == MarkerBlock.EventAction.CANCEL) {
                    return true
                }
            }

            return false
        } finally {
            // Stack was changed
            if (cachedPermutation == null || markersStack.size() != cachedPermutation!!.size()) {
                cachedPermutation = null
                //noinspection ConstantConditions
                topBlockConstraints = if (markersStack.isEmpty())
                    startConstraints
                else
                    markersStack.last().getBlockConstraints()
            }
        }

    }

    private fun applyProcessingResult(index: Int?, markerBlock: MarkerBlock, processingResult: MarkerBlock.ProcessingResult) {
        closeChildren(index!!, processingResult.childrenAction)

        // process self
        if (markerBlock.acceptAction(processingResult.selfAction)) {
            markersStack.remove(index)
        }
    }

    private fun closeChildren(index: Int, childrenAction: MarkerBlock.ClosingAction) {
        if (childrenAction != MarkerBlock.ClosingAction.NOTHING) {
            var latterIndex = markersStack.size() - 1
            while (latterIndex > index) {
                if (postponedActions.containsKey(latterIndex)) {
                    System.err?.println("Processing postponed marker block :(")
                    postponedActions.remove(latterIndex)
                }

                val result = markersStack.get(latterIndex).acceptAction(childrenAction)
                assert(result, "If closing action is not NOTHING, marker should be gone")

                markersStack.remove(latterIndex)
                --latterIndex
            }
        }
    }

}
