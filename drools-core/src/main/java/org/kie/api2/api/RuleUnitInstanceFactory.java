package org.kie.api2.api;

public interface RuleUnitInstanceFactory extends UnitInstanceFactory {

    <U extends RuleUnit> RuleUnitInstance<U> create(U unit);
}
