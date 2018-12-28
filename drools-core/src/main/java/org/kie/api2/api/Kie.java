package org.kie.api2.api;

import org.kie.api.KieBase;

public interface Kie {

    public static KieBase getKieBase() {
        return null;
    }

    static Runtime.Factory runtime() {
        return null;
    }

    interface Runtime {

        interface Factory {

            <U extends Unit> UnitRuntime<U> of(U unit);
        }
    }
}
