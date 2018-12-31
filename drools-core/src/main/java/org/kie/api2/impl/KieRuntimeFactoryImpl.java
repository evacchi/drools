package org.kie.api2.impl;

import java.util.NoSuchElementException;

import org.kie.api.KieBase;
import org.kie.api2.api.Kie;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.api.RuleUnitInstance;
import org.kie.api2.api.RuleUnitInstanceFactory;
import org.kie.api2.api.RuleUnitInstanceFactoryImpl;
import org.kie.api2.api.Unit;
import org.kie.api2.api.UnitInstance;
import org.kie.api2.api.UnitInstanceFactory;

public class KieRuntimeFactoryImpl implements Kie.Runtime.Factory {

    private final KieBase kBase;

    public KieRuntimeFactoryImpl(KieBase kBase) {
        this.kBase = kBase;
    }

    @Override
    public <U extends Unit> UnitInstance<U> of(U unit) {
        UnitInstanceFactory factory = lookup(unit.getClass());
        return factory.create(unit);
    }

    @Override
    public <U extends RuleUnit> RuleUnitInstance<U> of(U unit) {
        RuleUnitInstanceFactory factory = lookupFactory(RuleUnitInstanceFactory.class);
        return (RuleUnitInstance<U>) factory.create(unit);
    }

    private UnitInstanceFactory lookup(Class<? extends Unit> unitClass) {
        if (unitClass == RuleUnit.class) {
            return lookupFactory(RuleUnitInstanceFactory.class);
        }
        throw new NoSuchElementException();
    }

    private <T extends UnitInstanceFactory> T lookupFactory(Class<T> cls) {
        // wizardry left as exercise for the reader
        if (cls == RuleUnitInstanceFactory.class) {
            return (T) new RuleUnitInstanceFactoryImpl(kBase);
        } else {
            throw new NoSuchElementException();
        }
    }
}
