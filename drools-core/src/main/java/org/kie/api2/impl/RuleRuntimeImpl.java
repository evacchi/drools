package org.kie.api2.impl;

import org.kie.api2.api.RuleRuntime;
import org.kie.api2.api.RuleUnit;

public class RuleRuntimeImpl<T extends RuleUnit> implements RuleRuntime<T> {

    public RuleRuntimeImpl(T unit) {
    }
}
