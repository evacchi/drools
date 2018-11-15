package org.drools.core.definitions.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.common.DroolsObjectInputStream;
import org.drools.core.common.DroolsObjectOutputStream;
import org.drools.core.rule.packaging.RulePackage;
import org.drools.core.definitions.rule.impl.GlobalImpl;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.facttemplates.FactTemplate;
import org.drools.core.rule.DialectRuntimeRegistry;
import org.drools.core.rule.Function;
import org.drools.core.rule.ImportDeclaration;
import org.drools.core.rule.InvalidRulePackage;
import org.drools.core.rule.JavaDialectRuntimeData;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.WindowDeclaration;
import org.drools.core.ruleunit.RuleUnitDescriptionLoader;
import org.drools.core.util.ClassUtils;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.rule.AccumulateFunction;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;

public class RulePackageImpl implements RulePackage {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DRL;
    }

    /**
     * Name of the pkg.
     */
    private String name;

    /**
     * Set of all rule-names in this <code>Package</code>.
     */
    private Map<String, RuleImpl> rules = new LinkedHashMap<>();

    private Map<String, ImportDeclaration> imports = new HashMap<>();

    private Map<String, Function> functions;

    private Map<String, AccumulateFunction> accumulateFunctions;

    private Set<String> staticImports;

    private Map<String, String> globals;

    private Map<String, FactTemplate> factTemplates;

    // private JavaDialectData packageCompilationData;
    private DialectRuntimeRegistry dialectRuntimeRegistry;

    private Map<String, TypeDeclaration> typeDeclarations = new ConcurrentHashMap<>();

    private Set<String> entryPointsIds = Collections.emptySet();

    private Map<String, WindowDeclaration> windowDeclarations;

    private ClassFieldAccessorStore classFieldAccessorStore;

    private TraitRegistry traitRegistry;

    private Map<String, Object> cloningResources = new HashMap<>();

    /**
     * This is to indicate the the package has no errors during the
     * compilation/building phase
     */
    private boolean valid = true;

    private boolean needStreamMode = false;

    /**
     * This will keep a summary error message as to why this package is not
     * valid
     */
    private String errorSummary;

    private transient TypeResolver typeResolver;

    private transient RuleUnitDescriptionLoader ruleUnitDescriptionLoader;

    private transient AtomicBoolean inUse = new AtomicBoolean(false);

    public RulePackageImpl() {
        this(null);
    }

    /**
     * Construct.
     *
     * @param name The name of this <code>Package</code>.
     */
    public RulePackageImpl(final String name) {
        this.name = name;
        this.accumulateFunctions = Collections.emptyMap();
        this.staticImports = Collections.emptySet();
        this.globals = Collections.emptyMap();
        this.factTemplates = Collections.emptyMap();
        this.functions = Collections.emptyMap();
        this.dialectRuntimeRegistry = new DialectRuntimeRegistry();
        this.classFieldAccessorStore = new ClassFieldAccessorStore();
        this.entryPointsIds = Collections.emptySet();
        this.windowDeclarations = Collections.emptyMap();
    }

    @Override
    public void merge(RulePackage rulePackage) {
        this.getAccumulateFunctions().putAll(rulePackage.getAccumulateFunctions());
        this.getStaticImports().addAll(rulePackage.getStaticImports());
        this.getGlobals().putAll(rulePackage.getGlobals());
        this.getFactTemplates().putAll(rulePackage.getFactTemplates());
        this.getFunctions().putAll(rulePackage.getFunctions());
        this.getDialectRuntimeRegistry().merge(rulePackage.getDialectRuntimeRegistry(), getPackageClassLoader());
        this.getClassFieldAccessorStore().merge(rulePackage.getClassFieldAccessorStore());
        this.getEntryPointIds().addAll(rulePackage.getEntryPointIds());
        this.getWindowDeclarations().putAll(rulePackage.getWindowDeclarations());
    }

    @Override
    public Collection<Rule> getRules() {
        return Collections.unmodifiableCollection(rules.values());
    }

    @Override
    public Function getFunction(String name) {
        return functions.getOrDefault(name, null);
    }

    @Override
    public Collection<FactType> getFactTypes() {
        if (typeDeclarations.isEmpty()) {
            return Collections.emptyList();
        }
        List<FactType> list = new ArrayList<FactType>();
        for (TypeDeclaration typeDeclaration : typeDeclarations.values()) {
            list.add(typeDeclaration.getTypeClassDef());
        }
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Map<String, FactType> getFactTypesMap() {
        Map<String, FactType> types = new HashMap<String, FactType>();
        for (Map.Entry<String, TypeDeclaration> entry : typeDeclarations.entrySet()) {
            types.put(entry.getKey(), entry.getValue().getTypeClassDef());
        }
        return types;
    }

    @Override
    public Collection<Query> getQueries() {
        List<Query> list = new ArrayList<Query>(rules.size());
        for (RuleImpl rule : rules.values()) {
            if (rule.isQuery()) {
                list.add(rule);
            }
        }
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Collection<String> getFunctionNames() {
        return Collections.unmodifiableCollection(functions.keySet());
    }

    @Override
    public Collection<Global> getGlobalVariables() {
        List<Global> list = new ArrayList<Global>(getGlobals().size());
        for (Map.Entry<String, String> global : getGlobals().entrySet()) {
            list.add(new GlobalImpl(global.getKey(), global.getValue()));
        }
        return Collections.unmodifiableCollection(list);
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
    @Override
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
        out.writeObject(this.classFieldAccessorStore);
        out.writeObject(this.dialectRuntimeRegistry);
        out.writeObject(this.typeDeclarations);
        out.writeObject(this.imports);
        out.writeObject(this.staticImports);
        out.writeObject(this.functions);
        out.writeObject(this.accumulateFunctions);
        out.writeObject(this.factTemplates);
        out.writeObject(this.globals);
        out.writeBoolean(this.valid);
        out.writeBoolean(this.needStreamMode);
        out.writeObject(this.rules);
        out.writeObject(this.entryPointsIds);
        out.writeObject(this.windowDeclarations);
        out.writeObject(this.traitRegistry);
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
    @Override
    public void readExternal(ObjectInput stream) throws IOException,
            ClassNotFoundException {
        boolean isDroolsStream = stream instanceof DroolsObjectInputStream;
        DroolsObjectInputStream in = isDroolsStream ? (DroolsObjectInputStream) stream
                : new DroolsObjectInputStream(
                new ByteArrayInputStream(
                        (byte[]) stream.readObject()));

        this.name = (String) in.readObject();
        this.classFieldAccessorStore = (ClassFieldAccessorStore) in.readObject();
        in.setStore(this.classFieldAccessorStore);

        this.dialectRuntimeRegistry = (DialectRuntimeRegistry) in.readObject();
        this.typeDeclarations = (Map) in.readObject();
        this.imports = (Map<String, ImportDeclaration>) in.readObject();
        this.staticImports = (Set) in.readObject();
        this.functions = (Map<String, Function>) in.readObject();
        this.accumulateFunctions = (Map<String, AccumulateFunction>) in.readObject();
        this.factTemplates = (Map) in.readObject();
        this.globals = (Map<String, String>) in.readObject();
        this.valid = in.readBoolean();
        this.needStreamMode = in.readBoolean();
        this.rules = (Map<String, RuleImpl>) in.readObject();
        this.entryPointsIds = (Set<String>) in.readObject();
        this.windowDeclarations = (Map<String, WindowDeclaration>) in.readObject();
        this.traitRegistry = (TraitRegistry) in.readObject();

        in.setStore(null);

        if (!isDroolsStream) {
            in.close();
        }
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
        JavaDialectRuntimeData javaRuntime = (JavaDialectRuntimeData) getDialectRuntimeRegistry().getDialectData("java");
        return javaRuntime.getClassLoader();
    }

    @Override
    public DialectRuntimeRegistry getDialectRuntimeRegistry() {
        return this.dialectRuntimeRegistry;
    }

    @Override
    public void setDialectRuntimeRegistry(DialectRuntimeRegistry dialectRuntimeRegistry) {
        this.dialectRuntimeRegistry = dialectRuntimeRegistry;
    }

    @Override
    public void addImport(final ImportDeclaration importDecl) {
        this.imports.put(importDecl.getTarget(),
                         importDecl);
    }

    @Override
    public Map<String, ImportDeclaration> getImports() {
        return this.imports;
    }

    @Override
    public void addTypeDeclaration(final TypeDeclaration typeDecl) {
        this.typeDeclarations.put(typeDecl.getTypeName(),
                                  typeDecl);
    }

    @Override
    public void removeTypeDeclaration(final String type) {
        this.typeDeclarations.remove(type);
    }

    @Override
    public Map<String, TypeDeclaration> getTypeDeclarations() {
        return this.typeDeclarations;
    }

    @Override
    public TypeDeclaration getTypeDeclaration(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        TypeDeclaration typeDeclaration = getTypeDeclaration(ClassUtils.getSimpleName(clazz));
        if (typeDeclaration == null) {
            // check if clazz is resolved by any of the type declarations
            for (TypeDeclaration type : this.typeDeclarations.values()) {
                if (type.isValid() && type.matches(clazz)) {
                    typeDeclaration = type;
                    break;
                }
            }
        }
        return typeDeclaration;
    }

    @Override
    public TypeDeclaration getTypeDeclaration(String type) {
        return this.typeDeclarations.get(type);
    }

    @Override
    public void addStaticImport(final String functionImport) {
        if (this.staticImports == Collections.EMPTY_SET) {
            this.staticImports = new HashSet<String>(2);
        }
        this.staticImports.add(functionImport);
    }

    @Override
    public void addFunction(final Function function) {
        if (this.functions == Collections.EMPTY_MAP) {
            this.functions = new HashMap<String, Function>(1);
        }

        this.functions.put(function.getName(),
                           function);
        dialectRuntimeRegistry.getDialectData(function.getDialect()).setDirty(true);
    }

    @Override
    public Map<String, Function> getFunctions() {
        return this.functions;
    }

    @Override
    public void addAccumulateFunction(final String name, final AccumulateFunction function) {
        if (this.accumulateFunctions == Collections.EMPTY_MAP) {
            this.accumulateFunctions = new HashMap<String, AccumulateFunction>(1);
        }

        this.accumulateFunctions.put(name,
                                     function);
    }

    @Override
    public Map<String, AccumulateFunction> getAccumulateFunctions() {
        return this.accumulateFunctions;
    }

    @Override
    public void removeFunctionImport(final String functionImport) {
        this.staticImports.remove(functionImport);
    }

    @Override
    public Set<String> getStaticImports() {
        return this.staticImports;
    }

    @Override
    public void addGlobal(final String identifier,
                          final Class<?> clazz) {
        if (this.globals == Collections.EMPTY_MAP) {
            this.globals = new HashMap<String, String>(1);
        }
        this.globals.put(identifier,
                         clazz.getName());
    }

    @Override
    public void removeGlobal(final String identifier) {
        this.globals.remove(identifier);
    }

    @Override
    public Map<String, String> getGlobals() {
        return this.globals;
    }

    @Override
    public void removeFunction(final String functionName) {
        Function function = this.functions.remove(functionName);
        if (function != null) {
            // FIXME this.dialectRuntimeRegistry.removeFunction(this, function);
        }
    }

    @Override
    public FactTemplate getFactTemplate(final String name) {
        return this.factTemplates.get(name);
    }

    @Override
    public void addFactTemplate(final FactTemplate factTemplate) {
        if (this.factTemplates == Collections.EMPTY_MAP) {
            this.factTemplates = new HashMap<>(1);
        }
        this.factTemplates.put(factTemplate.getName(),
                               factTemplate);
    }

    @Override
    public Map<String, FactTemplate> getFactTemplates() {
        return factTemplates;
    }

    /**
     * Add a <code>Rule</code> to this <code>Package</code>.
     *
     * @param rule The rule to add.
     * @throws org.drools.core.rule.DuplicateRuleNameException If the <code>Rule</code> attempting to be added has the
     *                                                         same name as another previously added <code>Rule</code>.
     * @throws org.drools.core.rule.InvalidRuleException       If the <code>Rule</code> is not valid.
     */
    @Override
    public void addRule(RuleImpl rule) {
        this.rules.put(rule.getName(),
                       rule);
    }

    @Override
    public void removeRule(RuleImpl rule) {
        this.rules.remove(rule.getName());

        //FIXME this.dialectRuntimeRegistry.removeRule(this, rule);
    }

    /**
     * Retrieve a <code>Rule</code> by name.
     *
     * @param name The name of the <code>Rule</code> to retrieve.
     * @return The named <code>Rule</code>, or <code>null</code> if not
     * such <code>Rule</code> has been added to this
     * <code>Package</code>.
     */
    @Override
    public RuleImpl getRule(final String name) {
        return this.rules.get(name);
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

        if (object == null || !(object instanceof RulePackageImpl)) {
            return false;
        }

        RulePackageImpl other = (RulePackageImpl) object;

        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public void clear() {
        this.rules.clear();
        this.dialectRuntimeRegistry.clear();
        this.imports.clear();
        this.functions.clear();
        this.accumulateFunctions.clear();
        this.staticImports.clear();
        this.globals.clear();
        this.factTemplates.clear();
        this.typeDeclarations.clear();
        this.windowDeclarations.clear();
    }

    @Override
    public FactType getFactType(final String typeName) {
        if (typeName == null || (this.name != null && !typeName.startsWith(this.name + "."))) {
            return null;
        }
        // in case the package name is != null, remove the package name from the
        // beginning of the type name
        String key = this.name == null ? typeName : typeName.substring(this.name.length() + 1);
        TypeDeclaration decl = this.typeDeclarations.get(key);
        if (decl == null) {
            return null;
        } else {
            if (decl.isDefinition() || decl.isGeneratedFact()) {
                return decl.getTypeClassDef();
            } else {
                throw new UnsupportedOperationException("KieBase.getFactType should only be used to retrieve declared beans. Class " + typeName + " exists outside DRL ");
            }
        }
    }

    @Override
    public ClassFieldAccessorStore getClassFieldAccessorStore() {
        return classFieldAccessorStore;
    }

    @Override
    public void setClassFieldAccessorCache(ClassFieldAccessorCache classFieldAccessorCache) {
        this.classFieldAccessorStore.setClassFieldAccessorCache(classFieldAccessorCache);
    }

    @Override
    public Set<String> getEntryPointIds() {
        return entryPointsIds;
    }

    @Override
    public void addEntryPointId(String id) {
        if (entryPointsIds == Collections.EMPTY_SET) {
            entryPointsIds = new HashSet<String>();
        }
        entryPointsIds.add(id);
    }

    @Override
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    @Override
    public void setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
        this.ruleUnitDescriptionLoader = new RuleUnitDescriptionLoader(typeResolver);
    }

    @Override
    public RuleUnitDescriptionLoader getRuleUnitDescriptionLoader() {
        return ruleUnitDescriptionLoader;
    }

    @Override
    public void addWindowDeclaration(WindowDeclaration window) {
        if (windowDeclarations == Collections.EMPTY_MAP) {
            windowDeclarations = new HashMap<String, WindowDeclaration>();
        }
        this.windowDeclarations.put(window.getName(), window);
    }

    @Override
    public Map<String, WindowDeclaration> getWindowDeclarations() {
        return windowDeclarations;
    }

    @Override
    public boolean hasTraitRegistry() {
        return traitRegistry != null;
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        if (traitRegistry == null) {
            traitRegistry = new TraitRegistry();
        }
        return traitRegistry;
    }

//    public boolean removeObjectsGeneratedFromResource(Resource resource) {
//        List<RuleImpl> rulesToBeRemoved = removeRulesGeneratedFromResource(resource);
//        List<TypeDeclaration> typesToBeRemoved = removeTypesGeneratedFromResource(resource);
//        List<Function> functionsToBeRemoved = removeFunctionsGeneratedFromResource(resource);
//        List<Process> processesToBeRemoved = removeProcessesGeneratedFromResource(resource);
//        boolean resourceTypePackageSomethingRemoved = removeFromResourceTypePackageGeneratedFromResource(resource);
//        return !rulesToBeRemoved.isEmpty()
//                || !typesToBeRemoved.isEmpty()
//                || !functionsToBeRemoved.isEmpty()
//                || !processesToBeRemoved.isEmpty()
//                || resourceTypePackageSomethingRemoved;
//    }

//    @Override
//    public boolean removeFromResourceTypePackageGeneratedFromResource(Resource resource) {
//        boolean somethingWasRemoved = false;
//        for (ResourceTypePackage rtp : resourceTypePackages.values()) {
//            somethingWasRemoved = rtp.removeResource(resource) || somethingWasRemoved;
//        }
//        return somethingWasRemoved;
//    }

    @Override
    public List<TypeDeclaration> removeTypesGeneratedFromResource(Resource resource) {
        List<TypeDeclaration> typesToBeRemoved = getTypesGeneratedFromResource(resource);
        if (!typesToBeRemoved.isEmpty()) {
            JavaDialectRuntimeData dialect = (JavaDialectRuntimeData) getDialectRuntimeRegistry().getDialectData("java");
            for (TypeDeclaration type : typesToBeRemoved) {
                if (type.getTypeClassName() != null) {
                    // the type declaration might not have been built up to actual class, if an error was found first
                    // in this case, no accessor would have been wired
                    classFieldAccessorStore.removeType(type);
                    dialect.remove(type.getTypeClassName());
                    if (typeResolver != null) {
                        typeResolver.registerClass( type.getTypeClassName(), null );
                    }
                }
                removeTypeDeclaration(type.getTypeName());
            }
            dialect.reload();
        }
        return typesToBeRemoved;
    }

    @Override
    public List<RuleImpl> removeRulesGeneratedFromResource(Resource resource) {
        List<RuleImpl> rulesToBeRemoved = getRulesGeneratedFromResource(resource);
        for (RuleImpl rule : rulesToBeRemoved) {
            removeRule(rule);
        }
        return rulesToBeRemoved;
    }

    @Override
    public List<RuleImpl> getRulesGeneratedFromResource(Resource resource) {
        List<RuleImpl> rulesFromResource = new ArrayList<RuleImpl>();
        for (RuleImpl rule : rules.values()) {
            if (resource.equals(rule.getResource())) {
                rulesFromResource.add(rule);
            }
        }
        return rulesFromResource;
    }

    private List<TypeDeclaration> getTypesGeneratedFromResource(Resource resource) {
        List<TypeDeclaration> typesFromResource = new ArrayList<TypeDeclaration>();
        for (TypeDeclaration type : typeDeclarations.values()) {
            if (resource.equals(type.getResource())) {
                typesFromResource.add(type);
            }
        }
        return typesFromResource;
    }

    @Override
    public List<Function> removeFunctionsGeneratedFromResource(Resource resource) {
        List<Function> functionsToBeRemoved = getFunctionsGeneratedFromResource(resource);
        for (Function function : functionsToBeRemoved) {
            removeFunction(function.getName());
        }
        return functionsToBeRemoved;
    }

    private List<Function> getFunctionsGeneratedFromResource(Resource resource) {
        List<Function> functionsFromResource = new ArrayList<Function>();
        for (Function function : functions.values()) {
            if (resource.equals(function.getResource())) {
                functionsFromResource.add(function);
            }
        }
        return functionsFromResource;
    }


    @Override
    public boolean needsStreamMode() {
        return needStreamMode;
    }

    @Override
    public void setNeedStreamMode() {
        this.needStreamMode = true;
    }

//    public KnowledgePackageImpl deepCloneIfAlreadyInUse(ClassLoader classLoader) {
//        if (inUse.compareAndSet(false, true)) {
//            return this;
//        }
//
//        if (classLoader instanceof ProjectClassLoader) {
//            JavaDialectRuntimeData javaDialectRuntimeData = (JavaDialectRuntimeData) dialectRuntimeRegistry.getDialectData("java");
//            if (javaDialectRuntimeData == null) {
//                // using the canonical model there's no runtime registry and no need for any clone
//                return this;
//            }
//            ClassLoader originalClassLoader = javaDialectRuntimeData.getRootClassLoader();
//            if (classLoader == originalClassLoader) {
//                // if the classloader isn't changed there's no need for a clone
//                return this;
//            }
//            if (originalClassLoader instanceof ProjectClassLoader) {
//                ((ProjectClassLoader) classLoader).initFrom((ProjectClassLoader) originalClassLoader);
//            }
//        }
//
//        return ClassUtils.deepClone(this, classLoader, cloningResources);
//    }
//
//    @Override
//    public void addCloningResource(String key, Object resource) {
//        this.cloningResources.put(key, resource);
//    }
}
