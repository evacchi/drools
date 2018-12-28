package org.kie.api2;

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.api2.api.DataSource;
import org.kie.api2.api.RuleRuntime;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.impl.DataSourceImpl;
import org.kie.api2.impl.RuleRuntimeImpl;

public class DataSourceTest {
    @Test
    public void testDs() {
        DataSource<String> ds = new DataSourceImpl<>();
        ds.add("foo");
        ds.add("bar");
        ds.add("baz");
    }

    @Test
    public void testRuleRuntime() {
        KieBase kieBase = null;
        UnitExecutor executor = new UnitExecutor(kieBase);
        DataSource<String> ds = new DataSourceImpl<>();
        ds.add("foo");

        RuleRuntime ruleRuntime = KieRuntimeFactory.of(kieBase).get(RuleRuntime.class);
        RuleUnitInstance<MyUnit> ui = ruleRuntime.instanceOf(new MyUnit(ds));
        executor.submit(ui);

        executor.run();
    }
}

class MyUnit implements RuleUnit{

    private DataSource<String> strings;

    public MyUnit(DataSource<String> strings) {
        this.strings = strings;
    }
}
