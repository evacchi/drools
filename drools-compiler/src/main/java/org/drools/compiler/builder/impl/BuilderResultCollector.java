/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.compiler.builder.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.compiler.compiler.BaseKnowledgeBuilderResultImpl;
import org.kie.internal.builder.KnowledgeBuilderResult;

public class BuilderResultCollector {

    private final List<KnowledgeBuilderResult> results = new ArrayList<>();

    public void add(KnowledgeBuilderResult result) {
        results.add(result);
    }

    public Collection<KnowledgeBuilderResult> get() {
        return results;
    }

    public void clear() {
        results.clear();
    }

    public void addAll(List<BaseKnowledgeBuilderResultImpl> errors) {
        results.addAll(errors);
    }
}
