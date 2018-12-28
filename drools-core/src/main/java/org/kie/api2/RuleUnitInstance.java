package org.kie.api2;

import org.drools.core.common.DefaultAgenda;
import org.drools.core.common.InternalAgenda;
import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.api.UnitRuntime;

public class RuleUnitInstance<T extends RuleUnit> implements UnitRuntime<T> {

    private RuleUnit unit;
    private InternalKnowledgeBase kBase;
    private InternalAgenda agenda;

    public RuleUnitInstance(RuleUnit unit, InternalKnowledgeBase kBase) {
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
}
