package net.nicoulaj.idea.markdown.lang.parser

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.sequentialparsers.SequentialParser

import java.util.ArrayList

public class ProductionHolder {
    public var currentPosition: Int = 0
        private set

    private val _production : MutableCollection<SequentialParser.Node> = ArrayList()
    public val production: Collection<SequentialParser.Node>
        get() {
            return _production
        }

    public fun updatePosition(position: Int) {
        currentPosition = position
    }

    public fun addProduction(nodes: Collection<SequentialParser.Node>) {
        _production.addAll(nodes)
    }

    public fun mark(): Marker {
        return Marker()
    }

    public inner class Marker {
        private val startPos: Int

        {
            startPos = currentPosition
        }

        public fun done(`type`: IElementType) {
            _production.add(SequentialParser.Node(startPos..currentPosition, type))
        }
    }
}
