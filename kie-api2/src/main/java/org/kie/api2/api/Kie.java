package org.kie.api2.api;

import java.util.UUID;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api2.impl.KieRuntimeFactoryImpl;

public interface Kie {

    static Runtime.Factory runtime() {
        return runtime(new KnowledgeBaseImpl(UUID.randomUUID().toString(), new RuleBaseConfiguration()));
    }

    static Runtime.Factory runtime(InternalKnowledgeBase kBase) {
        return new KieRuntimeFactoryImpl(kBase);
    }

    interface Runtime {

        interface Factory {

            <U extends Unit> UnitInstance<U> of(U unit);

            <U extends RuleUnit> RuleUnitInstance<U> of(U unit);

            <U extends ProcessUnit> ProcessUnitInstance<U> of(U unit);
        }
    }
}
