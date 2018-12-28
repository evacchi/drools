package org.kie.api2;

import org.junit.Test;
import org.kie.api2.api.DataSource;
import org.kie.api2.impl.DataSourceImpl;

public class DataSourceTest {
    @Test
    public void testDs() {
        DataSource<String> ds = new DataSourceImpl<>();
        ds.add("foo");
        ds.add("bar");
        ds.add("baz");
    }
}
