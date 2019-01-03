package org.kie.api2;

import org.junit.Test;
import org.kie.api2.api.Kie;
import org.kie.api2.api.ProcessUnit;
import org.kie.api2.api.ProcessUnitInstance;

public class ProcessUnitTest {

    @Test
//    @Ignore("Failing because of class loading issue")
    public void startTest() {
        // example usage of a ProcessUnit
        // notice how the API is type-safe in the type of the Unit, returning
        // a ProcessUnitInstance because MyProcessUnit implements ProcessUnit
        ProcessUnitInstance<MyProcessUnit> pu = Kie.runtime().of(new MyProcessUnit());
        pu.run();
    }
}

class MyProcessUnit implements ProcessUnit {

}