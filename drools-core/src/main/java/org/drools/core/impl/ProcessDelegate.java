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

package org.drools.core.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.core.definitions.InternalKnowledgePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.ResourceType;

public class ProcessDelegate {
    private final KnowledgeBaseImpl parentBase;

    public ProcessDelegate(KnowledgeBaseImpl parentBase) {
        this.parentBase = parentBase;
    }

    private ResourceTypePackage<Process> getPackage() {
        InternalKnowledgePackage kiePackage = (InternalKnowledgePackage) parentBase.getKiePackage("$$PROCESS$$");
        return (ResourceTypePackage<Process>) kiePackage.getResourceTypePackages().get(ResourceType.BPMN2);
    }

    public Collection<Process> values() {
        ArrayList<Process> ps = new ArrayList<>();
        for (Process process : getPackage()) {
            ps.add(process);
        }
        return ps;
    }

    public void put(String id, Process process) {
        getPackage().add(process);
    }

    public Process get(String id) {
        return getPackage().lookup(id);
    }

    public void remove(String id) {
        getPackage().remove(id);
    }

}
