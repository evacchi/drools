package org.drools.core.definitions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.facttemplates.FactTemplate;
import org.drools.core.rule.DialectRuntimeRegistry;
import org.drools.core.rule.Function;
import org.drools.core.rule.ImportDeclaration;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.WindowDeclaration;
import org.drools.core.ruleunit.RuleUnitDescriptionLoader;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.rule.AccumulateFunction;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;

public interface RulePackage extends KiePluginPackage {

    Map<ResourceType, ResourceTypePackage> getResourceTypePackages();

    Collection<Rule> getRules();

    Function getFunction(String name);

    Collection<FactType> getFactTypes();

    Map<String, FactType> getFactTypesMap();

    Collection<Query> getQueries();

    Collection<String> getFunctionNames();

    Collection<Global> getGlobalVariables();

    void writeExternal(ObjectOutput stream) throws IOException;

    void readExternal(ObjectInput stream) throws IOException,
            ClassNotFoundException;

    String getName();

    ClassLoader getPackageClassLoader();

    DialectRuntimeRegistry getDialectRuntimeRegistry();

    void setDialectRuntimeRegistry(DialectRuntimeRegistry dialectRuntimeRegistry);

    void addImport(ImportDeclaration importDecl);

    Map<String, ImportDeclaration> getImports();

    void addTypeDeclaration(TypeDeclaration typeDecl);

    void removeTypeDeclaration(String type);

    Map<String, TypeDeclaration> getTypeDeclarations();

    TypeDeclaration getTypeDeclaration(Class<?> clazz);

    TypeDeclaration getTypeDeclaration(String type);

    void addStaticImport(String functionImport);

    void addFunction(Function function);

    Map<String, Function> getFunctions();

    void addAccumulateFunction(String name, AccumulateFunction function);

    Map<String, AccumulateFunction> getAccumulateFunctions();

    void removeFunctionImport(String functionImport);

    Set<String> getStaticImports();

    void addGlobal(String identifier,
                   Class<?> clazz);

    void removeGlobal(String identifier);

    Map<String, String> getGlobals();

    void removeFunction(String functionName);

    FactTemplate getFactTemplate(String name);

    void addFactTemplate(FactTemplate factTemplate);

    void addRule(RuleImpl rule);

    void removeRule(RuleImpl rule);

    RuleImpl getRule(String name);

    String toString();

    void setError(String summary);

    void resetErrors();

    boolean isValid();

    void checkValidity();

    String getErrorSummary();

    void clear();

    FactType getFactType(String typeName);

    ClassFieldAccessorStore getClassFieldAccessorStore();

    void setClassFieldAccessorCache(ClassFieldAccessorCache classFieldAccessorCache);

    Set<String> getEntryPointIds();

    void addEntryPointId(String id);

    TypeResolver getTypeResolver();

    void setTypeResolver(TypeResolver typeResolver);

    RuleUnitDescriptionLoader getRuleUnitDescriptionLoader();

    void addWindowDeclaration(WindowDeclaration window);

    Map<String, WindowDeclaration> getWindowDeclarations();

    boolean hasTraitRegistry();

    TraitRegistry getTraitRegistry();

    List<TypeDeclaration> removeTypesGeneratedFromResource(Resource resource);

    List<RuleImpl> removeRulesGeneratedFromResource(Resource resource);

    List<RuleImpl> getRulesGeneratedFromResource(Resource resource);

    List<Function> removeFunctionsGeneratedFromResource(Resource resource);

    boolean needsStreamMode();

    void setNeedStreamMode();
}
