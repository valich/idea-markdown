package net.nicoulaj.idea.markdown.lang.parser.markerblocks;

import net.nicoulaj.idea.markdown.lang.IElementType;
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints;
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder;
import net.nicoulaj.idea.markdown.lang.parser.TokensCache;
import org.jetbrains.annotations.NotNull;

public interface MarkerBlock {

    @NotNull
    ProcessingResult processToken(@NotNull IElementType tokenType, @NotNull TokensCache.Iterator builder, @NotNull MarkdownConstraints currentConstraints);

    @NotNull
    MarkdownConstraints getBlockConstraints();

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    boolean acceptAction(@NotNull ClosingAction action);

    enum ClosingAction {
        DONE {
            @Override public void doAction(@NotNull ProductionHolder.Marker marker, @NotNull IElementType type) {
                marker.done(type);
            }
        },
        DROP {
            @Override public void doAction(@NotNull ProductionHolder.Marker marker, @NotNull IElementType type) {

            }
        },
        DEFAULT {
            @Override public void doAction(@NotNull ProductionHolder.Marker marker, @NotNull IElementType type) {

            }
        },
        NOTHING {
            @Override public void doAction(@NotNull ProductionHolder.Marker marker, @NotNull IElementType type) {
            }
        };

        public abstract void doAction(@NotNull ProductionHolder.Marker marker, @NotNull IElementType type);
    }

    enum EventAction {
        PROPAGATE,
        CANCEL
    }

    public static class ProcessingResult {
        public static final ProcessingResult PASS = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE);
        public static final ProcessingResult CANCEL = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL);
        public static final ProcessingResult DEFAULT = new ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.PROPAGATE);

        public final ClosingAction childrenAction;
        public final ClosingAction selfAction;
        public final EventAction eventAction;

        public final boolean isPostponed;

        public ProcessingResult(@NotNull ClosingAction childrenAction,
                                @NotNull ClosingAction selfAction,
                                @NotNull EventAction eventAction) {
            this(childrenAction, selfAction, eventAction, false);
        }

        private ProcessingResult(@NotNull ClosingAction childrenAction,
                                 @NotNull ClosingAction selfAction,
                                 @NotNull EventAction eventAction,
                                 boolean isPostponed) {
            this.childrenAction = childrenAction;
            this.selfAction = selfAction;
            this.eventAction = eventAction;
            this.isPostponed = isPostponed;
        }

        @NotNull
        public ProcessingResult postpone() {
            if (isPostponed) {
                return this;
            }

            return new ProcessingResult(childrenAction, selfAction, eventAction, true);
        }

    }

}
