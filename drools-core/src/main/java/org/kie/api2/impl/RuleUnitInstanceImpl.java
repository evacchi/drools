package org.kie.api2.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.drools.core.SessionConfiguration;
import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalAgendaGroup;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.Memory;
import org.drools.core.common.MemoryFactory;
import org.drools.core.common.NodeMemories;
import org.drools.core.common.ObjectStore;
import org.drools.core.common.ObjectTypeConfigurationRegistry;
import org.drools.core.common.TruthMaintenanceSystem;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.event.AgendaEventSupport;
import org.drools.core.event.RuleEventListenerSupport;
import org.drools.core.event.RuleRuntimeEventSupport;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.phreak.PropagationEntry;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ReteooFactHandleFactory;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.rule.EntryPointId;
import org.drools.core.runtime.process.InternalProcessRuntime;
import org.drools.core.spi.Activation;
import org.drools.core.spi.AsyncExceptionHandler;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.spi.GlobalResolver;
import org.drools.core.time.TimerService;
import org.drools.core.util.bitmask.BitMask;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.time.SessionClock;
import org.kie.api2.api.DataSource;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.api.RuleUnitInstance;

public class RuleUnitInstanceImpl<T extends RuleUnit> implements RuleUnitInstance<T> {

    private final DummyWorkingMemory workingMemory;
    private RuleUnit unit;
    private InternalKnowledgeBase kBase;
    private InternalAgenda agenda;

    public RuleUnitInstanceImpl(RuleUnit unit, InternalKnowledgeBase kBase) {
        this.unit = unit;
        this.kBase = kBase;
        this.agenda = new PatchedDefaultAgenda(kBase);
        this.workingMemory = new DummyWorkingMemory();
        this.agenda.setWorkingMemory(workingMemory);
    }

    public int fireAllRules() {
        return fireAllRules(null, -1);
    }

    private int fireAllRules(AgendaFilter agendaFilter, int fireLimit) {
        return this.agenda.fireAllRules(agendaFilter, fireLimit);
    }

    public void start() {

    }

    @Override
    public void run() {
        bindDataSources();
        agenda.flushPropagations();

        InternalAgendaGroup unitGroup =
                (InternalAgendaGroup) agenda.getAgendaGroup(unit.getClass().getName());
        unitGroup.setAutoDeactivate(false);
        unitGroup.setFocus();

        fireAllRules();
    }

