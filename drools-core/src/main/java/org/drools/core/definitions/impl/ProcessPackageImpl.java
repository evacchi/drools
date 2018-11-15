package org.drools.core.definitions.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.rule.packaging.ProcessPackage;
import org.drools.core.rule.InvalidRulePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;

public class ProcessPackageImpl implements ProcessPackage {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.BPMN2;
    }

    /**
     * Name of the pkg.
     */
    private String name;

    private Map<String, Process> ruleFlows;

    /**
     * This is to indicate the the package has no errors during the
     * compilation/building phase
     */
    private boolean valid = true;

    /**
     * This will keep a summary error message as to why this package is not
     * valid
     */
    private String errorSummary;


    public ProcessPackageImpl() {
        this(null);
    }

    /**
     * Construct.
     *
     * @param name The name of this <code>Package</code>.
     */
    public ProcessPackageImpl(final String name) {
        this.name = name;
        this.ruleFlows = Collections.emptyMap();
    }


    @Override
    public Collection<Process> getProcesses() {
        if (getRuleFlows().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<org.kie.api.definition.process.Process> processes = getRuleFlows().values();
        List<Process> list = new ArrayList<Process>(processes.size());
        for (org.kie.api.definition.process.Process process : processes) {
            list.add(process);
        }
        return Collections.unmodifiableCollection(list);
    }



    // ------------------------------------------------------------
    // Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the name of this <code>Package</code>.
     *
     * @return The name of this <code>Package</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ClassLoader getPackageClassLoader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a rule flow to this package.
     */
    @Override
    public void addProcess(Process process) {
        if (this.ruleFlows == Collections.EMPTY_MAP) {
            this.ruleFlows = new HashMap<String, Process>();
        }
        this.ruleFlows.put(process.getId(),
                           process);
    }

    /**
     * Get the rule flows for this package. The key is the ruleflow id. It will
     * be Collections.EMPTY_MAP if none have been added.
     */
    @Override
    public Map<String, Process> getRuleFlows() {
        return this.ruleFlows;
    }

    /**
     * Rule flows can be removed by ID.
     */
    @Override
    public void removeRuleFlow(String id) {
        if (!this.ruleFlows.containsKey(id)) {
            throw new IllegalArgumentException("The rule flow with id [" + id + "] is not part of this package.");
        }
        this.ruleFlows.remove(id);
    }



    // public JavaDialectData getPackageCompilationData() {
    // return this.packageCompilationData;
    // }

    @Override
    public String toString() {
        return "[Package name=" + this.name + "]";
    }

    /**
     * Once this is called, the package will be marked as invalid
     */
    @Override
    public void setError(final String summary) {
        this.errorSummary = summary;
        this.valid = false;
    }

    /**
     * Once this is called, the package will be marked as invalid
     */
    @Override
    public void resetErrors() {
        this.errorSummary = "";
        this.valid = true;
    }

    /**
     * @return true (default) if there are no build/structural problems.
     */
    @Override
    public boolean isValid() {
        return this.valid;
    }

    /**
     * This will throw an exception if the package is not valid
     */
    @Override
    public void checkValidity() {
        if (!isValid()) {
            throw new InvalidRulePackage(this.getErrorSummary());
        }
    }

    /**
     * This will return the error summary (if any) if the package is invalid.
     */
    @Override
    public String getErrorSummary() {
        return this.errorSummary;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || !(object instanceof ProcessPackageImpl)) {
            return false;
        }

        ProcessPackageImpl other = (ProcessPackageImpl) object;

        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public void clear() {
        this.ruleFlows.clear();
    }



    @Override
    public List<Process> removeProcessesGeneratedFromResource(Resource resource) {
        List<Process> processesToBeRemoved = getProcessesGeneratedFromResource(resource);
        for (Process process : processesToBeRemoved) {
            removeProcess(process);
        }
        return processesToBeRemoved;
    }

    @Override
    public void removeProcess(Process process) {
        ruleFlows.remove(process.getId());
    }

    @Override
    public List<Process> getProcessesGeneratedFromResource(Resource resource) {
        List<Process> processesFromResource = new ArrayList<Process>();
        for (Process process : ruleFlows.values()) {
            if (resource.equals(process.getResource())) {
                processesFromResource.add(process);
            }
        }
        return processesFromResource;
    }

}
