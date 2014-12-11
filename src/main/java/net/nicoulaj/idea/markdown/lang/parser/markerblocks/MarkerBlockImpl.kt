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

import com.intellij.openapi.diagnostic.Logger
import net.nicoulaj.idea.markdown.lang.IElementType
import net.nicoulaj.idea.markdown.lang.parser.MarkdownConstraints
import net.nicoulaj.idea.markdown.lang.parser.ProductionHolder
import net.nicoulaj.idea.markdown.lang.parser.TokensCache


public abstract class MarkerBlockImpl(protected val constraints: MarkdownConstraints,
                                      protected val marker: ProductionHolder.Marker,
                                      private val interestingTypes: Set<IElementType>? = null) : MarkerBlock {

    override fun processToken(tokenType: IElementType, builder: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (interestingTypes != null && !interestingTypes.contains(tokenType)) {
            return MarkerBlock.ProcessingResult.PASS
        }
        return doProcessToken(tokenType, builder, currentConstraints)
    }

    override fun getBlockConstraints(): MarkdownConstraints {
        return constraints
    }

    override fun acceptAction(action: MarkerBlock.ClosingAction): Boolean {
        var actionToRun = action
        if (actionToRun == MarkerBlock.ClosingAction.DEFAULT) {
            actionToRun = getDefaultAction()
        }

        actionToRun.doAction(marker, getDefaultNodeType())

        return actionToRun != MarkerBlock.ClosingAction.NOTHING

    }

    protected abstract fun getDefaultAction(): MarkerBlock.ClosingAction

    protected abstract fun doProcessToken(tokenType: IElementType, iterator: TokensCache.Iterator, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult

    public abstract fun getDefaultNodeType(): IElementType

    class object {
        protected val LOG: Logger = Logger.getInstance(javaClass<MarkerBlockImpl>())
    }
}
