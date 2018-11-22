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

package org.drools.compiler.builder.impl.resourcetypes;

import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.drools.compiler.builder.impl.BuilderResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.BaseKnowledgeBuilderResultImpl;
import org.drools.compiler.compiler.ProcessBuilderFactory;
import org.drools.compiler.compiler.ProcessLoadError;
import org.drools.core.io.impl.ReaderResource;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderResult;
import org.kie.internal.builder.ResourceChange;

public abstract class ProcessKnowledgeBuilder {

    private final org.drools.compiler.compiler.ProcessBuilder processBuilder;
    protected final KnowledgeBuilder knowledgeBuilder;
    protected final BuilderResultCollector results;
    private KnowledgeBuilderImpl.AssetFilter assetFilter; // fixme
    private List<Process> processes;

    public ProcessKnowledgeBuilder(
            KnowledgeBuilder knowledgeBuilder) {
        this.processBuilder = ProcessBuilderFactory.newProcessBuilder(knowledgeBuilder);
        this.knowledgeBuilder = knowledgeBuilder;
        this.results = new BuilderResultCollector();
        this.processes = Collections.emptyList();
    }

    public void addProcessFromXml(Resource resource) {
        if (processBuilder == null) {
            throw new RuntimeException("Unable to instantiate a process assembler");
        }

        try {
            List<Process> processes = processBuilder.addProcessFromXml(resource);
            List<BaseKnowledgeBuilderResultImpl> errors = processBuilder.getErrors();
            if (errors.isEmpty()) {
                this.processes = processes.stream()
                        .filter(p -> filterAccepts(ResourceChange.Type.PROCESS, p.getNamespace(), p.getId()))
                        .collect(Collectors.toList());
            } else {
                this.results.addAll(errors);
                errors.clear();
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            this.results.add(new ProcessLoadError(resource, "Unable to load process.", e));
        }
        this.processes = Collections.emptyList();
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public Collection<KnowledgeBuilderResult> getBuilderResults() {
        return results.get();
    }

    boolean filterAccepts(ResourceChange.Type type, String namespace, String name) {
        return assetFilter == null || !KnowledgeBuilderImpl.AssetFilter.Action.DO_NOTHING.equals(assetFilter.accept(type, namespace, name));
    }

    /**
     * Add a ruleflow (.rfm) asset to this package.
     */
    public void addRuleFlow(Reader processSource) {
        addProcessFromXml(processSource);
    }

    public void addProcessFromXml( Reader processSource) {
        addProcessFromXml(new ReaderResource(processSource, ResourceType.DRF));
    }



}
