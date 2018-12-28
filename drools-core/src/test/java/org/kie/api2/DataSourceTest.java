package org.kie.api2;

import org.junit.Test;
import org.kie.api2.api.DataSource;
import org.kie.api2.api.Kie;
import org.kie.api2.api.RuleRuntime;
import org.kie.api2.api.RuleUnit;
import org.kie.api2.api.UnitRuntime;
import org.kie.api2.impl.DataSourceImpl;

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
        final DataSource<String> ds = new DataSourceImpl<>();
        ds.add("foo");
        UnitRuntime<MyUnit> rt = Kie.runtime().of(new MyUnit(ds));
        rt.run();

    }
}

class MyUnit implements RuleUnit {

    private DataSource<String> strings;

    public MyUnit(DataSource<String> strings) {
        this.strings = strings;
    }
}
