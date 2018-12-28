package org.kie.api2.api;

import org.kie.api2.RuleUnitInstance;

public interface RuleRuntime {

    <T extends RuleUnit> RuleUnitInstance<T> instanceOf(T unit);
}
