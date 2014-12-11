package net.nicoulaj.idea.markdown.lang.ast

import net.nicoulaj.idea.markdown.lang.IElementType

public class CompositeASTNode(type: IElementType, override val children: List<ASTNode>)
    : ASTNodeImpl(type, children[0].startOffset, children[children.size() - 1].endOffset)
