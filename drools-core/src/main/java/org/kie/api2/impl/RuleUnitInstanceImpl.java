package org.kie.api2.impl;

import org.drools.core.common.DefaultAgenda;
import org.drools.core.common.InternalAgenda;
import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.api.RuleUnitInstance;

public class RuleUnitInstanceImpl<T extends RuleUnit> implements RuleUnitInstance<T> {

    private RuleUnit unit;
    private InternalKnowledgeBase kBase;
    private InternalAgenda agenda;

    public RuleUnitInstanceImpl(RuleUnit unit, InternalKnowledgeBase kBase) {
        this.unit = unit;
        this.kBase = kBase;
        this.agenda = new DefaultAgenda(kBase);
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

    }
}
