package org.drools.core.process.instance.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.process.instance.TypedWorkItem;

public class TypedWorkItemImpl<T> implements TypedWorkItem<T>,
                                             Serializable {

    private static final long serialVersionUID = 510l;

    private long id;
    private String name;
    private int state = 0;
    private T value;
    private Map<String, Object> results = new HashMap<String, Object>();
    private long processInstanceId;
    private String deploymentId;
    private long nodeInstanceId;
    private long nodeId;

    public TypedWorkItemImpl(T value) {
        this.value = value;
    }

    public TypedWorkItemImpl() {
        this(null);
    }


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setParameters(T parameters) {
        this.value = parameters;
    }

    public T getParameters() {
        return value;
    }

    public void setResults(Map<String, Object> results) {
        if (results != null) {
            this.results = results;
        }
    }

    public void setResult(String name, Object value) {
        results.put(name, value);
    }

    public Object getResult(String name) {
        return results.get(name);
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public long getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(long nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("WorkItem ");
        b.append(id);
        b.append(" [name=");
        b.append(name);
        b.append(", state=");
        b.append(state);
        b.append(", processInstanceId=");
        b.append(processInstanceId);
        b.append(", parameters=");
        b.append(value);
        b.append("]");
        return b.toString();
    }
}
