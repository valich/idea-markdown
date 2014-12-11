package net.nicoulaj.idea.markdown.lang.ast

import net.nicoulaj.idea.markdown.lang.IElementType

public trait ASTNode {
    val type : IElementType
    val startOffset : Int
    val endOffset : Int
    val children : List<ASTNode>
}
