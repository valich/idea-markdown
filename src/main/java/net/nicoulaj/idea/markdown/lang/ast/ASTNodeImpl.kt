package net.nicoulaj.idea.markdown.lang.ast

import net.nicoulaj.idea.markdown.lang.IElementType

abstract class ASTNodeImpl(override val type: IElementType, override val startOffset: Int, override val endOffset: Int) : ASTNode
