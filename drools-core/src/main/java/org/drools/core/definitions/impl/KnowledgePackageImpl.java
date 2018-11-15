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
import org.drools.core.rule.packaging.ProcessPackage;
import org.drools.core.rule.packaging.RulePackage;
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

public class KnowledgePackageImpl
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
    private KiePackageImpl packages;

    public KnowledgePackageImpl() {
        this(null);
    }

    /**
     * Construct.
     *
     * @param name The name of this <code>Package</code>.
     */
    public KnowledgePackageImpl(final String name) {
        packages = new KiePackageImpl();
    }

    private RulePackage rule() {
        return (RulePackage) packages.getResourceTypePackages().get(ResourceType.DRL);
    }

    private ProcessPackage process() {
        return (ProcessPackage) packages.getResourceTypePackages().get(ResourceType.BPMN2);
    }

    /************************* deprecated delegates **************************/

    @Override
    public Map<ResourceType, ResourceTypePackage> getResourceTypePackages() {
        return packages.getResourceTypePackages();
    }

    @Override
    public Collection<Rule> getRules() {
        return rule().getRules();
    }

    public Function getFunction(String name) {
        return rule().getFunction(name);
    }

    @Override
    public Collection<FactType> getFactTypes() {
        return rule().getFactTypes();
    }

    public Map<String, FactType> getFactTypesMap() {
        return rule().getFactTypesMap();
    }

    @Override
    public Collection<Query> getQueries() {
        return rule().getQueries();
    }

    @Override
    public Collection<String> getFunctionNames() {
        return rule().getFunctionNames();
    }

    @Override
    public Collection<Global> getGlobalVariables() {
        return rule().getGlobalVariables();
    }

    @Override
    public String getName() {
        return rule().getName();
    }

    @Override
    public ClassLoader getPackageClassLoader() {
        return rule().getPackageClassLoader();
    }

    @Override
    public void addProcess(Process process) {
        process().addProcess(process);
    }

    @Override
    public Map<String, Process> getRuleFlows() {
        return process().getRuleFlows();
    }

    @Override
    public void removeRuleFlow(String id) {
        process().removeRuleFlow(id);
    }

    @Override
    public DialectRuntimeRegistry getDialectRuntimeRegistry() {
        return rule().getDialectRuntimeRegistry();
    }

    @Override
    public void setDialectRuntimeRegistry(DialectRuntimeRegistry dialectRuntimeRegistry) {
        rule().setDialectRuntimeRegistry(dialectRuntimeRegistry);
    }

    @Override
    public void addImport(ImportDeclaration importDecl) {
        rule().addImport(importDecl);
    }

    @Override
    public Map<String, ImportDeclaration> getImports() {
        return rule().getImports();
    }

    @Override
    public void addTypeDeclaration(TypeDeclaration typeDecl) {
        rule().addTypeDeclaration(typeDecl);
    }

    @Override
    public void removeTypeDeclaration(String type) {
        rule().removeTypeDeclaration(type);
    }

    @Override
    public Map<String, TypeDeclaration> getTypeDeclarations() {
        return rule().getTypeDeclarations();
    }

    @Override
    public TypeDeclaration getTypeDeclaration(Class<?> clazz) {
        return rule().getTypeDeclaration(clazz);
    }

    @Override
    public TypeDeclaration getTypeDeclaration(String type) {
        return rule().getTypeDeclaration(type);
    }

    @Override
    public void addStaticImport(String functionImport) {
        rule().addStaticImport(functionImport);
    }

    @Override
    public void addFunction(Function function) {
        rule().addFunction(function);
    }

    @Override
    public Map<String, Function> getFunctions() {
        return rule().getFunctions();
    }

    @Override
    public void addAccumulateFunction(String name, AccumulateFunction function) {
        rule().addAccumulateFunction(name, function);
    }

    @Override
    public Map<String, AccumulateFunction> getAccumulateFunctions() {
        return rule().getAccumulateFunctions();
    }

    public void removeFunctionImport(String functionImport) {
        rule().removeFunctionImport(functionImport);
    }

    @Override
    public Set<String> getStaticImports() {
        return rule().getStaticImports();
    }

    @Override
    public void addGlobal(String identifier, Class<?> clazz) {
        rule().addGlobal(identifier, clazz);
    }

    @Override
    public void removeGlobal(String identifier) {
        rule().removeGlobal(identifier);
    }

    @Override
    public Map<String, String> getGlobals() {
        return rule().getGlobals();
    }

    @Override
    public void removeFunction(String functionName) {
        rule().removeFunction(functionName);
    }

    @Override
    public FactTemplate getFactTemplate(String name) {
        return rule().getFactTemplate(name);
    }

    @Override
    public void addFactTemplate(FactTemplate factTemplate) {
        rule().addFactTemplate(factTemplate);
    }

    @Override
    public void addRule(RuleImpl rule) {
        rule().addRule(rule);
    }

    @Override
    public void removeRule(RuleImpl rule) {
        rule().removeRule(rule);
    }

    @Override
    public RuleImpl getRule(String name) {
        return rule().getRule(name);
    }

    @Override
    public String toString() {
        return rule().toString();
    }

    @Override
    public void setError(String summary) {
        rule().setError(summary);
    }

    @Override
    public void resetErrors() {
        rule().resetErrors();
    }

    @Override
    public boolean isValid() {
        return rule().isValid();
    }

    @Override
    public void checkValidity() {
        rule().checkValidity();
    }

    public String getErrorSummary() {
        return rule().getErrorSummary();
    }

    @Override
    public FactType getFactType(String typeName) {
        return rule().getFactType(typeName);
    }

    @Override
    public ClassFieldAccessorStore getClassFieldAccessorStore() {
        return rule().getClassFieldAccessorStore();
    }

    @Override
    public void setClassFieldAccessorCache(ClassFieldAccessorCache classFieldAccessorCache) {
        rule().setClassFieldAccessorCache(classFieldAccessorCache);
    }

    @Override
    public Set<String> getEntryPointIds() {
        return rule().getEntryPointIds();
    }

    @Override
    public void addEntryPointId(String id) {
        rule().addEntryPointId(id);
    }

    @Override
    public TypeResolver getTypeResolver() {
        return rule().getTypeResolver();
    }

    @Override
    public void setTypeResolver(TypeResolver typeResolver) {
        rule().setTypeResolver(typeResolver);
    }

    @Override
    public RuleUnitDescriptionLoader getRuleUnitDescriptionLoader() {
        return rule().getRuleUnitDescriptionLoader();
    }

    @Override
    public void addWindowDeclaration(WindowDeclaration window) {
        rule().addWindowDeclaration(window);
    }

    @Override
    public Map<String, WindowDeclaration> getWindowDeclarations() {
        return rule().getWindowDeclarations();
    }

    @Override
    public boolean hasTraitRegistry() {
        return rule().hasTraitRegistry();
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        return rule().getTraitRegistry();
    }

    @Override
    public List<TypeDeclaration> removeTypesGeneratedFromResource(Resource resource) {
        return rule().removeTypesGeneratedFromResource(resource);
    }

    @Override
    public List<RuleImpl> removeRulesGeneratedFromResource(Resource resource) {
        return rule().removeRulesGeneratedFromResource(resource);
    }

    @Override
    public List<RuleImpl> getRulesGeneratedFromResource(Resource resource) {
        return rule().getRulesGeneratedFromResource(resource);
    }

    @Override
    public List<Function> removeFunctionsGeneratedFromResource(Resource resource) {
        return rule().removeFunctionsGeneratedFromResource(resource);
    }

    public boolean needsStreamMode() {
        return rule().needsStreamMode();
    }

    @Override
    public void setNeedStreamMode() {
        rule().setNeedStreamMode();
    }

    @Override
    public Collection<Process> getProcesses() {
        return process().getProcesses();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
        //return ruleKnowledgePackage().equals(object);
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
        rule().clear();
    }

    @Override
    public List<Process> removeProcessesGeneratedFromResource(Resource resource) {
        return process().removeProcessesGeneratedFromResource(resource);
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
        out.writeObject(this.packages);
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
        this.packages = (KiePackageImpl) in.readObject();
        in.setStore(null);

        if (!isDroolsStream) {
            in.close();
        }
    }

}
