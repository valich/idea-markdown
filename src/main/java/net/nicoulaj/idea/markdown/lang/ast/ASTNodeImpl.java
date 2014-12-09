package net.nicoulaj.idea.markdown.lang.ast;

import net.nicoulaj.idea.markdown.lang.IElementType;
import org.jetbrains.annotations.NotNull;

abstract class ASTNodeImpl implements ASTNode {
    @NotNull
    private final IElementType type;
    private final int startOffset;
    private final int endOffset;

    public ASTNodeImpl(@NotNull IElementType type, int startOffset, int endOffset) {
        this.type = type;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @NotNull @Override public IElementType getType() {
        return type;
    }

    @Override public int getStartOffset() {
        return startOffset;
    }

    @Override public int getEndOffset() {
        return endOffset;
    }
}
