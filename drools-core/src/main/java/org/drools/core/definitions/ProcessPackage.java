package org.drools.core.definitions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;

public interface ProcessPackage extends KiePluginPackage {

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
