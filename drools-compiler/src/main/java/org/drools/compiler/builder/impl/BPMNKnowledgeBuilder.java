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

import org.drools.compiler.compiler.BPMN2ProcessFactory;
import org.kie.api.io.Resource;

public class BPMNKnowledgeBuilder extends ProcessKnowledgeBuilder {

    public BPMNKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {
        super(knowledgeBuilder);
    }

    @Override
    public void addProcessFromXml(Resource resource) {
        BPMN2ProcessFactory.configurePackageBuilder(knowledgeBuilder);
        super.addProcessFromXml(resource);
    }
}
