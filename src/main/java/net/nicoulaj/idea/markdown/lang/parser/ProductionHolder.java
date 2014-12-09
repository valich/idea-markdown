package net.nicoulaj.idea.markdown.lang.parser;

import com.intellij.openapi.util.TextRange;
import net.nicoulaj.idea.markdown.lang.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ProductionHolder {
    private int currentPosition = 0;

    private final Collection<SequentialParser.Node> production = new ArrayList<SequentialParser.Node>();

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void updatePosition(int position) {
        currentPosition = position;
    }

    public void addProduction(@NotNull Collection<SequentialParser.Node> nodes) {
        production.addAll(nodes);
    }

    public Marker mark() {
        return new Marker();
    }

    public Collection<SequentialParser.Node> getProduction() {
        return production;
    }

    public class Marker {
        private final int startPos;

        public Marker() {
            startPos = currentPosition;
        }

        public void done(@NotNull IElementType type) {
            production.add(new SequentialParser.Node(TextRange.create(startPos, currentPosition), type));
        }
    }
}
