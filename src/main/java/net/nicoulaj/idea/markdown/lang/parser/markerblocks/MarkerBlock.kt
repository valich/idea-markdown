/*
 * Copyright (c) 2011-2014 Julien Nicoulaud <julien.nicoulaud@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.nicoulaj.idea.markdown.lang.parser.markerblocks

import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder
import net.nicoulaj.idea.markdown.lang.parser.TokensCache

public trait MarkerBlock {

    public fun processToken(tokenType: IElementType, builder: TokensCache.Iterator, currentConstraints: MarkdownConstraints): ProcessingResult

    public fun getBlockConstraints(): MarkdownConstraints

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    public fun acceptAction(action: ClosingAction): Boolean

    public enum class ClosingAction {
        DONE {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                marker.done(type)
            }
        }
        DROP {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        }
        DEFAULT {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                throw UnsupportedOperationException("Should not be invoked")
            }
        }
        NOTHING {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        }

        public abstract fun doAction(marker: ProductionHolder.Marker, `type`: IElementType)
    }

    public enum class EventAction {
        PROPAGATE
        CANCEL
    }

    class object {


        public fun ProcessingResult(childrenAction: ClosingAction, selfAction: ClosingAction, eventAction: EventAction): ProcessingResult {
            return ProcessingResult(childrenAction, selfAction, eventAction, false)
        }

        public class ProcessingResult internal (public val childrenAction: ClosingAction,
                                                public val selfAction: ClosingAction,
                                                public val eventAction: EventAction,
                                                public val isPostponed: Boolean) {

            public fun postpone(): ProcessingResult {
                if (isPostponed) {
                    return this
                }

                return ProcessingResult(childrenAction, selfAction, eventAction, true)
            }

            class object {
                public val PASS: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE)
                public val CANCEL: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL)
                public val DEFAULT: ProcessingResult = ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.PROPAGATE)
            }

        }
    }

}
