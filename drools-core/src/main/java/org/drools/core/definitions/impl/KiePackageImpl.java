/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.definitions.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.common.DroolsObjectInputStream;
import org.drools.core.common.DroolsObjectOutputStream;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.KiePluginPackage;
import org.drools.core.definitions.RulePackage;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.facttemplates.FactTemplate;
import org.drools.core.rule.DialectRuntimeRegistry;
import org.drools.core.rule.Function;
import org.drools.core.rule.ImportDeclaration;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.WindowDeclaration;
import org.drools.core.ruleunit.RuleUnitDescriptionLoader;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.rule.AccumulateFunction;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;

/**
 * Offer a backwards compatible impl of {@link InternalKnowledgeRuntime}
 * offering a new alternative API.
 *
 * {@link #get(Class)}
 *
 * <code><pre>
 *     RulePackage rpkg = pkg.get(RulePackage.class)
 * </pre></code>
 *
 *
 */
public class KiePackageImpl
        implements
        InternalKnowledgePackage,
        Externalizable {

    private static final long serialVersionUID = 510l;

    /**
     * Name of the pkg.
     */
    private String name;

    /**
     * Set of all rule-names in this <code>Package</code>.
     */
    private RulePackage ruleKnowledgePackage;
    private ProcessPackageImpl processKnowledgePackage;

    public KiePackageImpl() {
        this(null);
    }

    /**
     * Construct.
     *
     * @param name The name of this <code>Package</code>.
     */
    public KiePackageImpl(final String name) {
        this.name = name;
        this.ruleKnowledgePackage = new RulePackageImpl(name);
        this.processKnowledgePackage = new ProcessPackageImpl(name);
    }

    public <T extends KiePluginPackage> T get(Class<T> type) {
        // FIXME this should be substituted for some lazy loading -- e.g. CDI
        if (type == ruleKnowledgePackage.getClass()) return (T) ruleKnowledgePackage;
        if (type == processKnowledgePackage.getClass()) return (T) processKnowledgePackage;
        throw new UnsupportedOperationException("Unknown type: " + type.getCanonicalName());
    }


    /************************* deprecated delegates **************************/

    @Override
    public Map<ResourceType, ResourceTypePackage> getResourceTypePackages() {
        return ruleKnowledgePackage.getResourceTypePackages();
    }

    @Override
    public Collection<Rule> getRules() {
        return ruleKnowledgePackage.getRules();
    }

    public Function getFunction(String name) {
        return ruleKnowledgePackage.getFunction(name);
    }

    @Override
    public Collection<FactType> getFactTypes() {
        return ruleKnowledgePackage.getFactTypes();
    }

    public Map<String, FactType> getFactTypesMap() {
        return ruleKnowledgePackage.getFactTypesMap();
    }

    @Override
    public Collection<Query> getQueries() {
        return ruleKnowledgePackage.getQueries();
    }

    @Override
    public Collection<String> getFunctionNames() {
        return ruleKnowledgePackage.getFunctionNames();
    }

    @Override
    public Collection<Global> getGlobalVariables() {
        return ruleKnowledgePackage.getGlobalVariables();
    }

    @Override
    public String getName() {
        return ruleKnowledgePackage.getName();
    }

    @Override
    public ClassLoader getPackageClassLoader() {
        return ruleKnowledgePackage.getPackageClassLoader();
    }

    @Override
    public void addProcess(Process process) {
        processKnowledgePackage.addProcess(process);
    }

    @Override
    public Map<String, Process> getRuleFlows() {
        return processKnowledgePackage.getRuleFlows();
    }

    @Override
    public void removeRuleFlow(String id) {
        processKnowledgePackage.removeRuleFlow(id);
    }

    @Override
    public DialectRuntimeRegistry getDialectRuntimeRegistry() {
        return ruleKnowledgePackage.getDialectRuntimeRegistry();
    }

    @Override
    public void setDialectRuntimeRegistry(DialectRuntimeRegistry dialectRuntimeRegistry) {
        ruleKnowledgePackage.setDialectRuntimeRegistry(dialectRuntimeRegistry);
    }

    @Override
    public void addImport(ImportDeclaration importDecl) {
        ruleKnowledgePackage.addImport(importDecl);
    }

    @Override
    public Map<String, ImportDeclaration> getImports() {
        return ruleKnowledgePackage.getImports();
    }

    @Override
    public void addTypeDeclaration(TypeDeclaration typeDecl) {
        ruleKnowledgePackage.addTypeDeclaration(typeDecl);
    }

    @Override
    public void removeTypeDeclaration(String type) {
        ruleKnowledgePackage.removeTypeDeclaration(type);
    }

    @Override
    public Map<String, TypeDeclaration> getTypeDeclarations() {
        return ruleKnowledgePackage.getTypeDeclarations();
    }

    @Override
    public TypeDeclaration getTypeDeclaration(Class<?> clazz) {
        return ruleKnowledgePackage.getTypeDeclaration(clazz);
    }

    @Override
    public TypeDeclaration getTypeDeclaration(String type) {
        return ruleKnowledgePackage.getTypeDeclaration(type);
    }

    @Override
    public void addStaticImport(String functionImport) {
        ruleKnowledgePackage.addStaticImport(functionImport);
    }

    @Override
    public void addFunction(Function function) {
        ruleKnowledgePackage.addFunction(function);
    }

    @Override
    public Map<String, Function> getFunctions() {
        return ruleKnowledgePackage.getFunctions();
    }

    @Override
    public void addAccumulateFunction(String name, AccumulateFunction function) {
        ruleKnowledgePackage.addAccumulateFunction(name, function);
    }

    @Override
    public Map<String, AccumulateFunction> getAccumulateFunctions() {
        return ruleKnowledgePackage.getAccumulateFunctions();
    }

    public void removeFunctionImport(String functionImport) {
        ruleKnowledgePackage.removeFunctionImport(functionImport);
    }

    @Override
    public Set<String> getStaticImports() {
        return ruleKnowledgePackage.getStaticImports();
    }

    @Override
    public void addGlobal(String identifier, Class<?> clazz) {
        ruleKnowledgePackage.addGlobal(identifier, clazz);
    }

    @Override
    public void removeGlobal(String identifier) {
        ruleKnowledgePackage.removeGlobal(identifier);
    }

    @Override
    public Map<String, String> getGlobals() {
        return ruleKnowledgePackage.getGlobals();
    }

    @Override
    public void removeFunction(String functionName) {
        ruleKnowledgePackage.removeFunction(functionName);
    }

    @Override
    public FactTemplate getFactTemplate(String name) {
        return ruleKnowledgePackage.getFactTemplate(name);
    }

    @Override
    public void addFactTemplate(FactTemplate factTemplate) {
        ruleKnowledgePackage.addFactTemplate(factTemplate);
    }

    @Override
    public void addRule(RuleImpl rule) {
        ruleKnowledgePackage.addRule(rule);
    }

    @Override
    public void removeRule(RuleImpl rule) {
        ruleKnowledgePackage.removeRule(rule);
    }

    @Override
    public RuleImpl getRule(String name) {
        return ruleKnowledgePackage.getRule(name);
    }

    @Override
    public String toString() {
        return ruleKnowledgePackage.toString();
    }

    @Override
    public void setError(String summary) {
        ruleKnowledgePackage.setError(summary);
    }

    @Override
    public void resetErrors() {
        ruleKnowledgePackage.resetErrors();
    }

    @Override
    public boolean isValid() {
        return ruleKnowledgePackage.isValid();
    }

    @Override
    public void checkValidity() {
        ruleKnowledgePackage.checkValidity();
    }

    public String getErrorSummary() {
        return ruleKnowledgePackage.getErrorSummary();
    }

    @Override
    public FactType getFactType(String typeName) {
        return ruleKnowledgePackage.getFactType(typeName);
    }

    @Override
    public ClassFieldAccessorStore getClassFieldAccessorStore() {
        return ruleKnowledgePackage.getClassFieldAccessorStore();
    }

    @Override
    public void setClassFieldAccessorCache(ClassFieldAccessorCache classFieldAccessorCache) {
        ruleKnowledgePackage.setClassFieldAccessorCache(classFieldAccessorCache);
    }

    @Override
    public Set<String> getEntryPointIds() {
        return ruleKnowledgePackage.getEntryPointIds();
    }

    @Override
    public void addEntryPointId(String id) {
        ruleKnowledgePackage.addEntryPointId(id);
    }

    @Override
    public TypeResolver getTypeResolver() {
        return ruleKnowledgePackage.getTypeResolver();
    }

    @Override
    public void setTypeResolver(TypeResolver typeResolver) {
        ruleKnowledgePackage.setTypeResolver(typeResolver);
    }

    @Override
    public RuleUnitDescriptionLoader getRuleUnitDescriptionLoader() {
        return ruleKnowledgePackage.getRuleUnitDescriptionLoader();
    }

    @Override
    public void addWindowDeclaration(WindowDeclaration window) {
        ruleKnowledgePackage.addWindowDeclaration(window);
    }

    @Override
    public Map<String, WindowDeclaration> getWindowDeclarations() {
        return ruleKnowledgePackage.getWindowDeclarations();
    }

    @Override
    public boolean hasTraitRegistry() {
        return ruleKnowledgePackage.hasTraitRegistry();
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        return ruleKnowledgePackage.getTraitRegistry();
    }

    @Override
    public List<TypeDeclaration> removeTypesGeneratedFromResource(Resource resource) {
        return ruleKnowledgePackage.removeTypesGeneratedFromResource(resource);
    }

    @Override
    public List<RuleImpl> removeRulesGeneratedFromResource(Resource resource) {
        return ruleKnowledgePackage.removeRulesGeneratedFromResource(resource);
    }

    @Override
    public List<RuleImpl> getRulesGeneratedFromResource(Resource resource) {
        return ruleKnowledgePackage.getRulesGeneratedFromResource(resource);
    }

    @Override
    public List<Function> removeFunctionsGeneratedFromResource(Resource resource) {
        return ruleKnowledgePackage.removeFunctionsGeneratedFromResource(resource);
    }

    public boolean needsStreamMode() {
        return ruleKnowledgePackage.needsStreamMode();
    }

    @Override
    public void setNeedStreamMode() {
        ruleKnowledgePackage.setNeedStreamMode();
    }

    @Override
    public Collection<Process> getProcesses() {
        return processKnowledgePackage.getProcesses();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
        //return ruleKnowledgePackage.equals(object);
    }

    @Override
    public boolean removeObjectsGeneratedFromResource(Resource resource) {
        return false;
    }

    @Override
    public boolean removeFromResourceTypePackageGeneratedFromResource(Resource resource) {
        return false;
    }

    @Override
    public InternalKnowledgePackage deepCloneIfAlreadyInUse(ClassLoader classLoader) {
        return null;
    }

    @Override
    public void addCloningResource(String key, Object resource) {

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void clear() {
        ruleKnowledgePackage.clear();
    }

    @Override
    public List<Process> removeProcessesGeneratedFromResource(Resource resource) {
        return processKnowledgePackage.removeProcessesGeneratedFromResource(resource);
    }

    /**
     * Handles the write serialization of the Package. Patterns in Rules may
     * reference generated data which cannot be serialized by default methods.
     * The Package uses PackageCompilationData to hold a reference to the
     * generated bytecode. The generated bytecode must be restored before any
     * Rules.
     *
     * @param stream out the stream to write the object to; should be an instance
     *               of DroolsObjectOutputStream or OutputStream
     */
    public void writeExternal(ObjectOutput stream) throws IOException {
        boolean isDroolsStream = stream instanceof DroolsObjectOutputStream;
        ByteArrayOutputStream bytes = null;
        ObjectOutput out;

        if (isDroolsStream) {
            out = stream;
        } else {
            bytes = new ByteArrayOutputStream();
            out = new DroolsObjectOutputStream(bytes);
        }

        out.writeObject(this.name);
        out.writeObject(this.ruleKnowledgePackage);
        out.writeObject(this.processKnowledgePackage);
        // writing the whole stream as a byte array
        if (!isDroolsStream) {
            bytes.flush();
            bytes.close();
            stream.writeObject(bytes.toByteArray());
        }
    }

    /**
     * Handles the read serialization of the Package. Patterns in Rules may
     * reference generated data which cannot be serialized by default methods.
     * The Package uses PackageCompilationData to hold a reference to the
     * generated bytecode; which must be restored before any Rules. A custom
     * ObjectInputStream, able to resolve classes against the bytecode in the
     * PackageCompilationData, is used to restore the Rules.
     *
     * @param stream, the stream to read data from in order to restore the object;
     *                should be an instance of DroolsObjectInputStream or
     *                InputStream
     */
    public void readExternal(ObjectInput stream) throws IOException,
            ClassNotFoundException {
        boolean isDroolsStream = stream instanceof DroolsObjectInputStream;
        DroolsObjectInputStream in = isDroolsStream ? (DroolsObjectInputStream) stream
                : new DroolsObjectInputStream(
                new ByteArrayInputStream(
                        (byte[]) stream.readObject()));

        this.name = (String) in.readObject();
        this.ruleKnowledgePackage = (RulePackage) in.readObject();
        this.processKnowledgePackage = (ProcessPackageImpl) in.readObject();
        in.setStore(null);

        if (!isDroolsStream) {
            in.close();
        }
    }

}
