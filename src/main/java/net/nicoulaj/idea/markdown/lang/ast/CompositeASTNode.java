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
package net.nicoulaj.idea.markdown.lang.ast;

import net.nicoulaj.idea.markdown.lang.IElementType;
import org.jetbrains.annotations.NotNull;

public class CompositeASTNode extends ASTNodeImpl {
    @NotNull
    private final ASTNode[] children;

    public CompositeASTNode(@NotNull IElementType type, @NotNull ASTNode[] children) {
        super(type, children[0].getStartOffset(), children[children.length - 1].getEndOffset());
        this.children = children;
    }

    @NotNull @Override public ASTNode[] getChildren() {
        return children;
    }
}
