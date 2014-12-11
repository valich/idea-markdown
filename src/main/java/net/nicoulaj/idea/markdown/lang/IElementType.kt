package net.nicoulaj.idea.markdown.lang

public open class IElementType(public val name: String) {

    public val id: Int

    {
        this.id = numOfRegisteredTypes++
    }

    override fun toString(): String {
        return name
    }

    class object {
        private var numOfRegisteredTypes = 0
    }
}
