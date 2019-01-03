package org.kie.api2;

import org.junit.Ignore;
import org.kie.api2.api.Kie;
import org.kie.api2.api.ProcessUnit;
import org.kie.api2.api.ProcessUnitInstance;

public class ProcessUnitTest {

    @Ignore("Failing because of class loading issue")
    public void startTest() {
        ProcessUnitInstance<MyProcessUnit> pu = Kie.runtime().of(new MyProcessUnit());
        pu.run();
    }
}

class MyProcessUnit implements ProcessUnit {

}