    private void bindDataSources() {
        try {
            for (Field field : unit.getClass().getDeclaredFields()) {
                if (field.getType().isAssignableFrom(DataSource.class)) {
                    field.setAccessible(true);
                    DataSourceImpl<?> v = (DataSourceImpl<?>) field.get(unit);
                    v.bind(v, workingMemory);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException();
        }
    }
}

class DummyWorkingMemory implements InternalWorkingMemory {

    @Override
    public InternalAgenda getAgenda() {
        return null;
    }

    @Override
    public void setGlobal(String identifier, Object value) {

    }

    @Override
    public Object getGlobal(String identifier) {
        return null;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public void setGlobalResolver(GlobalResolver globalResolver) {

    }

    @Override
    public GlobalResolver getGlobalResolver() {
        return null;
    }

    @Override
    public InternalKnowledgeBase getKnowledgeBase() {
        return null;
    }

    @Override
    public void delete(FactHandle factHandle, RuleImpl rule, TerminalNode terminalNode) {

    }

    @Override
    public void delete(FactHandle factHandle, RuleImpl rule, TerminalNode terminalNode, FactHandle.State fhState) {

    }

    @Override
    public void update(FactHandle handle, Object object, BitMask mask, Class<?> modifiedClass, Activation activation) {

    }

    @Override
    public TruthMaintenanceSystem getTruthMaintenanceSystem() {
        return null;
    }

    @Override
    public int fireAllRules() {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter) {
        return 0;
    }

    @Override
    public int fireAllRules(int fireLimit) {
        return 0;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int fireLimit) {
        return 0;
    }

    @Override
    public Object getObject(FactHandle handle) {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter filter) {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        return null;
    }

    @Override
    public long getFactCount() {
        return 0;
    }

    @Override
    public String getEntryPointId() {
        return null;
    }

    @Override
    public FactHandle insert(Object object) {
        return null;
    }

    @Override
    public void retract(FactHandle handle) {

    }

    @Override
    public void delete(FactHandle handle) {

    }

    @Override
    public void delete(FactHandle handle, FactHandle.State fhState) {

    }

    @Override
    public void update(FactHandle handle, Object object) {

    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {

    }

    @Override
    public FactHandle getFactHandle(Object object) {
        return null;
    }

    @Override
    public long getIdentifier() {
        return 0;
    }

    @Override
    public void setIdentifier(long id) {

    }

    @Override
    public void setRuleRuntimeEventSupport(RuleRuntimeEventSupport workingMemoryEventSupport) {

    }

    @Override
    public void setAgendaEventSupport(AgendaEventSupport agendaEventSupport) {

    }

    @Override
    public <T extends Memory> T getNodeMemory(MemoryFactory<T> node) {
        return null;
    }

    @Override
    public void clearNodeMemory(MemoryFactory node) {

    }

    @Override
    public NodeMemories getNodeMemories() {
        return null;
    }

    @Override
    public long getNextPropagationIdCounter() {
        return 0;
    }

    @Override
    public ObjectStore getObjectStore() {
        return null;
    }

    @Override
    public FactHandleFactory getHandleFactory() {
        return null;
    }

    @Override
    public void queueWorkingMemoryAction(WorkingMemoryAction action) {

    }

    @Override
    public FactHandleFactory getFactHandleFactory() {
        return new ReteooFactHandleFactory();
    }

    @Override
    public EntryPointId getEntryPoint() {
        return null;
    }

    @Override
    public InternalWorkingMemory getInternalWorkingMemory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntryPointNode getEntryPointNode() {
        return null;
    }

    @Override
    public EntryPoint getEntryPoint(String name) {
        return null;
    }

    @Override
    public FactHandle getFactHandleByIdentity(Object object) {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public Iterator<?> iterateObjects() {
        return null;
    }

    @Override
    public Iterator<?> iterateObjects(ObjectFilter filter) {
        return null;
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles() {
        return null;
    }

    @Override
    public Iterator<InternalFactHandle> iterateFactHandles(ObjectFilter filter) {
        return null;
    }

    @Override
    public void setFocus(String focus) {

    }

    @Override
    public QueryResults getQueryResults(String query, Object... arguments) {
        return null;
    }

    @Override
    public void setAsyncExceptionHandler(AsyncExceptionHandler handler) {

    }

    @Override
    public void clearAgenda() {

    }

    @Override
    public void clearAgendaGroup(String group) {

    }

    @Override
    public void clearActivationGroup(String group) {

    }

    @Override
    public void clearRuleFlowGroup(String group) {

    }

    @Override
    public ProcessInstance startProcess(String processId) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long id) {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(long id, boolean readOnly) {
        return null;
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return null;
    }

    @Override
    public void halt() {

    }

    @Override
    public FactHandle insert(Object object, boolean dynamic) {
        return null;
    }

    @Override
    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String id) {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Override
    public SessionClock getSessionClock() {
        return null;
    }

    @Override
    public Lock getLock() {
        return null;
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public ObjectTypeConfigurationRegistry getObjectTypeConfigurationRegistry() {
        return null;
    }

    @Override
    public InternalFactHandle getInitialFactHandle() {
        return null;
    }

    @Override
    public Calendars getCalendars() {
        return null;
    }

    @Override
    public TimerService getTimerService() {
        return null;
    }

    @Override
    public InternalKnowledgeRuntime getKnowledgeRuntime() {
        return null;
    }

    @Override
    public Map<String, Channel> getChannels() {
        return null;
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return null;
    }

    @Override
    public SessionConfiguration getSessionConfiguration() {
        return null;
    }

    @Override
    public void startBatchExecution() {

    }

    @Override
    public void endBatchExecution() {

    }

    @Override
    public void startOperation() {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public long getIdleTime() {
        return 0;
    }

    @Override
    public long getTimeToNextJob() {
        return 0;
    }

    @Override
    public void updateEntryPointsCache() {

    }

    @Override
    public void prepareToFireActivation() {

    }

    @Override
    public void activationFired() {

    }

    @Override
    public long getTotalFactCount() {
        return 0;
    }

    @Override
    public InternalProcessRuntime getProcessRuntime() {
        return null;
    }

    @Override
    public InternalProcessRuntime internalGetProcessRuntime() {
        return null;
    }

    @Override
    public void closeLiveQuery(InternalFactHandle factHandle) {

    }

    @Override
    public void addPropagation(PropagationEntry propagationEntry) {

    }

    @Override
    public void flushPropagations() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean tryDeactivate() {
        return false;
    }

    @Override
    public Iterator<? extends PropagationEntry> getActionsIterator() {
        return null;
    }

    @Override
    public void removeGlobal(String identifier) {

    }

    @Override
    public void notifyWaitOnRest() {

    }

    @Override
    public void cancelActivation(Activation activation, boolean declarativeAgenda) {

    }

    @Override
    public void addEventListener(RuleRuntimeEventListener listener) {

    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener listener) {

    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return null;
    }

    @Override
    public AgendaEventSupport getAgendaEventSupport() {
        return null;
    }

    @Override
    public RuleRuntimeEventSupport getRuleRuntimeEventSupport() {
        return null;
    }

    @Override
    public RuleEventListenerSupport getRuleEventSupport() {
        return null;
    }

    @Override
    public void addEventListener(AgendaEventListener listener) {

    }

    @Override
    public void removeEventListener(AgendaEventListener listener) {

    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return null;
    }

    @Override
    public void addEventListener(KieBaseEventListener listener) {

    }

    @Override
    public void removeEventListener(KieBaseEventListener listener) {

    }

    @Override
    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return null;
    }
}