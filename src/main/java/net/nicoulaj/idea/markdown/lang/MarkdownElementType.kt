package net.nicoulaj.idea.markdown.lang

public class MarkdownElementType(name: String) : IElementType(name) {

    override fun toString(): String {
        return "Markdown:" + super.toString()
    }
}
