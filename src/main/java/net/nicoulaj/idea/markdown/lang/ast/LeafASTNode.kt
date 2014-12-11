package net.nicoulaj.idea.markdown.lang.ast

import net.nicoulaj.idea.markdown.lang.IElementType
import java.util.ArrayList

public class LeafASTNode(type: IElementType, startOffset: Int, endOffset: Int) : ASTNodeImpl(type, startOffset, endOffset) {
    override val children: List<ASTNode>
        get() = EMPTY_CHILDREN

    class object {
        private val EMPTY_CHILDREN = ArrayList<ASTNode>(0)
    }
}
