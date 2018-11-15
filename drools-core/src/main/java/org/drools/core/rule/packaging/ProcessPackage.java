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

package org.drools.core.rule.packaging;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kie.api.definition.process.Process;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.Resource;

public interface ProcessPackage extends ResourceTypePackage {

    Collection<Process> getProcesses();

    String getName();

    ClassLoader getPackageClassLoader();

    void addProcess(Process process);

    Map<String, Process> getRuleFlows();

    void removeRuleFlow(String id);

    String toString();

    void setError(String summary);

    void resetErrors();

    boolean isValid();

    void checkValidity();

    String getErrorSummary();

    void clear();

    List<Process> removeProcessesGeneratedFromResource(Resource resource);

    void removeProcess(Process process);

    List<Process> getProcessesGeneratedFromResource(Resource resource);
}
