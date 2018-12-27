package org.drools.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.drools.core.common.EventSupport;
import org.drools.core.event.AgendaEventSupport;
import org.drools.core.event.RuleEventListenerSupport;
import org.drools.core.event.RuleRuntimeEventSupport;
import org.kie.api.KieBase;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.internal.event.rule.RuleEventListener;

public class SessionEventSupport implements EventSupport {

    private RuleRuntimeEventSupport ruleRuntimeEventSupport;
    private AgendaEventSupport agendaEventSupport;
    private List<KieBaseEventListener> kieBaseEventListeners;
    private KieBase kBase;
    private RuleEventListenerSupport ruleEventListenerSupport;

    public SessionEventSupport(
            RuleRuntimeEventSupport ruleRuntimeEventSupport,
            AgendaEventSupport agendaEventSupport,
            KieBase kBase,
            RuleEventListenerSupport ruleEventListenerSupport) {
        this.ruleRuntimeEventSupport = ruleRuntimeEventSupport;
        this.agendaEventSupport = agendaEventSupport;
        this.kBase = kBase;
        this.ruleEventListenerSupport = ruleEventListenerSupport;
        this.kieBaseEventListeners = new ArrayList<>();
    }

    public RuleRuntimeEventSupport getRuleRuntimeEventSupport() {
        return this.ruleRuntimeEventSupport;
    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return null;
    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return null;
    }

    public AgendaEventSupport getAgendaEventSupport() {
        return this.agendaEventSupport;
    }

    public void setRuleRuntimeEventSupport(RuleRuntimeEventSupport ruleRuntimeEventSupport) {
        this.ruleRuntimeEventSupport = ruleRuntimeEventSupport;
    }

    public void setAgendaEventSupport(AgendaEventSupport agendaEventSupport) {
        this.agendaEventSupport = agendaEventSupport;
    }

    public void addEventListener(final RuleRuntimeEventListener listener) {
        this.ruleRuntimeEventSupport.addEventListener(listener);
    }

    //
    public void removeEventListener(final RuleRuntimeEventListener listener) {
        this.ruleRuntimeEventSupport.removeEventListener(listener);
    }

    //
    public void addEventListener(final AgendaEventListener listener) {
        this.agendaEventSupport.addEventListener(listener);
    }

    //
    public void removeEventListener(final AgendaEventListener listener) {
        this.agendaEventSupport.removeEventListener(listener);
    }

    public void addEventListener(KieBaseEventListener listener) {
        this.kBase.addEventListener(listener);
        this.kieBaseEventListeners.add(listener);
    }

    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return Collections.unmodifiableCollection(kieBaseEventListeners);
    }

    public void removeEventListener(KieBaseEventListener listener) {
        this.kBase.removeEventListener(listener);
        this.kieBaseEventListeners.remove(listener);
    }

    public RuleEventListenerSupport getRuleEventSupport() {
        return ruleEventListenerSupport;
    }

    public void addEventListener(final RuleEventListener listener) {
        this.ruleEventListenerSupport.addEventListener(listener);
    }

    public void removeEventListener(final RuleEventListener listener) {
        this.ruleEventListenerSupport.removeEventListener(listener);
    }

    public void clear() {
        this.kieBaseEventListeners.clear();
        this.ruleRuntimeEventSupport.clear();
        this.ruleEventListenerSupport.clear();
        this.agendaEventSupport.clear();
    }
}
