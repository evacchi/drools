package org.kie.api2.api;

public interface UnitInstance<T> {

    void run();

    T unit();
}
